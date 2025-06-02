package es.superstrellaa.cinematictools.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.vertex.PoseStack;

public class OutsidePlayerRenderHandler {

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            if (CinematicToolsClient.isPlaying() && CinematicToolsClient.getScene().mode != null && CinematicToolsClient.getScene().mode.outside()) {
                Player player = mc.player;
                EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
                PoseStack poseStack = context.matrixStack();
                MultiBufferSource buffer = context.consumers();

                
                dispatcher.render(
                    player,
                    player.getX() - context.camera().getPosition().x,
                    player.getY() - context.camera().getPosition().y,
                    player.getZ() - context.camera().getPosition().z,
                    player.getYRot(),
                    context.tickDelta(),
                    poseStack,
                    buffer,
                    15728880
                );
            }
        });
    }
}