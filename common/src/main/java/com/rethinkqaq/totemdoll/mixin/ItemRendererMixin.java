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

import com.rethinkqaq.totemdoll.config.TotemDollConfig;
import com.rethinkqaq.totemdoll.client.DollPreviewContext;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.DollAnimationManager;
import com.rethinkqaq.totemdoll.doll.DollAnimationModels;
import com.rethinkqaq.totemdoll.client.DollBoneRenderer;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void totemdoll$renderBoneModel(ItemStack stack, ItemDisplayContext context, boolean leftHand,
                                           PoseStack poseStack, MultiBufferSource buffers, int light, int overlay,
                                           BakedModel referenceModel, CallbackInfo callback) {
        if (!stack.is(Items.TOTEM_OF_UNDYING)) return;
        DollStyle style = DollPreviewContext.current();
        if (style == null) style = TotemDollConfig.selectedStyle();
        if (!DollBoneModels.contains(style.id())) return;
        //? >=1.21.4 {
        /*float partialTick = Minecraft.getInstance().getDeltaTracker()
                .getGameTimeDeltaPartialTick(false);*/
        //?} else {
        float partialTick = Minecraft.getInstance().getTimer()
                .getGameTimeDeltaPartialTick(false);
        //?}
        if (DollBoneRenderer.render(style, context, leftHand, poseStack, buffers, light, overlay,
                referenceModel, partialTick)) callback.cancel();
    }

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void totemdoll$selectDollModel(
            ItemStack stack,
            Level level,
            LivingEntity entity,
            int seed,
            CallbackInfoReturnable<BakedModel> callback
    ) {
        if (!stack.is(Items.TOTEM_OF_UNDYING)) {
            return;
        }

        DollStyle style = DollPreviewContext.current();
        if (style == null) {
            style = TotemDollConfig.selectedStyle();
        }
        if (!style.usesCustomModel()) {
            return;
        }

        if (style.hasAnimations()) {
            var animation = DollAnimationManager.displayAnimation(style);
            if (animation == null) return;
            int frame = DollAnimationManager.currentFrame(style, animation.id());
            ResourceLocation animatedModelId = DollAnimationModels.frameModelId(
                    style,
                    animation.frames().get(Math.min(frame, animation.frames().size() - 1))
            );
            BakedModel animatedModel = totemdoll$findModel(animatedModelId);
            if (animatedModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
                callback.setReturnValue(animatedModel);
                return;
            }
        }

        BakedModel model = totemdoll$findModel(style.model());
        if (model != Minecraft.getInstance().getModelManager().getMissingModel()) {
            callback.setReturnValue(model);
        }
    }

    private static BakedModel totemdoll$findModel(ResourceLocation modelId) {
        var manager = Minecraft.getInstance().getModelManager();
        //? >=1.21.4 {
        /*BakedModel model = manager.getModel(new ModelResourceLocation(modelId, "inventory"));*/
        //?} else {
        BakedModel model = manager.getModel(ModelResourceLocation.inventory(modelId));
        //?}
        if (model == manager.getMissingModel()) {
            model = manager.getModel(new ModelResourceLocation(modelId, "standalone"));
        }
        if (model == manager.getMissingModel()) {
            model = manager.getModel(new ModelResourceLocation(modelId, "fabric_resource"));
        }
        return model;
    }
}
