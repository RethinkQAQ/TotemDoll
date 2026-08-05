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

package com.rethinkqaq.totemdoll.doll;

import com.rethinkqaq.totemdoll.utils.DollResourceId;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModels;

public record DollStyle(
        DollResourceId id,
        String displayName,
        String translationKey,
        DollResourceId model,
        boolean usesCustomModel,
        DollResourceId templateId,
        boolean userCreated,
        DollSkinDefinition skin,
        DollStyleOrigin origin,
        Map<String, DollResourceId> textures,
        List<DollAnimationDefinition> animations,
        String modelType,
        DollResourceId definitionSource
) {

    public Component label() {
        return translationKey == null || translationKey.isBlank()
                ? Component.literal(displayName)
                : Component.translatable(translationKey);
    }

    public boolean supportsSkin() {
        return skin != null;
    }

    public boolean isTemplate() {
        return !userCreated && supportsSkin();
    }

    public boolean isLocal() {
        return userCreated || origin == DollStyleOrigin.LOCAL;
    }

    public boolean hasAnimations() {
        return hasDynamicTextures() || hasDynamicModel();
    }

    public boolean isBoneModel() {
        return "mesh".equals(modelType);
    }

    public boolean hasDynamicModel() {
        var model = DollBoneModels.get(id);
        return model != null && !model.animations().isEmpty();
    }

    public boolean hasDynamicTextures() {
        return animations != null && !animations.isEmpty();
    }

    public DollAnimationDefinition animation(String id) {
        return animations.stream().filter(animation -> animation.id().equals(id)).findFirst().orElse(null);
    }
}
