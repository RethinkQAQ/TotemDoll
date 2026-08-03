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
//? < 1.21.4 {
import net.minecraft.client.resources.model.BakedModel;
//?}
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    //? < 1.21.4 {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void totemdoll$renderBoneModel(ItemStack stack, ItemDisplayContext context, boolean leftHand,
                                           PoseStack poseStack, MultiBufferSource buffers, int light, int overlay,
                                           BakedModel referenceModel, CallbackInfo callback) {
        if (!stack.is(Items.TOTEM_OF_UNDYING)) return;
        DollStyle style = DollPreviewContext.current();
        if (style == null) style = TotemDollConfig.selectedStyle();
        if (style == null || !DollBoneModels.contains(style.id())) return;

        //? >= 1.21.3 {
        /*float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        *///?} else {
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        //?}
        if (DollBoneRenderer.render(style, context, leftHand, poseStack, buffers, light, overlay, partialTick)) {
            callback.cancel();
        }
    }
    //?}
}
