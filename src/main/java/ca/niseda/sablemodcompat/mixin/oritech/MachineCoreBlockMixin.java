package ca.niseda.sablemodcompat.mixin.oritech;

import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import rearth.oritech.block.blocks.processing.MachineCoreBlock;
import rearth.oritech.block.entity.MachineCoreEntity;

@Mixin(MachineCoreBlock.class)
public abstract class MachineCoreBlockMixin implements BlockSubLevelAssemblyListener {
    @Override
    public void beforeMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState newState, BlockPos oldPos, BlockPos newPos) {
        MachineCoreEntity coreEntity = (MachineCoreEntity)originLevel.getBlockEntity(oldPos);
        if(coreEntity != null){
            BlockPos positionDiff = newPos.subtract(oldPos);
            coreEntity.setControllerPos(coreEntity.getControllerPos().offset(positionDiff.getX(), positionDiff.getY(), positionDiff.getZ()));
        }
    }

    @Override
    public void afterMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState newState, BlockPos oldPos, BlockPos newPos) {

    }
}
