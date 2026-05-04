package ca.niseda.sablemodcompat.mixin.yorihook;


import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.yori3o.yo_hooks.common.entity.HookEntity;
import foundry.veil.api.client.render.MatrixStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import org.joml.Quaterniondc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LevelRenderer.class, priority = 1500)
public class LevelRendererMixinMixin {
    @TargetHandler(
            mixin = "dev.ryanhcode.sable.mixin.entity.entity_rendering.LevelRendererMixin",
            name = "renderEntity"
    )
    @WrapOperation(method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lfoundry/veil/api/client/render/MatrixStack;rotateAround(Lorg/joml/Quaterniondc;DDD)V"))
    private void nisedasablecompat$noRotateHook(MatrixStack instance, Quaterniondc quaterniondc, double x, double y, double z, Operation<Void> original, @Local(argsOnly = true) Entity entity){
        if(!(entity instanceof HookEntity))
            original.call(instance, quaterniondc, x, y, z);
    }
}
