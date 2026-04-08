package es.superstrellaa.cinematictools.client.mixin.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import es.superstrellaa.cinematictools.client.CinematicToolsClient;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void onRenderEffects(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (CinematicToolsClient.isPlaying()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void onRenderPlayerHealth(GuiGraphics graphics, CallbackInfo ci) {
        if (CinematicToolsClient.isPlaying()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void onRenderSelectedItemName(GuiGraphics graphics, CallbackInfo ci) {
        if (CinematicToolsClient.isPlaying()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void onRenderExperienceBar(GuiGraphics graphics, int x, CallbackInfo ci) {
        if (CinematicToolsClient.isPlaying()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    private void onRenderHotbar(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (CinematicToolsClient.isPlaying()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
    private void onRenderVehicleHealth(GuiGraphics graphics, CallbackInfo ci) {
        if (CinematicToolsClient.isPlaying()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (CinematicToolsClient.isPlaying()) {
            ci.cancel();
        }
    }
}
