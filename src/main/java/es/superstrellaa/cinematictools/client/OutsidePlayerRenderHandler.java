package es.superstrellaa.cinematictools.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

public class OutsidePlayerRenderHandler {

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            if (CinematicToolsClient.isPlaying() &&
                    CinematicToolsClient.getScene().mode != null &&
                    CinematicToolsClient.getScene().mode.outside()) {

                EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
                PoseStack poseStack = context.matrixStack();
                MultiBufferSource buffer = context.consumers();

                for (Player player : mc.level.players()) {
                    if (player.isSpectator()) continue;

                    BlockPos pos = player.blockPosition();
                    int blockLight = mc.level.getBrightness(LightLayer.BLOCK, pos);
                    int skyLight = mc.level.getBrightness(LightLayer.SKY, pos);
                    int light = LightTexture.pack(blockLight, skyLight);

                    dispatcher.render(
                            player,
                            player.getX() - context.camera().getPosition().x,
                            player.getY() - context.camera().getPosition().y,
                            player.getZ() - context.camera().getPosition().z,
                            player.getYRot(),
                            context.tickDelta(),
                            poseStack,
                            buffer,
                            light
                    );
                }
            }
        });
    }
}