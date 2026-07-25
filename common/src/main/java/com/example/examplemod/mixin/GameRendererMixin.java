package com.example.examplemod.mixin;

import com.example.examplemod.config.TotemDollConfig;
import com.example.examplemod.doll.DollAnimationManager;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "displayItemActivation", at = @At("HEAD"))
    private void totemdoll$triggerTotemAnimation(ItemStack stack, CallbackInfo callback) {
        if (stack.is(Items.TOTEM_OF_UNDYING)) {
            DollAnimationManager.trigger(TotemDollConfig.selectedStyle(), "on_totem_activate");
        }
    }
}
