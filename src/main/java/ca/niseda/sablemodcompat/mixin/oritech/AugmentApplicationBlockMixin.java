package ca.niseda.sablemodcompat.mixin.oritech;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.entity.EntitySubLevelUtil;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rearth.oritech.block.blocks.augmenter.AugmentApplicationBlock;

@Mixin(AugmentApplicationBlock.class)
public abstract class AugmentApplicationBlockMixin extends HorizontalDirectionalBlock implements EntityBlock {
    protected AugmentApplicationBlockMixin(Properties properties) {
        super(properties);
    }

    // getDeltaMovement doesnt work on sublevels for some reason?? so just doing this instead
    // Bit hacky but oh well
    @WrapOperation(method = "lockPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;"))
    public Vec3 nisedasablecompat$fixLockPlayer(Player instance, Operation<Vec3> original, @Local(argsOnly = true) Player player){
        SubLevel subLevel = Sable.HELPER.getTrackingSubLevel(player);
        if(subLevel != null)
            return player.getPosition(1).subtract(player.getPosition(0));

        return original.call(instance);
    }

    @WrapOperation(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;getBottomCenter()Lnet/minecraft/world/phys/Vec3;"))
    public Vec3 nisedasablecompat$projectFromSubworld(BlockPos instance, Operation<Vec3> original, @Local(argsOnly = true) Level level){
        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, instance);

        if (subLevelAccess != null) {
            Pose3dc pose = subLevelAccess.logicalPose();
            Vec3 projected = pose.transformPosition(original.call(instance));
            return projected;
        }

        return original.call(instance);
    }
}
