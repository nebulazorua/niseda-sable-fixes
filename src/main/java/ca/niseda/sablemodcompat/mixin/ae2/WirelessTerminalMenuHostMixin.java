package ca.niseda.sablemodcompat.mixin.ae2;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.util.DimensionalBlockPos;
import appeng.helpers.WirelessTerminalMenuHost;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WirelessTerminalMenuHost.class)
public class WirelessTerminalMenuHostMixin {
    @WrapOperation(method = "getAccessPointSignal", at = @At(value = "INVOKE", target = "Lappeng/api/implementations/blockentities/IWirelessAccessPoint;getLocation()Lappeng/api/util/DimensionalBlockPos;"))
    private DimensionalBlockPos nisedasablecompat$sublevelAccessPOint(IWirelessAccessPoint instance, Operation<DimensionalBlockPos> original){
        DimensionalBlockPos dbp = original.call(instance);
        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(dbp.getLevel(), dbp.getPos());
        Vec3 pos = new Vec3(dbp.getPos().getX(), dbp.getPos().getY(), dbp.getPos().getZ());
        if (subLevelAccess != null)
            pos = subLevelAccess.logicalPose().transformPosition(pos);

        return new DimensionalBlockPos(dbp.getLevel(), new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z)));
    }
}