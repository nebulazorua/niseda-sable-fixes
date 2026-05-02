package ca.niseda.sablemodcompat.physics;

import ca.niseda.sablemodcompat.mixin.oritech.NukeBlockAccessor;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.physics.callback.FragileBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import rearth.oritech.block.blocks.reactor.NukeBlock;
import rearth.oritech.init.BlockContent;
import rearth.oritech.init.OritechConfig;

public class ManhattanModuleCallback extends FragileBlockCallback {
    public static final ManhattanModuleCallback INSTANCE = new ManhattanModuleCallback();

    public ManhattanModuleCallback() {}

    @Override
    public boolean shouldTriggerFor(BlockState state) {
        return state.getBlock() instanceof NukeBlock;
    }

    @Override
    public double getTriggerVelocity() {
        return 10.0;
    }

    @Override
    public CollisionResult onHit(ServerLevel world, BlockPos pos, BlockState state, Vector3d hitPos) {
        Vec3 position = new Vec3(pos.getX(), pos.getY(), pos.getZ());
        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(world, pos);
        if(subLevelAccess != null) {
            Pose3dc pose = subLevelAccess.logicalPose();

            // Transform the position to global space
            position = pose.transformPosition(position);
            BlockPos nukePos = new BlockPos((int)Math.floor(position.x), (int)Math.floor(position.y), (int)Math.floor(position.z));

            if ((Boolean) OritechConfig.boringNukes.get()) {
                Vec3 center = nukePos.getCenter();
                world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                world.explode((Entity) null, center.x, center.y, center.z, 3.0F, true, Level.ExplosionInteraction.TNT);
                world.playSound((Player) null, position.x, position.y, position.z, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 1.0F, 1.0F);
                return new CollisionResult(JOMLConversion.ZERO, true);
            }

            Block target = ((NukeBlockAccessor) state.getBlock()).getSmall() ? BlockContent.REACTOR_EXPLOSION_MEDIUM : BlockContent.REACTOR_EXPLOSION_LARGE;
            world.setBlockAndUpdate(nukePos, target.defaultBlockState());
            world.destroyBlock(pos, false);
            world.playSound((Player) null, position.x, position.y, position.z, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            return new CollisionResult(JOMLConversion.ZERO, true);
        }

        return new CollisionResult(JOMLConversion.ZERO, false);

    }
}
