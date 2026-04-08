package es.superstrellaa.cinematictools.common.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ServerPacketHandler {

    public static void registerPackets() {
        PayloadTypeRegistry.playC2S().register(ModPackets.CHECK_OP_TYPE, ModPackets.CHECK_OP_CODEC);
        PayloadTypeRegistry.playS2C().register(ModPackets.CHECK_OP_TYPE, ModPackets.CHECK_OP_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ModPackets.CHECK_OP_TYPE, (payload, context) -> {
            boolean isOp = context.player().hasPermissions(4);
            context.responseSender().sendPacket(new ModPackets.CheckOpPayload(isOp));
        });
    }
}
