package studio.dolphinproductions.utils.cinematictools.client.mixin.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.dolphinproductions.utils.cinematictools.client.CinematicToolsClient;

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