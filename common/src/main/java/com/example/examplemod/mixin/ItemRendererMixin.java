package com.example.examplemod.mixin;

import com.example.examplemod.config.TotemDollConfig;
import com.example.examplemod.client.DollPreviewContext;
import com.example.examplemod.doll.DollStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

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

        BakedModel model = Minecraft.getInstance()
                .getModelManager()
                .getModel(ModelResourceLocation.inventory(style.model()));
        if (model == Minecraft.getInstance().getModelManager().getMissingModel()) {
            model = Minecraft.getInstance()
                    .getModelManager()
                    .getModel(new ModelResourceLocation(style.model(), "standalone"));
        }
        if (model == Minecraft.getInstance().getModelManager().getMissingModel()) {
            model = Minecraft.getInstance()
                    .getModelManager()
                    .getModel(new ModelResourceLocation(style.model(), "fabric_resource"));
        }
        if (model != Minecraft.getInstance().getModelManager().getMissingModel()) {
            callback.setReturnValue(model);
        }
    }
}
