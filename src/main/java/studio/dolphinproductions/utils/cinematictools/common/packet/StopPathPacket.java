package studio.dolphinproductions.utils.cinematictools.common.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import studio.dolphinproductions.utils.cinematictools.client.CinematicToolsClient;
import team.creative.creativecore.common.network.CreativePacket;

public class StopPathPacket extends CreativePacket {
    
    public StopPathPacket() {}
    
    @Override
    public void executeClient(Player player) {
        if (CinematicToolsClient.isPlaying() && CinematicToolsClient.getScene().serverSynced())
            CinematicToolsClient.stopServer();
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
