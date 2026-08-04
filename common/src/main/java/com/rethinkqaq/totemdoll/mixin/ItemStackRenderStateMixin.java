package com.rethinkqaq.totemdoll.mixin;

import org.spongepowered.asm.mixin.Mixin;
//? >= 1.21.4 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.rethinkqaq.totemdoll.client.DollBoneRenderer;
import com.rethinkqaq.totemdoll.client.DollThirdPersonState;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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

    //? < 1.21.4 {
    @Shadow
    private boolean isLeftHand;
    //?}

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
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
