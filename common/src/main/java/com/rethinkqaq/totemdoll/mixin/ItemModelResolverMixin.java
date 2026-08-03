package com.rethinkqaq.totemdoll.mixin;

import org.spongepowered.asm.mixin.Mixin;
//? >= 1.21.4 {
/*import com.rethinkqaq.totemdoll.client.DollThirdPersonState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;*/
//?} else {
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
            boolean leftHand,
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
    *///?}
}
