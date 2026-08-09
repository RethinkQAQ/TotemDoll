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

package com.rethinkqaq.totemdoll.mixin;

import org.spongepowered.asm.mixin.Mixin;
//? >= 1.21.6 {
import com.rethinkqaq.totemdoll.client.gui.DollGuiPreviewAccess;
import com.rethinkqaq.totemdoll.client.gui.DollGuiPreviewRenderState;
//? >= 26.1.2 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
*///?}
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
//?} else if >= 1.21.4 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.rethinkqaq.totemdoll.client.DollBoneRenderer;
import com.rethinkqaq.totemdoll.client.DollPreviewContext;
import com.rethinkqaq.totemdoll.config.TotemDollConfig;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?} else {
/*import com.rethinkqaq.totemdoll.utils.Dummy;
*///?}

@Mixin(
        //? >= 26.1.2 {
        GuiGraphicsExtractor.class
        //?} else if >= 1.21.4 {
        /*GuiGraphics.class
        *///?} else {
        /*Dummy.class
        *///?}
)
public abstract class GuiGraphicsMixin
        //? >= 1.21.6 {
        implements DollGuiPreviewAccess
        //?}
{
    //? >= 1.21.6 {
    @Shadow @Final private GuiRenderState guiRenderState;

    @Override
    public void totemdoll$submitPreview(DollGuiPreviewRenderState state) {
        //? >= 26.1 {
        guiRenderState.addPicturesInPictureState(state);
        //?} else {
        /*guiRenderState.submitPicturesInPictureState(state);
        *///?}
    }
    //?} else if >= 1.21.4 {
    /*@Shadow @Final private PoseStack pose;
    @Shadow @Final private MultiBufferSource.BufferSource bufferSource;

    @Inject(
            method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void totemdoll$renderGuiItem(
            LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, CallbackInfo callback
    ) {
        if (!stack.is(Items.TOTEM_OF_UNDYING) || DollPreviewContext.isNativeRender()) {
            return;
        }
        DollStyle style = DollPreviewContext.current();
        if (style == null) {
            style = TotemDollConfig.selectedStyle();
        }
        if (style == null || !DollBoneModels.contains(style.id())) {
            return;
        }

        pose.pushPose();
        pose.translate(x + 8, y + 8, 150);
        pose.scale(16.0F, -16.0F, 16.0F);
        float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        boolean rendered = DollBoneRenderer.render(style, ItemDisplayContext.GUI, false, pose, bufferSource,
                15728880, OverlayTexture.NO_OVERLAY, partialTicks);
        pose.popPose();
        if (rendered) {
            bufferSource.endBatch();
            callback.cancel();
        }
    }
    *///?}
}
