package ca.niseda.sablemodcompat.mixin.figura;


import ca.niseda.sablemodcompat.NisedaSableModCompat;
import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.Cancellable;

@Mixin(value = Camera.class, priority = 1500)
public abstract class CameraMixin {
    @Unique
    private Avatar avatar;

    @Inject(method = "setup", at = @At(value = "HEAD"))
    private void nisedasablecompat$setupAvatarVar(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        avatar = AvatarManager.getAvatar(focusedEntity);
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
        if (!RenderUtils.vanillaModelAndScript(avatar)) {
            avatar = null;
            original.call(instance, yRot, xRot, roll);
            return;
        }

        float x = xRot;
        float y = yRot;

        FiguraVec3 rot = avatar.luaRuntime.renderer.cameraRot;
        if (rot != null && rot.notNaN()) {
            x = (float) rot.x;
            y = (float) rot.y;
        }

        FiguraVec3 offset = avatar.luaRuntime.renderer.cameraOffsetRot;
        if (offset != null && offset.notNaN()) {
            x += (float) offset.x;
            y += (float) offset.y;
        }

        original.call(instance, y, x, roll);
    }

    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void nisedasablecompat$setupRotFixYX(Camera instance, float yRot, float xRot, Operation<Void> original) {
        if (!RenderUtils.vanillaModelAndScript(avatar)) {
            avatar = null;
            original.call(instance, yRot, xRot);
            return;
        }

        float x = xRot;
        float y = yRot;

        FiguraVec3 rot = avatar.luaRuntime.renderer.cameraRot;
        if (rot != null && rot.notNaN()) {
            x = (float) rot.x;
            y = (float) rot.y;
        }

        FiguraVec3 offset = avatar.luaRuntime.renderer.cameraOffsetRot;
        if (offset != null && offset.notNaN()) {
            x += (float) offset.x;
            y += (float) offset.y;
        }

        original.call(instance, y, x);
    }

}
