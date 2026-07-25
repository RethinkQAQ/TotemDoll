package com.example.examplemod.mixin;

import com.example.examplemod.config.TotemDollConfig;
import com.example.examplemod.client.DollPreviewContext;
import com.example.examplemod.doll.DollStyle;
import com.example.examplemod.doll.DollAnimationManager;
import com.example.examplemod.doll.DollAnimationModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
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

        if (style.hasAnimations()) {
            var animation = style.animations().get(0);
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
        BakedModel model = manager.getModel(ModelResourceLocation.inventory(modelId));
        if (model == Minecraft.getInstance().getModelManager().getMissingModel()) {
            model = manager.getModel(new ModelResourceLocation(modelId, "standalone"));
        }
        if (model == Minecraft.getInstance().getModelManager().getMissingModel()) {
            model = manager.getModel(new ModelResourceLocation(modelId, "fabric_resource"));
        }
        return model;
    }
}
