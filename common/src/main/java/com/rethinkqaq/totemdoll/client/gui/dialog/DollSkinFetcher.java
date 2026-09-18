/*
 * Totem Doll
 * Copyright (C) 2026 Rethink_QAQ
 *
 * This file is part of Totem Doll.
 *
 * Totem Doll is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Totem Doll is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Totem Doll. If not, see <https://www.gnu.org/licenses/>.
 */

package com.rethinkqaq.totemdoll.client.gui.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

final class DollSkinFetcher {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final String USERNAME_PATTERN = "[A-Za-z0-9_]{1,16}";
    private static final int MAX_TEXTURE_BYTES = 4 * 1024 * 1024;

    static CompletableFuture<byte[]> fetch(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fetchBlocking(playerName);
            } catch (FetchException exception) {
                throw new CompletionException(exception);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new CompletionException(new FetchException(Reason.NETWORK, exception));
            } catch (IOException exception) {
                throw new CompletionException(new FetchException(Reason.NETWORK, exception));
            }
        });
    }

    private static byte[] fetchBlocking(String playerName) throws IOException, InterruptedException {
        String name = playerName == null ? "" : playerName.trim();
        if (!name.matches(USERNAME_PATTERN)) {
            throw new FetchException(Reason.INVALID_NAME);
        }

        JsonObject profile = getJson(
                URI.create("https://api.mojang.com/users/profiles/minecraft/" + name),
                Reason.NOT_FOUND
        );
        String uuid = string(profile, "id");
        JsonArray properties = profile.has("properties")
                ? profile.getAsJsonArray("properties") : null;

        JsonObject texturesProperty = null;
        if (properties != null) {
            for (var propertyElement : properties) {
                JsonObject property = propertyElement.getAsJsonObject();
                if ("textures".equals(stringOrNull(property, "name"))) {
                    texturesProperty = property;
                    break;
                }
            }
        }

        if (texturesProperty == null || !texturesProperty.has("value")) {
            JsonObject sessionProfile = getJson(
                    URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid),
                    Reason.NO_SKIN
            );
            properties = sessionProfile.has("properties")
                    ? sessionProfile.getAsJsonArray("properties") : null;
            if (properties != null) {
                for (var propertyElement : properties) {
                    JsonObject property = propertyElement.getAsJsonObject();
                    if ("textures".equals(stringOrNull(property, "name"))) {
                        texturesProperty = property;
                        break;
                    }
                }
            }
        }

        if (texturesProperty == null || !texturesProperty.has("value")) {
            throw new FetchException(Reason.NO_SKIN);
        }

        try {
            String encoded = texturesProperty.get("value").getAsString();
            JsonObject textureData = JsonParser.parseString(
                    new String(Base64.getDecoder().decode(encoded), java.nio.charset.StandardCharsets.UTF_8)
            ).getAsJsonObject();
            JsonObject textures = textureData.getAsJsonObject("textures");
            JsonObject skin = textures == null ? null : textures.getAsJsonObject("SKIN");
            String skinUrl = skin == null ? null : stringOrNull(skin, "url");
            if (skinUrl == null) throw new FetchException(Reason.NO_SKIN);
            return downloadTexture(skinUrl);
        } catch (RuntimeException exception) {
            throw new FetchException(Reason.INVALID_RESPONSE, exception);
        }
    }

    private static JsonObject getJson(URI uri, Reason missingReason) throws IOException, InterruptedException {
        HttpResponse<String> response = send(uri, HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
        if (response.statusCode() == 204 || response.statusCode() == 404) {
            throw new FetchException(missingReason);
        }
        if (response.statusCode() == 429) {
            throw new FetchException(Reason.RATE_LIMITED);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new FetchException(Reason.NETWORK);
        }
        try {
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (RuntimeException exception) {
            throw new FetchException(Reason.INVALID_RESPONSE, exception);
        }
    }

    private static byte[] downloadTexture(String value) throws IOException, InterruptedException {
        URI source;
        try {
            source = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw new FetchException(Reason.INVALID_RESPONSE, exception);
        }
        if (!"textures.minecraft.net".equalsIgnoreCase(source.getHost())
                || (!"https".equalsIgnoreCase(source.getScheme()) && !"http".equalsIgnoreCase(source.getScheme()))) {
            throw new FetchException(Reason.INVALID_RESPONSE);
        }
        try {
            source = new URI("https", source.getUserInfo(), source.getHost(), source.getPort(),
                    source.getPath(), source.getQuery(), source.getFragment());
        } catch (java.net.URISyntaxException exception) {
            throw new FetchException(Reason.INVALID_RESPONSE, exception);
        }

        HttpResponse<byte[]> response = send(source, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() == 429) throw new FetchException(Reason.RATE_LIMITED);
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new FetchException(Reason.NETWORK);
        }
        if (response.body().length == 0 || response.body().length > MAX_TEXTURE_BYTES) {
            throw new FetchException(Reason.INVALID_TEXTURE);
        }
        return response.body();
    }

    private static <T> HttpResponse<T> send(URI uri, HttpResponse.BodyHandler<T> handler)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json, image/png")
                .header("User-Agent", "TotemDoll")
                .GET()
                .build();
        return CLIENT.send(request, handler);
    }

    private static String string(JsonObject object, String member) throws IOException {
        String value = stringOrNull(object, member);
        if (value == null || value.isBlank()) throw new FetchException(Reason.INVALID_RESPONSE);
        return value;
    }

    private static String stringOrNull(JsonObject object, String member) {
        return object != null && object.has(member) && !object.get(member).isJsonNull()
                ? object.get(member).getAsString() : null;
    }

    enum Reason {
        INVALID_NAME,
        NOT_FOUND,
        NO_SKIN,
        RATE_LIMITED,
        INVALID_RESPONSE,
        INVALID_TEXTURE,
        NETWORK
    }

    static final class FetchException extends IOException {
        private final Reason reason;

        FetchException(Reason reason) {
            this(reason, null);
        }

        FetchException(Reason reason, Throwable cause) {
            super(reason.name(), cause);
            this.reason = reason;
        }

        Reason reason() {
            return reason;
        }
    }

    private DollSkinFetcher() {
    }
}
