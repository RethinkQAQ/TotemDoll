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

import com.mojang.blaze3d.vertex.PoseStack;
import com.rethinkqaq.totemdoll.client.DollBoneRenderer;
import com.rethinkqaq.totemdoll.client.DollPreviewContext;
import com.rethinkqaq.totemdoll.config.TotemDollConfig;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? >= 1.21.4 {
/*import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
*///?} else {
import net.minecraft.client.resources.model.BakedModel;
//?}

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Inject(
            //? >= 1.21.9 {
            /*method = "renderStatic",
            *///?} else if >= 1.21.5 {
            /*method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
            *///?} else if >= 1.21.4 {
            /*method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",*/
            //?} else {
            method = "render",
            //?}
            at = @At("HEAD"),
            cancellable = true
            //? >= 1.21.9 {
            /*, require = 0
            *///?}
    )
    private void totemdoll$renderBoneModel(
            //? >= 1.21.9 {
            /*ItemStack stack, ItemDisplayContext context, int light, int overlay, PoseStack poseStack, MultiBufferSource buffers, Level level, int seed, CallbackInfo ci
            *///?} else if >= 1.21.5 {
            /*LivingEntity entity, ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, Level level, int light, int overlay, int seed, CallbackInfo ci
            *///?} else if >= 1.21.4 {
            /*LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack, MultiBufferSource buffers, Level level, int light, int overlay, int seed, CallbackInfo ci*/
            //?} else {
            ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay, BakedModel referenceModel, CallbackInfo ci
             //?}
    ) {
        if (!stack.is(Items.TOTEM_OF_UNDYING)) return;
        if (DollPreviewContext.isNativeRender()) return;
        DollStyle style = DollPreviewContext.current();
        if (style == null) style = TotemDollConfig.selectedStyle();
        if (style == null || !DollBoneModels.contains(style.id())) return;

        //? >= 1.21.5 {
        /*boolean leftHand = context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        *///?}

        //? >= 1.21.3 {
        /*float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        *///?} else {
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        //?}
        if (DollBoneRenderer.render(style, context, leftHand, poseStack, buffers, light, overlay, partialTick)) {
            ci.cancel();
        }
    }
}
