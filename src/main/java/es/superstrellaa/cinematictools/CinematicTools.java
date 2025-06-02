package es.superstrellaa.cinematictools;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import es.superstrellaa.cinematictools.common.packet.*;
import io.github.fabricators_of_create.porting_lib.features.LevelExtensions;
import io.github.fabricators_of_create.porting_lib.features.entity.MultiPartEntity;
import io.github.fabricators_of_create.porting_lib.features.entity.PartEntity;
import io.github.fabricators_of_create.porting_lib.features.registry.LazyRegistrar;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import es.superstrellaa.cinematictools.common.command.argument.CamModeArgument;
import es.superstrellaa.cinematictools.common.command.argument.CamPitchModeArgument;
import es.superstrellaa.cinematictools.common.command.argument.DurationArgument;
import es.superstrellaa.cinematictools.common.command.argument.InterpolationArgument;
import es.superstrellaa.cinematictools.common.command.argument.InterpolationArgument.AllInterpolationArgument;
import es.superstrellaa.cinematictools.common.command.builder.SceneCommandBuilder;
import es.superstrellaa.cinematictools.common.command.builder.SceneStartCommandBuilder;
import es.superstrellaa.cinematictools.common.entity.ModEntities;
import es.superstrellaa.cinematictools.common.entity.NPCEntity;
import es.superstrellaa.cinematictools.common.network.ServerPacketHandler;
import es.superstrellaa.cinematictools.common.scene.CamScene;
import es.superstrellaa.cinematictools.common.utils.DolphinLogger;
import es.superstrellaa.cinematictools.fabric.ArgumentTypeInfosHelper;
import es.superstrellaa.cinematictools.server.CinematicToolsServer;
import es.superstrellaa.cinematictools.server.CamEventHandler;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.creativecore.common.network.CreativePacket;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Mod(value = CinematicTools.MODID)
public class CinematicTools implements ModInitializer {
    
    public static final String MODID = "cinematictools";

