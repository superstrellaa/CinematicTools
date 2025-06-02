package es.superstrellaa.cinematictools.server;

import java.util.Collection;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import es.superstrellaa.cinematictools.common.scene.CamScene;

public class CinematicToolsServer {

    public static final CamCommandProcessorServer PROCESSOR = new CamCommandProcessorServer();

    private static CamSaveData getData(Level level) {
        return ((ServerLevel) level).getDataStorage().computeIfAbsent(
                tag -> new CamSaveData(tag),
                CamSaveData::new,
                CamSaveData.DATA_NAME
        );
    }

    public static CamScene get(Level level, String name) {
        return getData(level).get(name);
    }

    public static void set(Level level, String name, CamScene scene) {
        getData(level).set(name, scene);
    }

    public static void markDirty(Level level) {
        getData(level).setDirty();
    }

    public static boolean removePath(Level level, String name) {
        return getData(level).remove(name);
    }

    public static Collection<String> getSavedPaths(Level level) {
        return getData(level).names();
    }

    public static void clearPaths(Level level) {
        getData(level).clear();
    }
}
