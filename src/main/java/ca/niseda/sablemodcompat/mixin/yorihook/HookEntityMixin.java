package ca.niseda.sablemodcompat.mixin.yorihook;

import ca.niseda.sablemodcompat.NisedaSableModCompat;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.yori3o.yo_hooks.common.entity.HookEntity;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HookEntity.class)
public abstract class HookEntityMixin extends Entity {
    public HookEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "onHitBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/BlockHitResult;getLocation()Lnet/minecraft/world/phys/Vec3;", ordinal = 1))
    private Vec3 nisedasableocmpat$fixLength(BlockHitResult instance, Operation<Vec3> original){
        Vec3 position = original.call(instance);
        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(this.level(), position);
        if (subLevelAccess != null)
            position = subLevelAccess.logicalPose().transformPosition(position);

        return position;
    }

    @WrapOperation(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;ILnet/minecraft/world/item/ItemStack;IZI)V", at = @At(value = "INVOKE", target = "Lcom/yori3o/yo_hooks/common/entity/HookEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void nisedasablecompat$inheritVelocity(HookEntity instance, Vec3 vec3, Operation<Void> original, @Local(argsOnly = true) LivingEntity owner){
        final SubLevel subLevel = Sable.HELPER.getTrackingSubLevel(owner);
        if (subLevel != null){
            final Vector3d subLevelGainedVelo = new Vector3d();
            final Vector3d currentLocalPos = subLevel.logicalPose().transformPositionInverse(new Vector3d(this.position().x, this.position().y, this.position().z));

            Sable.HELPER.getVelocity(owner.level(), subLevel, currentLocalPos, subLevelGainedVelo);
            subLevelGainedVelo.mul(1.0 / 20.0);
            Vec3 mojangVel = JOMLConversion.toMojang(subLevelGainedVelo);
            vec3 = vec3.add(mojangVel);
        }
        original.call(instance, vec3);
    }
}
