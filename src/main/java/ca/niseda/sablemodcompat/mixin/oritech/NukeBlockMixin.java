package ca.niseda.sablemodcompat.mixin.oritech;


import ca.niseda.sablemodcompat.physics.ManhattanModuleCallback;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.api.block.BlockWithSubLevelCollisionCallback;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rearth.oritech.block.blocks.reactor.NukeBlock;

@Mixin(NukeBlock.class)
public abstract class NukeBlockMixin implements BlockWithSubLevelCollisionCallback {
    @Override
    public BlockSubLevelCollisionCallback sable$getCallback() {
        return ManhattanModuleCallback.INSTANCE;
    }

    // Moves the nuclear explosion block out of the sub world so the explosion properly happens
    @WrapOperation(method = "primeTnt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean nisedasablecompat$moveToWorld(Level instance, BlockPos pos, BlockState state, Operation<Boolean> original){
        Vector3d projectedPos = SableCompanion.INSTANCE.projectOutOfSubLevel(instance, new Vector3d(pos.getX(), pos.getY(), pos.getZ()));
        BlockPos projected = BlockPos.containing(projectedPos.x, projectedPos.y, projectedPos.z);

        if(!projected.equals(pos))
            original.call(instance, pos, Blocks.AIR.defaultBlockState()); // If the nuke was in a sublevel, ensure we break the nuke.

        return original.call(instance, projected, state);
    }
}
