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

package com.rethinkqaq.totemdoll.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
//? >= 1.21.6 {
import com.rethinkqaq.totemdoll.client.gui.DollGuiPreviewRenderer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
//?} else {
/*import com.rethinkqaq.totemdoll.utils.Dummy;
*///?}

@Mixin(
        //? >= 1.21.6 {
        GameRenderer.class
        //?} else {
        /*Dummy.class
        *///?}
)
public abstract class GameRendererPreviewMixin {
    //? >= 1.21.6 {
    @ModifyArgs(
            method = "<init>",
//? >= 26.2 {
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/renderer/state/gui/GuiRenderState;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;Ljava/util/List;)V")
//?} else if >= 1.21.10 {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/renderer/state/gui/GuiRenderState;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;Ljava/util/List;)V")
            *///?} else {
            /*at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/gui/render/state/GuiRenderState;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Ljava/util/List;)V")
            *///?}
    )
    private void totemdoll$addPreviewRenderer(Args args) {
        //? >= 26.2 {
        int rendererIndex = 2;
        //?} else if >= 1.21.10 {
        /*int rendererIndex = 4;
        *///?} else {
        /*int rendererIndex = 2;
        *///?}
        addPreviewRenderer(args, rendererIndex);
    }

    private static void addPreviewRenderer(Args args, int rendererIndex) {
        List<PictureInPictureRenderer<?>> renderers = args.get(rendererIndex);
        List<PictureInPictureRenderer<?>> result = new ArrayList<>(renderers);
        //? >= 26.2 {
        result.add(new DollGuiPreviewRenderer());
        //?} else {
        /*result.add(new DollGuiPreviewRenderer(args.get(1)));
        *///?}
        args.set(rendererIndex, result);
    }
    //?} else {
    /*
    *///?}
}
