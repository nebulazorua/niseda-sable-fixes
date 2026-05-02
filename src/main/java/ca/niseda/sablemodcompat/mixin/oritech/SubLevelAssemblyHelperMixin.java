package ca.niseda.sablemodcompat.mixin.oritech;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.util.LevelAccelerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rearth.oritech.block.base.block.MultiblockMachine;
import rearth.oritech.block.blocks.processing.MachineCoreBlock;
import rearth.oritech.block.entity.MachineCoreEntity;
import rearth.oritech.util.MultiblockMachineController;

import java.util.ArrayList;

@Mixin(SubLevelAssemblyHelper.class)
public class SubLevelAssemblyHelperMixin {
    @Inject(method = "moveBlocks", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Ldev/ryanhcode/sable/platform/SableAssemblyPlatform;setIgnoreOnPlace(Lnet/minecraft/world/level/Level;Z)V"))
    private static void nisedasablecompat$properlyMoveMultiblockControllers(ServerLevel level, SubLevelAssemblyHelper.AssemblyTransform transform, Iterable<BlockPos> blocks, CallbackInfo ci, @Local(ordinal=1) ServerLevel resultingLevel){
        final LevelAccelerator accelerator = new LevelAccelerator(level);
        final LevelAccelerator resultingAccelerator = new LevelAccelerator(resultingLevel);
        for(final BlockPos block : blocks){
            final BlockEntity entity = accelerator.getBlockEntity(block);
            final BlockPos newPos = transform.apply(block);
            if(entity instanceof MultiblockMachineController controller){
                BlockPos positionDiff = newPos.subtract(block);
                ArrayList<BlockPos> coreBlocksConnected = controller.getConnectedCores();
                ArrayList<BlockPos> oldCoreBlocksConnected = new ArrayList<BlockPos>(coreBlocksConnected);

                coreBlocksConnected.clear();
                for(BlockPos pos : oldCoreBlocksConnected){
                    coreBlocksConnected.add(pos.offset(positionDiff));
                }
            }
        }
    }

    @Inject(method = "moveBlocks", at = @At(value = "TAIL"))
    private static void nisedasablecompat$deconstructBrokenMultiblocks(ServerLevel level, SubLevelAssemblyHelper.AssemblyTransform transform, Iterable<BlockPos> blocks, CallbackInfo ci, @Local(ordinal=1) ServerLevel resultingLevel){
        final LevelAccelerator accelerator = new LevelAccelerator(level);
        final LevelAccelerator resultingAccelerator = new LevelAccelerator(resultingLevel);
        for(BlockPos untransformed : blocks){
            final BlockPos transformed = transform.apply(untransformed);
            BlockEntity entity = resultingAccelerator.getBlockEntity(transformed);
            if(entity instanceof MachineCoreEntity machineCore){
                if(!(resultingAccelerator.getBlockEntity(machineCore.getControllerPos()) instanceof MultiblockMachineController)){
                    BlockPos positionDiff = transformed.subtract(untransformed);
                    BlockPos controllerPos = machineCore.getControllerPos().offset(-positionDiff.getX(), -positionDiff.getY(), -positionDiff.getZ());
                    if(accelerator.getBlockEntity(controllerPos) instanceof MultiblockMachineController controller){
                        controller.onCoreBroken(untransformed);
                    }
                    BlockState state = resultingAccelerator.getBlockState(transformed);
                    if (state.getBlock() instanceof MachineCoreBlock) {
                        resultingLevel.setBlockAndUpdate(transformed, state.setValue(MachineCoreBlock.USED, false));
                    }
                }
            }else if(entity instanceof MultiblockMachineController controller){
                boolean hasCores = true;
                ArrayList<BlockPos> coreBlocksConnected = controller.getConnectedCores();
                for(BlockPos pos : coreBlocksConnected){
                    if(!(resultingAccelerator.getBlockEntity(pos) instanceof MachineCoreEntity)){
                        hasCores = false;
                        break;
                    }
                }

                if(!hasCores){
                    BlockPos positionDiff = transformed.subtract(untransformed);
                    for(BlockPos pos : coreBlocksConnected){
                        final BlockPos originalCorePos = pos.offset(-positionDiff.getX(), -positionDiff.getY(), -positionDiff.getZ());
                        BlockState state = accelerator.getBlockState(originalCorePos);
                        if(state.getBlock() instanceof MachineCoreBlock){
                            level.setBlockAndUpdate(originalCorePos, state.setValue(MachineCoreBlock.USED, false));
                        }
                    }

                    resultingLevel.setBlockAndUpdate(transformed, resultingAccelerator.getBlockState(transformed).setValue(MultiblockMachine.ASSEMBLED, false));


                    coreBlocksConnected.clear();
                }
            }
        }
    }
}
