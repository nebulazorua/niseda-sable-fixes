package ca.niseda.sablemodcompat.mixin.fluid;

import com.adonis.fluid.block.CopperTap.CopperTapBlockEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.ryanhcode.sable.ActiveSableCompanion;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CopperTapBlockEntity.class)
public abstract class CopperTapMixin extends SmartBlockEntity {
    public CopperTapMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @WrapOperation(method = "tryProcess", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    public BlockEntity nisedasablecompat$tryProcessSubLevelBlockEntities(Level level, BlockPos pos, Operation<BlockEntity> original){
        return nisedasablecompat$getBlockEntity(level, pos, original);
    }

    @WrapOperation(method = "tryProcess", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public BlockState nisedasablecompat$tryProcessSubLevelBlockStates(Level level, BlockPos pos, Operation<BlockState> original){
        return nisedasablecompat$getBlockState(level, pos, original);
    }

    @WrapOperation(method = "tryTransferFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    public BlockEntity nisedasablecompat$tryTransferSubLevelBlockEntities(Level level, BlockPos pos, Operation<BlockEntity> original){
        return nisedasablecompat$getBlockEntity(level, pos, original);
    }

    @WrapOperation(method = "tryTransferFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public BlockState nisedasablecompat$tryTransferSubLevelBlockStates(Level level, BlockPos pos, Operation<BlockState> original){
        return nisedasablecompat$getBlockState(level, pos, original);
    }

    @WrapOperation(method = "finishItemFilling", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    public BlockEntity nisedasablecompat$finishItemFillSubLevelBlockEntities(Level level, BlockPos pos, Operation<BlockEntity> original){
        return nisedasablecompat$getBlockEntity(level, pos, original);
    }

    @WrapOperation(method = "finishItemFilling", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public BlockState nisedasablecompat$finishItemFillSubLevelBlockStates(Level level, BlockPos pos, Operation<BlockState> original){
        return nisedasablecompat$getBlockState(level, pos, original);
    }

    @WrapOperation(method = "validateItemStillPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    public BlockEntity nisedasablecompat$itemValidateSubLevelBlockEntities(Level level, BlockPos pos, Operation<BlockEntity> original){
        return nisedasablecompat$getBlockEntity(level, pos, original);
    }

    @WrapOperation(method = "tryProcess", at = @At(value = "INVOKE", target = "Lcom/adonis/fluid/block/CopperTap/CopperTapBlockEntity;tryFillCauldron(Lnet/neoforged/neoforge/fluids/capability/IFluidHandler;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    public boolean nisedasablecompat$tryFillCauldronSubLevels(CopperTapBlockEntity instance, IFluidHandler iFluidHandler, BlockPos targetPos, BlockState targetState, Operation<Boolean> original){
        // There's prob a better way of doing this
        final ActiveSableCompanion helper = Sable.HELPER;
        final BlockPos actualPos = helper.runIncludingSubLevels(instance.getLevel(), targetPos.getCenter(), true, helper.getContaining(instance.getLevel(), targetPos), (subLevel, internalPos) -> {
            final BlockState state = instance.getLevel().getBlockState(internalPos);
            if(state == targetState)
                return internalPos;

            return null;
        });
        if(actualPos != null)
            targetPos = actualPos;

        return original.call(instance, iFluidHandler, targetPos, targetState);
    }

    @WrapOperation(method = "sendFillingParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;atCenterOf(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 nisedasablecompat$fixParticles(Vec3i toCopy, Operation<Vec3> original){
        Vec3 pos = original.call(toCopy);
        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, pos);

        if (subLevelAccess != null) {
            Pose3dc pose = subLevelAccess.logicalPose();
            pos = pose.transformPosition(pos);
        }

        return pos;
    }

    @Unique
    private BlockEntity nisedasablecompat$getBlockEntity(Level level, BlockPos pos, Operation<BlockEntity> original){
        final ActiveSableCompanion helper = Sable.HELPER;
        final BlockEntity caught = helper.runIncludingSubLevels(level, pos.getCenter(), true, helper.getContaining(level, pos), (subLevel, internalPos) -> original.call(level, internalPos));
        if(caught != null)
            return caught;

        return original.call(level, pos);
    }

    @Unique
    private BlockState nisedasablecompat$getBlockState(Level level, BlockPos pos, Operation<BlockState> original){
        final ActiveSableCompanion helper = Sable.HELPER;
        final BlockState caught = helper.runIncludingSubLevels(level, pos.getCenter(), true, helper.getContaining(level, pos), (subLevel, internalPos) -> {
            final BlockState state = original.call(level, internalPos);
            if(!state.isEmpty())
                return state;

            return null;
        });

        if(caught != null)
            return caught;

        return original.call(level, pos);
    }

}
