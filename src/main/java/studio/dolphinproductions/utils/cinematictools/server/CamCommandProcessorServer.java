package studio.dolphinproductions.utils.cinematictools.server;

import java.util.Collection;
import java.util.Collections;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import studio.dolphinproductions.utils.cinematictools.CinematicTools;
import studio.dolphinproductions.utils.cinematictools.client.SceneException;
import studio.dolphinproductions.utils.cinematictools.common.command.CamCommandProcessor;
import studio.dolphinproductions.utils.cinematictools.common.math.point.CamPoint;
import studio.dolphinproductions.utils.cinematictools.common.packet.StartPathPacket;
import studio.dolphinproductions.utils.cinematictools.common.packet.TeleportPathPacket;
import studio.dolphinproductions.utils.cinematictools.common.scene.CamScene;
import team.creative.creativecore.common.network.CreativePacket;

public class CamCommandProcessorServer implements CamCommandProcessor {
    
    @Override
    public CamScene getScene(CommandContext<CommandSourceStack> context) {
        String name = StringArgumentType.getString(context, "name");
        CamScene scene = CinematicToolsServer.get(context.getSource().getLevel(), name);
        return scene;
    }
    
    @Override
    public boolean canSelectTarget() {
        return false;
    }
    
    @Override
    public void selectTarget(CommandContext<CommandSourceStack> context, boolean look) {}
    
    @Override
    public boolean canCreatePoint(CommandContext<CommandSourceStack> context) {
        return context.getSource().getEntity() != null;
    }
    
    @Override
    public CamPoint createPoint(CommandContext<CommandSourceStack> context) {
        return CamPoint.create(context.getSource().getEntity());
    }
    
    @Override
    public boolean requiresSceneName() {
        return true;
    }
    
    @Override
    public boolean requiresPlayer() {
        return true;
    }
    
    public Collection<ServerPlayer> getPlayers(CommandContext<CommandSourceStack> context) {
        try {
            return EntityArgument.getPlayers(context, "players");
        } catch (CommandSyntaxException e) {
            return Collections.EMPTY_LIST;
        }
    }
    
    @Override
    public void start(CommandContext<CommandSourceStack> context) throws SceneException {
        CamScene scene = getScene(context);
        if (scene.points.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("scene.create_fail"));
            return;
        }
        CreativePacket packet = new StartPathPacket(scene);
        for (ServerPlayer player : getPlayers(context))
            CinematicTools.NETWORK.sendToClient(packet, player);
    }
    
    @Override
    public void teleport(CommandContext<CommandSourceStack> context, int index) {
        CreativePacket packet = new TeleportPathPacket(getScene(context).points.get(index));
        for (ServerPlayer player : getPlayers(context))
            CinematicTools.NETWORK.sendToClient(packet, player);
    }
    
    @Override
    public void markDirty(CommandContext<CommandSourceStack> context) {
        CinematicToolsServer.markDirty(context.getSource().getLevel());
    }
    
    @Override
    public Player getPlayer(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return EntityArgument.getPlayer(context, "player");
    }
    
    @Override
    public Entity getEntity(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return EntityArgument.getEntity(context, name);
    }
    
}
