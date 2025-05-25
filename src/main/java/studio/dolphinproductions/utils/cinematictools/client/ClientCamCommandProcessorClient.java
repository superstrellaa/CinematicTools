package studio.dolphinproductions.utils.cinematictools.client;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import studio.dolphinproductions.utils.cinematictools.client.command.ClientCamCommandProcessor;
import studio.dolphinproductions.utils.cinematictools.common.math.point.CamPoint;
import studio.dolphinproductions.utils.cinematictools.common.scene.CamScene;

public class ClientCamCommandProcessorClient implements ClientCamCommandProcessor {

    @Override
    public CamScene getScene(CommandContext<FabricClientCommandSource> context) {
        return CinematicToolsClient.getConfigScene();
    }

    @Override
    public boolean canSelectTarget() {
        return true;
    }

    @Override
    public void selectTarget(CommandContext<FabricClientCommandSource> context, boolean look) throws SceneException {
        if (!CinematicToolsClient.isOp) {
            context.getSource().sendError(Component.translatable("commands.permission.failure"));
            return;
        }
        if (!look)
            checkFollowTarget(context, true);
        CamEventHandlerClient.startSelectionMode(x -> {
            try {
                setTarget(context, x, look);
            } catch (SceneException e) {}
        });
    }

    @Override
    public boolean canCreatePoint(CommandContext<FabricClientCommandSource> context) {
        return true;
    }

    @Override
    public CamPoint createPoint(CommandContext<FabricClientCommandSource> context) {
        return CamPoint.createLocal();
    }

    @Override
    public boolean requiresSceneName() {
        return false;
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    public void start(CommandContext<FabricClientCommandSource> context) throws SceneException {
        if (!CinematicToolsClient.isOp) {
            context.getSource().sendError(Component.translatable("commands.permission.failure"));
            return;
        }
        CinematicToolsClient.start(CinematicToolsClient.createScene());
    }

    @Override
    public void teleport(CommandContext<FabricClientCommandSource> context, int index) {
        if (!CinematicToolsClient.isOp) {
            context.getSource().sendError(Component.translatable("commands.permission.failure"));
            return;
        }
        CinematicToolsClient.teleportTo(getScene(context).points.get(index));
    }

    @Override
    public void markDirty(CommandContext<FabricClientCommandSource> context) {
        CinematicToolsClient.checkTargetMarker();
    }

    @Override
    public Player getPlayer(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        EntitySelectorClient selector = (EntitySelectorClient) context.getArgument(name, EntitySelector.class);
        return selector.findSinglePlayerClient(context.getSource());
    }

    @Override
    public Entity getEntity(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        EntitySelectorClient selector = (EntitySelectorClient) context.getArgument(name, EntitySelector.class);
        return selector.findSingleEntityClient(context.getSource());
    }

}