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
//? >= 1.21.4 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.rethinkqaq.totemdoll.client.DollBoneRenderer;
import com.rethinkqaq.totemdoll.client.DollThirdPersonState;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
//? >= 1.21.10 {
/^import net.minecraft.client.renderer.SubmitNodeCollector;
^///?} else {
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
//?}
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?} else {
import com.rethinkqaq.totemdoll.utils.Dummy;
//?}

@Mixin(
        //? >= 1.21.4 {
        /*ItemStackRenderState.class
        *///?} else {
        Dummy.class
        //?}
)
public abstract class ItemStackRenderStateMixin {

    //? >= 1.21.4 {
    /*@Shadow
    private ItemDisplayContext displayContext;

    //? < 1.21.5 {
    @Shadow
    private boolean isLeftHand;
    //?}

//? >= 1.21.10 {
    /^@Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void totemdoll$submitFormat3(
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int light,
            int overlay,
            int outlineColor,
            CallbackInfo callback
    ) {
        DollStyle style = DollThirdPersonState.get((ItemStackRenderState) (Object) this);
        if (style == null) return;

        boolean isLeftHand = displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (DollBoneRenderer.submit(
                style,
                displayContext,
                isLeftHand,
                poseStack,
                nodeCollector,
                light,
                overlay,
                outlineColor
        )) {
            callback.cancel();
        }
    }
    ^///?} else {
    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private void totemdoll$renderFormat3(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay,
            CallbackInfo callback
    ) {
        DollStyle style = DollThirdPersonState.get((ItemStackRenderState) (Object) this);
        if (style == null) return;

        //? >= 1.21.5 {
        /^boolean isLeftHand = displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        ^///?}

        float partialTick = Minecraft.getInstance()
                .getDeltaTracker()
                .getGameTimeDeltaPartialTick(false);
        if (DollBoneRenderer.render(
                style,
                displayContext,
                isLeftHand,
                poseStack,
                buffers,
                light,
                overlay,
                partialTick
        )) {
            callback.cancel();
        }
    }
    *///?}
}
