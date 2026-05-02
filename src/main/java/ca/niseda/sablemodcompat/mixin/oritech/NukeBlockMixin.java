package ca.niseda.sablemodcompat.mixin.oritech;


import ca.niseda.sablemodcompat.physics.ManhattanModuleCallback;
import dev.ryanhcode.sable.api.block.BlockWithSubLevelCollisionCallback;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import org.spongepowered.asm.mixin.Mixin;
import rearth.oritech.block.blocks.reactor.NukeBlock;

@Mixin(NukeBlock.class)
public abstract class NukeBlockMixin implements BlockWithSubLevelCollisionCallback {
    @Override
    public BlockSubLevelCollisionCallback sable$getCallback() {
        return ManhattanModuleCallback.INSTANCE;
    }
}
