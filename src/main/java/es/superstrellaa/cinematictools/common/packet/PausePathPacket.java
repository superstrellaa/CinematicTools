package es.superstrellaa.cinematictools.common.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import es.superstrellaa.cinematictools.client.CinematicToolsClient;
import team.creative.creativecore.common.network.CreativePacket;

public class PausePathPacket extends CreativePacket {
    
    public PausePathPacket() {}
    
    @Override
    public void executeClient(Player player) {
        if (CinematicToolsClient.isPlaying() && CinematicToolsClient.getScene().serverSynced())
            CinematicToolsClient.getScene().pause();
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
