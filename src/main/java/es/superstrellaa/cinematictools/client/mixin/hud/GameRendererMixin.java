package es.superstrellaa.cinematictools.client.mixin.hud;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import es.superstrellaa.cinematictools.client.CinematicToolsClient;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (CinematicToolsClient.isPlaying()) {
            ((GameRenderer) (Object) this).setRenderHand(false);
        } else {
            ((GameRenderer) (Object) this).setRenderHand(true);
        }
    }
}