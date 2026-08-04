package com.rethinkqaq.totemdoll.client;

//? >= 1.21.4 {
/*import com.rethinkqaq.totemdoll.config.TotemDollConfig;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.IdentityHashMap;
import java.util.Map;

// Associates 1.21.4's resolved hand state with the TotemDoll style it came from.
public final class DollThirdPersonState {
    private static final Map<ItemStackRenderState, DollStyle> STYLES = new IdentityHashMap<>();

    public static synchronized void mark(ItemStackRenderState renderState, ItemStack stack,
                                          ItemDisplayContext context) {
        if (context != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                && context != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                && context != ItemDisplayContext.GROUND
                && context != ItemDisplayContext.FIXED) {
            STYLES.remove(renderState);
            return;
        }

        if (!stack.is(Items.TOTEM_OF_UNDYING)) {
            STYLES.remove(renderState);
            return;
        }

        DollStyle style = DollPreviewContext.current();
        if (style == null) style = TotemDollConfig.selectedStyle();
        if (style != null && DollBoneModels.contains(style.id())) {
            STYLES.put(renderState, style);
        } else {
            STYLES.remove(renderState);
        }
    }

    public static synchronized DollStyle get(ItemStackRenderState renderState) {
        return STYLES.get(renderState);
    }

    private DollThirdPersonState() {
    }
}
*///?} else {
public final class DollThirdPersonState {
    private DollThirdPersonState() {
    }
}
//?}
