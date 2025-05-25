package studio.dolphinproductions.utils.cinematictools.server;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import studio.dolphinproductions.utils.cinematictools.CinematicTools;
import studio.dolphinproductions.utils.cinematictools.common.packet.ConnectPacket;

public class CamEventHandler {
    public CamEventHandler() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            onPlayerConnect(handler.getPlayer());
        });
    }

    public void onPlayerConnect(ServerPlayer player) {
        CinematicTools.NETWORK.sendToClient(new ConnectPacket(), player);
    }
    
}
