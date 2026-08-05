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
/*import com.rethinkqaq.totemdoll.client.DollThirdPersonState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
//? >= 1.21.9 {
/^import net.minecraft.world.entity.ItemOwner;
^///?}
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
//? >= 1.21.6 {
/^import net.minecraft.world.level.Level;
^///?}
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?} else {
import com.rethinkqaq.totemdoll.utils.Dummy;
//?}

@Mixin(
        //? >= 1.21.4 {
        /*ItemModelResolver.class
        *///?} else {
        Dummy.class
        //?}
)
public abstract class ItemModelResolverMixin {

    //? >= 1.21.4 {
    /*@Inject(method = "updateForLiving", at = @At("HEAD"))
    private void totemdoll$markLivingItem(
            ItemStackRenderState renderState,
            ItemStack stack,
            ItemDisplayContext context,
            //? < 1.21.5 {
            boolean leftHand,
            //?}
            LivingEntity entity,
            CallbackInfo callback
    ) {
        DollThirdPersonState.mark(renderState, stack, context);
    }

    @Inject(method = "updateForNonLiving", at = @At("HEAD"))
    private void totemdoll$markNonLivingItem(
            ItemStackRenderState renderState,
            ItemStack stack,
            ItemDisplayContext context,
            Entity entity,
            CallbackInfo callback
    ) {
        DollThirdPersonState.mark(renderState, stack, context);
    }

    //? >= 1.21.6 {
    /^@Inject(method = "updateForTopItem", at = @At("RETURN"))
    private void totemdoll$markGuiItem(
            ItemStackRenderState renderState,
            ItemStack stack,
            ItemDisplayContext context,
            Level level,
            //? >= 1.21.9 {
            /^¹ItemOwner entity,
            ¹^///?} else {
            LivingEntity entity,
            //?}
            int seed,
            CallbackInfo callback
    ) {
        DollThirdPersonState.mark(renderState, stack, context);
    }
    ^///?}
    *///?}
}
