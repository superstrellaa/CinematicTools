package es.superstrellaa.cinematictools.common.packet;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import es.superstrellaa.cinematictools.CinematicTools;
import es.superstrellaa.cinematictools.common.scene.CamScene;
import es.superstrellaa.cinematictools.server.CinematicToolsServer;
import team.creative.creativecore.common.network.CreativePacket;

public class GetPathPacket extends CreativePacket {
    
    public String id;
    
    public GetPathPacket() {}
    
    public GetPathPacket(String id) {
        this.id = id;
    }
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {
        CamScene path = CinematicToolsServer.get(player.level(), id);
        if (path != null)
            CinematicTools.NETWORK.sendToClient(new SetPathPacket(id, path), player);
        else
            player.sendSystemMessage(Component.translatable("scene.load_fail", id));
    }
    
}