    private static final Logger LOGGER = LogManager.getLogger(CinematicTools.MODID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork(1, LOGGER, new ResourceLocation(CinematicTools.MODID, "main"));
    public static final CinematicToolsConfig CONFIG = new CinematicToolsConfig();
    public static final LazyRegistrar<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = LazyRegistrar.create(Registries.COMMAND_ARGUMENT_TYPE, MODID);

    public static final Map<String, NPCEntity> npcMap = new HashMap<>();

    private void initPartEntity(){
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof MultiPartEntity partEntity && partEntity.isMultipartEntity()) {
                PartEntity<?>[] parts = partEntity.getParts();
                if (parts != null) {
                    for (PartEntity<?> part : parts) {
                        ((LevelExtensions)(world)).getPartEntityMap().put(part.getId(), part);
                    }
                }
            }
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof MultiPartEntity partEntity && partEntity.isMultipartEntity()) {
                PartEntity<?>[] parts = partEntity.getParts();
                if (parts != null) {
                    for (PartEntity<?> part : parts) {
                        ((LevelExtensions)world).getPartEntityMap().remove(part.getId());
                    }
                }
            }
        });
    }

    public void onInitialize() {
        ServerPacketHandler.registerPackets();
        DolphinLogger.logModInfo();
        ModEntities.registerAttributes();
        this.initPartEntity();
        this.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            commands(dispatcher);
        });

        COMMAND_ARGUMENT_TYPES.register("duration", () -> ArgumentTypeInfosHelper.registerByClass(DurationArgument.class, SingletonArgumentInfo.<DurationArgument>contextFree(
            () -> DurationArgument.duration())));
        COMMAND_ARGUMENT_TYPES.register("cam_mode", () -> ArgumentTypeInfosHelper.registerByClass(CamModeArgument.class, SingletonArgumentInfo.<CamModeArgument>contextFree(
            () -> CamModeArgument.mode())));
        COMMAND_ARGUMENT_TYPES.register("interpolation", () -> ArgumentTypeInfosHelper.registerByClass(InterpolationArgument.class, SingletonArgumentInfo
                .<InterpolationArgument>contextFree(() -> InterpolationArgument.interpolation())));
        COMMAND_ARGUMENT_TYPES.register("all_interpolation", () -> ArgumentTypeInfosHelper.registerByClass(AllInterpolationArgument.class, SingletonArgumentInfo
                .<AllInterpolationArgument>contextFree(() -> InterpolationArgument.interpolationAll())));
        COMMAND_ARGUMENT_TYPES.register("pitch_mode", () -> ArgumentTypeInfosHelper.registerByClass(CamPitchModeArgument.class, SingletonArgumentInfo.<CamPitchModeArgument>contextFree(
            () -> CamPitchModeArgument.pitchMode())));
        COMMAND_ARGUMENT_TYPES.register();

        DolphinLogger.info("CinematicTools from Utils loaded successfully!");
    }
    
    private void init() {
        NETWORK.registerType(ConnectPacket.class, ConnectPacket::new);
        NETWORK.registerType(GetPathPacket.class, GetPathPacket::new);
        NETWORK.registerType(SetPathPacket.class, SetPathPacket::new);
        NETWORK.registerType(StartPathPacket.class, StartPathPacket::new);
        NETWORK.registerType(StopPathPacket.class, StopPathPacket::new);
        NETWORK.registerType(TeleportPathPacket.class, TeleportPathPacket::new);
        NETWORK.registerType(PausePathPacket.class, PausePathPacket::new);
        NETWORK.registerType(ResumePathPacket.class, ResumePathPacket::new);
        
        new CamEventHandler();
        
        CreativeConfigRegistry.ROOT.registerValue(MODID, CONFIG);
    }
    
    private void commands(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> camServer = Commands.literal("cinematic-server");

        LiteralArgumentBuilder<CommandSourceStack> npcCommand = Commands.literal("npc");
        npcCommand.then(Commands.literal("summon")
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            String name = StringArgumentType.getString(context, "name");
                            Vec3 position = source.getPosition();
                            float yaw = source.getRotation().y;
                            float pitch = source.getRotation().x;
                            return summonNPC(source, name, position, yaw, pitch);
                        })
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    String name = StringArgumentType.getString(context, "name");
                                    Vec3 position = Vec3Argument.getVec3(context, "pos");
                                    float yaw = source.getRotation().y;
                                    float pitch = source.getRotation().x;
                                    return summonNPC(source, name, position, yaw, pitch);
                                })
                                .then(Commands.argument("yaw", FloatArgumentType.floatArg(-180, 180))
                                        .then(Commands.argument("pitch", FloatArgumentType.floatArg(-90, 90))
                                                .executes(context -> {
                                                    CommandSourceStack source = context.getSource();
                                                    String name = StringArgumentType.getString(context, "name");
                                                    Vec3 position = Vec3Argument.getVec3(context, "pos");
                                                    float yaw = FloatArgumentType.getFloat(context, "yaw");
                                                    float pitch = FloatArgumentType.getFloat(context, "pitch");
                                                    return summonNPC(source, name, position, yaw, pitch);
                                                })
                                        )
                                )
                        )
                )
        );

        npcCommand.then(Commands.literal("walk")
                .then(Commands.argument("name", StringArgumentType.string())
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    String name = StringArgumentType.getString(context, "name");
                                    Vec3 position = Vec3Argument.getVec3(context, "pos");
                                    return walkNPC(source, name, position);
                                })
                        )
                )
        );

        npcCommand.then(Commands.literal("run")
                .then(Commands.argument("name", StringArgumentType.string())
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    String name = StringArgumentType.getString(context, "name");
                                    Vec3 position = Vec3Argument.getVec3(context, "pos");
                                    return runNPC(source, name, position);
                                })
                        )
                )
        );

        npcCommand.then(Commands.literal("jump")
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            String name = StringArgumentType.getString(context, "name");
                            return jumpNPC(source, name);
                        })
                )
        );

        npcCommand.then(Commands.literal("animation")
                .then(Commands.literal("play")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("animation", StringArgumentType.string())
                                        .executes(context -> {
                                            if (context.getSource().hasPermission(2)) {
                                                String name = StringArgumentType.getString(context, "name");
                                                String animation = StringArgumentType.getString(context, "animation");
                                                return playAnimation(context.getSource(), name, animation);
                                            } else {
                                                context.getSource().sendFailure(Component.translatable("commands.permission.failure"));
                                                return 0;
                                            }
                                        })
                                )
                        )
                )
                .then(Commands.literal("stop")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("animation", StringArgumentType.string())
                                        .executes(context -> {
                                            if (context.getSource().hasPermission(2)) {
                                                String name = StringArgumentType.getString(context, "name");
                                                String animation = StringArgumentType.getString(context, "animation");
                                                return stopAnimation(context.getSource(), name, animation);
                                            } else {
                                                context.getSource().sendFailure(Component.translatable("commands.permission.failure"));
                                                return 0;
                                            }
                                        })
                                )
                        )
                )
                .then(Commands.literal("resume")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("animation", StringArgumentType.string())
                                        .executes(context -> {
                                            if (context.getSource().hasPermission(2)) {
                                                String name = StringArgumentType.getString(context, "name");
                                                String animation = StringArgumentType.getString(context, "animation");
                                                return resumeAnimation(context.getSource(), name, animation);
                                            } else {
                                                context.getSource().sendFailure(Component.translatable("commands.permission.failure"));
                                                return 0;
                                            }
                                        })
                                )
                        )
                )
        );

        camServer.then(npcCommand);

        SceneStartCommandBuilder.start(camServer, CinematicToolsServer.PROCESSOR);

        LiteralArgumentBuilder<CommandSourceStack> get = Commands.literal("get");
        SceneCommandBuilder.scene(get, CinematicToolsServer.PROCESSOR);
        camServer.then(get);

        dispatcher.register(camServer.then(Commands.literal("stop").then(Commands.argument("players", EntityArgument.players()).executes(x -> {
            if (x.getSource().hasPermission(2)) {
                CreativePacket packet = new StopPathPacket();
                for (ServerPlayer player : EntityArgument.getPlayers(x, "players"))
                    CinematicTools.NETWORK.sendToClient(packet, player);
                return 0;
            } else {
                x.getSource().sendFailure(Component.translatable("commands.permission.failure"));
                return 0;
            }
        }))).then(Commands.literal("pause").then(Commands.argument("players", EntityArgument.players()).executes(x -> {
            if (x.getSource().hasPermission(2)) {
                CreativePacket packet = new PausePathPacket();
                for (ServerPlayer player : EntityArgument.getPlayers(x, "players"))
                    CinematicTools.NETWORK.sendToClient(packet, player);
                return 0;
            } else {
                x.getSource().sendFailure(Component.translatable("commands.permission.failure"));
                return 0;
            }
        }))).then(Commands.literal("resume").then(Commands.argument("players", EntityArgument.players()).executes(x -> {
            if (x.getSource().hasPermission(2)) {
                CreativePacket packet = new ResumePathPacket();
                for (ServerPlayer player : EntityArgument.getPlayers(x, "players"))
                    CinematicTools.NETWORK.sendToClient(packet, player);
                return 0;
            } else {
                x.getSource().sendFailure(Component.translatable("commands.permission.failure"));
                return 0;
            }
        }))).then(Commands.literal("list").executes((x) -> {
            if (x.getSource().hasPermission(2)) {
                Collection<String> names = CinematicToolsServer.getSavedPaths(x.getSource().getLevel());
                x.getSource().sendSystemMessage(Component.translatable("scenes.list", names.size(), String.join(", ", names)));
                return 0;
            } else {
                x.getSource().sendFailure(Component.translatable("commands.permission.failure"));
                return 0;
            }
        })).then(Commands.literal("clear").executes((x) -> {
            if (x.getSource().hasPermission(2)) {
                CinematicToolsServer.clearPaths(x.getSource().getLevel());
                x.getSource().sendSuccess(() -> Component.translatable("scenes.clear"), true);
                return 0;
            } else {
                x.getSource().sendFailure(Component.translatable("commands.permission.failure"));
                return 0;
            }
        })).then(Commands.literal("remove").then(Commands.argument("name", StringArgumentType.string()).executes((x) -> {
            if (x.getSource().hasPermission(2)) {
                String name = StringArgumentType.getString(x, "name");
                if (CinematicToolsServer.removePath(x.getSource().getLevel(), name))
                    x.getSource().sendSuccess(() -> Component.translatable("scene.remove", name), true);
                else
                    x.getSource().sendFailure(Component.translatable("scene.remove_fail", name));
                return 0;
            } else {
                x.getSource().sendFailure(Component.translatable("commands.permission.failure"));
                return 0;
            }
        }))).then(Commands.literal("create").then(Commands.argument("name", StringArgumentType.string()).executes((x) -> {
            if (x.getSource().hasPermission(2)) {
                String name = StringArgumentType.getString(x, "name");
                if (CinematicToolsServer.get(x.getSource().getLevel(), name) != null)
                    x.getSource().sendSuccess(() -> Component.translatable("scene.exists", name), true);
                else {
                    CinematicToolsServer.set(x.getSource().getLevel(), name, CamScene.createDefault());
                    x.getSource().sendSuccess(() -> Component.translatable("scene.create", name), true);
                }
                return 0;
            } else {
                x.getSource().sendFailure(Component.translatable("commands.permission.failure"));
                return 0;
            }
        }))));
    }

    private int playAnimation(CommandSourceStack source, String name, String animation) {
        Level level = source.getLevel();
        NPCEntity npc = findNPCByName(name);

        if (npc == null) {
            source.sendFailure(Component.translatable("npc.not_found", name));
            return 0;
        }

        npc.playAnimation(animation);

        source.sendSuccess(() -> Component.translatable("npc.animation.success", name, animation), true);
        return 1;
    }



    private int stopAnimation(CommandSourceStack source, String name, String animation) {
        Level level = source.getLevel();
        NPCEntity npc = findNPCByName(name);

        if (npc == null) {
            source.sendFailure(Component.translatable("npc.not_found", name));
            return 0;
        }

        npc.stopAnimation();

        source.sendSuccess(() -> Component.translatable("npc.animation.stopped", name), true);
        return 1;
    }


    private int resumeAnimation(CommandSourceStack source, String name, String animation) {
        Level level = source.getLevel();
        NPCEntity npc = findNPCByName(name);

        if (npc == null) {
            source.sendFailure(Component.translatable("npc.not_found", name));
            return 0;
        }

        npc.resumeAnimation();

        source.sendSuccess(() -> Component.translatable("npc.animation.resumed", name), true);
        return 1;
    }

    private int summonNPC(CommandSourceStack source, String name, Vec3 position, float yaw, float pitch) {
        Level level = source.getLevel();
        double x = position.x();
        double y = position.y();
        double z = position.z();

        NPCEntity npc = new NPCEntity(ModEntities.NPC, level);
        npc.setPos(x, y, z);
        npc.setYRot(yaw);
        npc.setXRot(pitch);
        npc.setName(name);
/*
        npc.downloadAndApplySkin(name);*/
        npc.setSkinURL("https://mc-heads.net/skin/" + name + ".png");

        level.addFreshEntity(npc);

        npcMap.put(name, npc);

        source.sendSuccess(() -> Component.translatable("npc.summon.success", name, x, y, z, yaw, pitch), true);
        return 1;
    }


    private int walkNPC(CommandSourceStack source, String name, Vec3 position) {
        Level level = source.getLevel();
        NPCEntity npc = findNPCByName(name);

        if (npc == null) {
            source.sendFailure(Component.translatable("npc.not_found", name));
            return 0;
        }

        double x = position.x();
        double y = position.y();
        double z = position.z();

        npc.setRunning(false);
        npc.getNavigation().moveTo(x, y, z, 1.0);

        source.sendSuccess(() -> Component.translatable("npc.walk.success", name, x, y, z), true);
        return 1;
    }

    private int runNPC(CommandSourceStack source, String name, Vec3 position) {
        Level level = source.getLevel();
        NPCEntity npc = findNPCByName(name);

        if (npc == null) {
            source.sendFailure(Component.translatable("npc.not_found", name));
            return 0;
        }

        double x = position.x();
        double y = position.y();
        double z = position.z();

        npc.getNavigation().moveTo(x, y, z, 1.5);
        npc.setRunning(true);

        source.sendSuccess(() -> Component.translatable("npc.run.success", name, x, y, z), true);
        return 1;
    }

    private int jumpNPC(CommandSourceStack source, String name) {
        Level level = source.getLevel();
        NPCEntity npc = findNPCByName(name);

        if (npc == null) {
            source.sendFailure(Component.translatable("npc.not_found", name));
            return 0;
        }

        npc.setJumping(true);
        source.sendSuccess(() -> Component.translatable("npc.jump.success", name), true);
        return 1;
    }

    private NPCEntity findNPCByName(String name) {
        return npcMap.get(name);
    }

}
