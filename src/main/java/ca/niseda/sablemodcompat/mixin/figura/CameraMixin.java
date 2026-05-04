package ca.niseda.sablemodcompat.mixin.figura;


import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Camera.class, priority = 1500)
public abstract class CameraMixin {
    @Unique
    private Avatar nisedasablecompat$avatar;

    @Inject(method = "setup", at = @At(value = "HEAD"))
    private void nisedasablecompat$setupAvatarVar(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        nisedasablecompat$avatar = AvatarManager.getAvatar(focusedEntity);
    }

    @TargetHandler(
            mixin = "dev.ryanhcode.sable.mixin.entity.entity_sublevel_collision.CameraMixin",
            name = "sable$setPosition"
    )
    @ModifyArg(
            method = "@MixinSquared:Handler",
            at = @At(value = "INVOKE", target = "Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;call([Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object[] nisedasablecompat$fixPosition(Object[] args){
        if (!RenderUtils.vanillaModelAndScript(nisedasablecompat$avatar)) {
            return args;
        }

        double x = (double) args[0];
        double y = (double) args[1];
        double z = (double) args[2];

        FiguraVec3 piv = nisedasablecompat$avatar.luaRuntime.renderer.cameraPivot;
        if (piv != null && piv.notNaN()) {
            x = piv.x;
            y = piv.y;
            z = piv.z;
        }

        FiguraVec3 offset = nisedasablecompat$avatar.luaRuntime.renderer.cameraOffsetPivot;
        if (offset != null && offset.notNaN()) {
            x += offset.x;
            y += offset.y;
            z += offset.z;
        }

        args[0] = x;
        args[1] = y;
        args[2] = z;

        return args;

    }


    @TargetHandler(
            mixin = "org.figuramc.figura.mixin.render.CameraMixin",
            name = "setupRot"
    )
    @Inject(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void nisedasablecompat$killFiguraSetup(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo oci, CallbackInfo ci) {
        ci.cancel();
    }

    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FFF)V"))
    private void nisedasablecompat$setupRotFixYXR(Camera instance, float yRot, float xRot, float roll, Operation<Void> original) {
        if (!RenderUtils.vanillaModelAndScript(nisedasablecompat$avatar)) {
            nisedasablecompat$avatar = null;
            original.call(instance, yRot, xRot, roll);
            return;
        }

        float x = xRot;
        float y = yRot;

        FiguraVec3 rot = nisedasablecompat$avatar.luaRuntime.renderer.cameraRot;
        if (rot != null && rot.notNaN()) {
            x = (float) rot.x;
            y = (float) rot.y;
        }

        FiguraVec3 offset = nisedasablecompat$avatar.luaRuntime.renderer.cameraOffsetRot;
        if (offset != null && offset.notNaN()) {
            x += (float) offset.x;
            y += (float) offset.y;
        }

        original.call(instance, y, x, roll);
    }

    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void nisedasablecompat$setupRotFixYX(Camera instance, float yRot, float xRot, Operation<Void> original) {
        if (!RenderUtils.vanillaModelAndScript(nisedasablecompat$avatar)) {
            nisedasablecompat$avatar = null;
            original.call(instance, yRot, xRot);
            return;
        }

        float x = xRot;
        float y = yRot;

        FiguraVec3 rot = nisedasablecompat$avatar.luaRuntime.renderer.cameraRot;
        if (rot != null && rot.notNaN()) {
            x = (float) rot.x;
            y = (float) rot.y;
        }

        FiguraVec3 offset = nisedasablecompat$avatar.luaRuntime.renderer.cameraOffsetRot;
        if (offset != null && offset.notNaN()) {
            x += (float) offset.x;
            y += (float) offset.y;
        }

        original.call(instance, y, x);
    }

}
