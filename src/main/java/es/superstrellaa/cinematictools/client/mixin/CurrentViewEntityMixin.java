package es.superstrellaa.cinematictools.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.player.LocalPlayer;
import es.superstrellaa.cinematictools.client.CinematicToolsClient;

@Mixin(LocalPlayer.class)
public abstract class CurrentViewEntityMixin {
    
    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/player/LocalPlayer;isControlledCamera()Z", cancellable = true)
    public void isControlledCamera(CallbackInfoReturnable<Boolean> callback) {
        if (CinematicToolsClient.isPlaying())
            callback.setReturnValue(true);
    }
    
}
