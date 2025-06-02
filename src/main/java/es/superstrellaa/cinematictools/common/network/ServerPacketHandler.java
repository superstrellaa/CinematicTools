package es.superstrellaa.cinematictools.common.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

public class ServerPacketHandler {

    public static void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.CHECK_OP_PACKET, (server, player, handler, buf, responseSender) -> {
            boolean isOp = player.hasPermissions(4);

            FriendlyByteBuf response = new FriendlyByteBuf(Unpooled.buffer());
            response.writeBoolean(isOp);
            ServerPlayNetworking.send(player, ModPackets.CHECK_OP_PACKET, response);
        });
    }
}