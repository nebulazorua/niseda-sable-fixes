package ca.niseda.sablemodcompat.mixin.fix_dupes;

import ca.niseda.sablemodcompat.NisedaSableModCompat;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.util.LevelAccelerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubLevelAssemblyHelper.class)
public class SubLevelAssemblyHelperMixin {
    // Ensures block entities are cleared properly
    @WrapOperation(method = "moveBlocks", at = @At(ordinal = 0, value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static BlockState nisedasablecompat$removeBlockEntity(LevelChunk instance, BlockPos pos, BlockState state, boolean isMoving, Operation<BlockState> original, @Local(name = "block") BlockPos block){
        if(instance.getLevel().getBlockEntity(block) != null)
            instance.getLevel().removeBlockEntity(block);

        return original.call(instance, pos, state, isMoving);
    }

    // Replaces blocks with something other than air, so brittle blocks don't detect being unsupported and drop themselves
    // A better solution would be detecting brittle blocks and clearing them FIRST rather than replacing everything
    // But unsure if there's a good way of doing that, Create has some hardcoded bullshit for it as-is so
    @Inject(method = "moveBlocks", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/platform/SableAssemblyPlatform;setIgnoreOnPlace(Lnet/minecraft/world/level/Level;Z)V",ordinal = 2))
    private static void nisedasablecompat$replaceBlocks(ServerLevel level, SubLevelAssemblyHelper.AssemblyTransform transform, Iterable<BlockPos> blocks, CallbackInfo ci){
        final LevelAccelerator accelerator = new LevelAccelerator(level);
        for (final BlockPos block : blocks) {
            try {
                final LevelChunk chunk = accelerator.getChunk(SectionPos.blockToSectionCoord(block.getX()),
                        SectionPos.blockToSectionCoord(block.getZ()));

                chunk.setBlockState(block, Blocks.DIRT.defaultBlockState(), true);
            } catch (final Exception e) {
                Sable.LOGGER.error("Failed to replace   old block during assembly {}", block, e);
            }
        }
    }

    // FInally, clears all blocks using the same method as Create
    @Redirect(method = "moveBlocks", at = @At(ordinal = 1, value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static BlockState nisedasablecompat$suppressDrops(LevelChunk instance, BlockPos pos, BlockState state, boolean isMoving){
        boolean success = instance.getLevel().setBlock(pos, state, Block.UPDATE_MOVE_BY_PISTON | Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_IMMEDIATE
                | Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        return success ? state : null;
    }
}
