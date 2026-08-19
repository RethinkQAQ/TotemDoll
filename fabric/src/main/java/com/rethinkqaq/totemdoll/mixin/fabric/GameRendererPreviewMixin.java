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
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.rethinkqaq.totemdoll.client.gui.DollGuiPreviewRenderer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.render.GuiRenderer;
//? >= 26.1.2 {
/^import net.minecraft.client.renderer.state.gui.GuiRenderState;
^///?} else {
import net.minecraft.client.gui.render.state.GuiRenderState;
//?}
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
//? < 26.2 {
/^import net.minecraft.client.renderer.MultiBufferSource;
^///?}
//? >= 1.21.10 {
/^import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
^///?}
import org.spongepowered.asm.mixin.injection.At;
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
    /*@WrapOperation(
            method = "<init>",
            at = @At(value = "NEW", target = "Lnet/minecraft/client/gui/render/GuiRenderer;")
    )
    private GuiRenderer totemdoll$wrapGuiRenderer(
            GuiRenderState guiRenderState,
            //? >= 26.2 {
/^            FeatureRenderDispatcher featureRenderDispatcher,
            List<PictureInPictureRenderer<?>> renderers,
            ^///?} else if >= 1.21.10 {
/^            MultiBufferSource.BufferSource bufferSource,
            SubmitNodeCollector submitNodeCollector,
            FeatureRenderDispatcher featureRenderDispatcher,
            List<PictureInPictureRenderer<?>> renderers,
            ^///?} else {
            MultiBufferSource.BufferSource bufferSource,
            List<PictureInPictureRenderer<?>> renderers,
            //?}
            Operation<GuiRenderer> original
    ) {
        List<PictureInPictureRenderer<?>> result = new ArrayList<>(renderers);
        //? >= 26.2 {
/^        result.add(new DollGuiPreviewRenderer());
        return original.call(guiRenderState, featureRenderDispatcher, result);
        ^///?} else if >= 1.21.10 {
/^        result.add(new DollGuiPreviewRenderer(bufferSource));
        return original.call(guiRenderState, bufferSource, submitNodeCollector, featureRenderDispatcher, result);
        ^///?} else {
        result.add(new DollGuiPreviewRenderer(bufferSource));
        return original.call(guiRenderState, bufferSource, result);
        //?}
    }
    *///?} else {
    
    //?}
}
