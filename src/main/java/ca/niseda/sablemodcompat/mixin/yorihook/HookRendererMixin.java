package ca.niseda.sablemodcompat.mixin.yorihook;

import ca.niseda.sablemodcompat.NisedaSableModCompat;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yori3o.yo_hooks.common.client.render.HookRenderer;
import com.yori3o.yo_hooks.common.entity.HookEntity;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HookRenderer.class)

public abstract class HookRendererMixin {
    @WrapOperation(method = "render(Lcom/yori3o/yo_hooks/common/entity/HookEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lcom/yori3o/yo_hooks/common/entity/HookEntity;getPosition(F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 nisedasablecompat$fixRender(HookEntity instance, float v, Operation<Vec3> original, @Local(name = "handPos") Vec3 handPos){
        Vec3 position = original.call(instance, v);
        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(instance);
        if (subLevelAccess != null)
            position = subLevelAccess.logicalPose().transformPosition(position);

        return position;
    }

}
