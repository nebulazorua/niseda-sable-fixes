package ca.niseda.sablemodcompat.mixin.yorihook;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.yori3o.yo_hooks.common.entity.HookEntity;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Player.class, priority = 1500)
public class PlayerMixinMixin {
    @TargetHandler(
            mixin = "com.yori3o.yo_hooks.common.mixin.PlayerMixin",
            name = "onTravel"
    )
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lcom/yori3o/yo_hooks/common/entity/HookEntity;position()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 nisedasablecompat$getWorldPOsition(HookEntity instance, Operation<Vec3> original){
        Vec3 position = original.call(instance);
        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(instance);
        if (subLevelAccess != null)
            position = subLevelAccess.logicalPose().transformPosition(position);

        return position;
    }

}
