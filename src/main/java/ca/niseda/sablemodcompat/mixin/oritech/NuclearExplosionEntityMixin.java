package ca.niseda.sablemodcompat.mixin.oritech;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import rearth.oritech.OritechPlatform;
import rearth.oritech.block.blocks.reactor.NuclearExplosionBlock;
import rearth.oritech.block.entity.reactor.NuclearExplosionEntity;

import java.util.HashSet;
import java.util.Set;

@Mixin(NuclearExplosionEntity.class)
public abstract class NuclearExplosionEntityMixin extends BlockEntity {

    @Shadow
    @Final
    private Set<BlockPos> removedBlocks;

    @Shadow
    @Final
    private Set<BlockPos> borderBlocks;

    @Shadow
    private Player getNukePlayerEntity() {
        return null;
    }

    public NuclearExplosionEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    // Incase the explosion
    @ModifyArgs(method = "tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lrearth/oritech/block/entity/reactor/NuclearExplosionEntity;)V", at = @At(value = "INVOKE", target = "Lrearth/oritech/block/entity/reactor/NuclearExplosionEntity;explosionSphere(IILnet/minecraft/core/BlockPos;)I"))
    private void nisedasablecompat$projectPosition(Args args, @Local(argsOnly = true) BlockPos bPos){
        final Vector3d projectedPos = Sable.HELPER.projectOutOfSubLevel(this.level, new Vector3d(bPos.getX(), bPos.getY(), bPos.getZ()));
        args.set(2, new BlockPos(Mth.floor(projectedPos.x), Mth.floor(projectedPos.y), Mth.floor(projectedPos.z)));
    }

    @WrapOperation(method = "lambda$processBorderBlocks$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;distSqr(Lnet/minecraft/core/Vec3i;)D"))
    private double nisedasablecompat$projectedDistance(BlockPos instance, Vec3i vec3i, Operation<Double> original){
        Vector3d projectedPos = SableCompanion.INSTANCE.projectOutOfSubLevel(this.level, new Vector3d(instance.getX(), instance.getY(), instance.getZ()));
        BlockPos projected = BlockPos.containing(projectedPos.x, projectedPos.y, projectedPos.z);

        return original.call(projected, vec3i);
    }

    @Inject(method = "explosionSphere", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z", shift = At.Shift.BEFORE))
    private void nisedasablecompat$explodeShips(int radius, int power, BlockPos pos, CallbackInfoReturnable<Integer> cir, @Local(name = "target") BlockPos target, @Local(name = "radiusSq") int radiusSq, @Local(name = "radiusSqExtra") int radiusSqExtra, @Local(name = "usedPower") LocalIntRef usedPower, @Local(name = "hardBusters") LocalIntRef hardBusters){
        final BoundingBox3d globalBounds = new BoundingBox3d(target);
        final Iterable<SubLevel> subLevels = Sable.HELPER.getAllIntersecting(this.level, globalBounds);
        final SubLevelContainer container = SubLevelContainer.getContainer(this.level);

        for(final SubLevel subLevel : subLevels) {
            Pose3d pose = subLevel.logicalPose();

            final BoundingBox3d localBounds = new BoundingBox3d();
            globalBounds.transformInverse(pose, localBounds);

            final BoundingBox3i blockBounds = new BoundingBox3i(
                Mth.floor(localBounds.minX() + 0.5),
                Mth.floor(localBounds.minY() + 0.5),
                Mth.floor(localBounds.minZ() + 0.5),
                Mth.floor(localBounds.maxX() - 0.5),
                Mth.floor(localBounds.maxY() - 0.5),
                Mth.floor(localBounds.maxZ() - 0.5)
            );
            final Vec3 localNukePos = pose.transformPositionInverse(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
            final BlockPos nukeBlock = BlockPos.containing(localNukePos.x, localNukePos.y, localNukePos.z);

            Set<Vec3> positionsToCheck = new HashSet<>();

            for (int x = blockBounds.minX(); x <= blockBounds.maxX(); x++) {
                for (int z = blockBounds.minZ(); z <= blockBounds.maxZ(); z++) {
                    for (int y = blockBounds.minY(); y <= blockBounds.maxY(); y++) {
                        final Vec3 localPosition = new Vec3(x, y, z);
                        positionsToCheck.add(localPosition);
                    }
                }
            }

            for (Vec3 localPosition : positionsToCheck) {
                final BlockPos newTarget = BlockPos.containing(localPosition);

                if (subLevel instanceof final ServerSubLevel serverSubLevel) {
                    final SubLevelPhysicsSystem physicsSystem = ((ServerSubLevelContainer) container).physicsSystem();
                    final RigidBodyHandle handle = physicsSystem.getPhysicsHandle(serverSubLevel);

                    final Vec3 center = newTarget.getCenter();
                    final Vec3 force = center.subtract(localNukePos).normalize().scale(power * 0.075f);
                    handle.applyImpulseAtPoint(center, force);
                }

                if(this.removedBlocks.contains(newTarget)) continue;

                double distSq = newTarget.distSqr(nukeBlock);
                if (distSq > (double) radiusSq) {
                    if (distSq <= (double) radiusSqExtra) {
                        this.borderBlocks.add(newTarget.immutable());
                    }
                    continue;
                }

                float removalPercentage = ((float)distSq - (float)radiusSq / 2.0F) / (float)radiusSq;
                removalPercentage -= 0.2f;

                float percentage = this.level.random.nextFloat();
                if (percentage < removalPercentage) {
                    this.borderBlocks.add(newTarget.immutable());
                    continue;
                }

                BlockState targetState = this.level.getBlockState(newTarget);
                Block targetBlock = targetState.getBlock();
                float targetHardness = targetBlock.getExplosionResistance();

                if (targetBlock instanceof NuclearExplosionBlock || targetState.isAir() || targetState.getDestroySpeed(level, newTarget) < 0.0)
                    continue;

                int busters = hardBusters.get();

                if (targetHardness > power && busters-- < 0) {
                    hardBusters.set(busters);
                    continue;
                }

                usedPower.set(usedPower.get() + Mth.floor(targetHardness));
                if (!OritechPlatform.INSTANCE.canPlayerBreakBlock(this.level, target, targetState, this.getNukePlayerEntity())) {
                    cir.setReturnValue(1000);
                    return;
                }

                targetBlock.destroy(this.level, pos, targetState);
                this.level.setBlock(newTarget, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE, 0);
                this.removedBlocks.add(newTarget.immutable());
                this.borderBlocks.remove(newTarget.immutable());
            }
        }
    }
}
