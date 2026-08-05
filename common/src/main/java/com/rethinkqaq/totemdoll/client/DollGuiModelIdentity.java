package com.rethinkqaq.totemdoll.client;

import com.rethinkqaq.totemdoll.doll.DollStyle;
//? >= 1.21.8 {
/*import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
*///?} else if >= 1.21.6 {
/*import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
*///?}

public final class DollGuiModelIdentity {
    private DollGuiModelIdentity() {
    }

    public static void mark(Object renderState, Object context, DollStyle style) {
        //? >= 1.21.8 {
        /*if (!(renderState instanceof TrackingItemStackRenderState trackingState)
                || context != ItemDisplayContext.GUI) {
            return;
        }
        trackingState.appendModelIdentityElement(style.id());
        if (style.hasDynamicModel() || style.hasDynamicTextures() || style.supportsSkin()) {
            trackingState.setAnimated();
        }
        *///?} else if >= 1.21.6 {
        /*if (!(renderState instanceof ItemStackRenderState) || context != ItemDisplayContext.GUI) {
            return;
        }
        ((ItemStackRenderState) renderState).clearModelIdentity();
        ((ItemStackRenderState) renderState).appendModelIdentityElement(style.id());
        if (style.hasDynamicModel() || style.hasDynamicTextures() || style.supportsSkin()) {
            ((ItemStackRenderState) renderState).setAnimated();
        }
        *///?}
    }
}
