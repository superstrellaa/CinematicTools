package studio.dolphinproductions.utils.cinematictools.common.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import studio.dolphinproductions.utils.cinematictools.client.CinematicToolsClient;
import team.creative.creativecore.common.network.CreativePacket;

public class ConnectPacket extends CreativePacket {
    
    public ConnectPacket() {}
    
    @Override
    public void executeClient(Player player) {
        CinematicToolsClient.setServerAvailability();
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
}
