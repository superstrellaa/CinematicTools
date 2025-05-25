package studio.dolphinproductions.utils.cinematictools.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import studio.dolphinproductions.utils.cinematictools.client.CinematicToolsClient;

@Mixin(Camera.class)
public class CameraMixin {
    
    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/Camera;isDetached()Z", cancellable = true)
    public void isDetached(CallbackInfoReturnable<Boolean> info) {
        if (CinematicToolsClient.isPlaying() && CinematicToolsClient.getScene().mode.getCamera() != Minecraft.getInstance().player)
            info.setReturnValue(true);
    }
    
}
