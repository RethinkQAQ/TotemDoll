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
/*import com.rethinkqaq.totemdoll.client.gui.DollGuiPreviewRenderer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
*///?} else {
import com.rethinkqaq.totemdoll.utils.Dummy;
//?}

@Mixin(
        //? >= 1.21.6 {
        /*GameRenderer.class
        *///?} else {
        Dummy.class
        //?}
)
public abstract class GameRendererPreviewMixin {
    //? >= 1.21.6 {
    /*@ModifyArgs(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/gui/render/state/GuiRenderState;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Ljava/util/List;)V")
    )
    private void totemdoll$addPreviewRenderer(Args args) {
        List<PictureInPictureRenderer<?>> renderers = args.get(2);
        List<PictureInPictureRenderer<?>> result = new ArrayList<>(renderers);
        result.add(new DollGuiPreviewRenderer(args.get(1)));
        args.set(2, result);
    }
    *///?}
}
