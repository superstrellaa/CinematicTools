package es.superstrellaa.cinematictools.common.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import es.superstrellaa.cinematictools.client.CinematicToolsClient;
import es.superstrellaa.cinematictools.common.math.point.CamPoint;
import team.creative.creativecore.common.network.CreativePacket;

public class TeleportPathPacket extends CreativePacket {
    
    public CompoundTag nbt;
    
    public TeleportPathPacket() {}
    
    public TeleportPathPacket(CamPoint point) {
        this.nbt = point.save(new CompoundTag());
    }
    
    @Override
    public void executeClient(Player player) {
        CinematicToolsClient.teleportTo(new CamPoint(nbt));
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
}
