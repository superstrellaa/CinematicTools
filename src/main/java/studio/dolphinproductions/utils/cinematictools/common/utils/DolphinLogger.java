package studio.dolphinproductions.utils.cinematictools.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.dolphinproductions.utils.cinematictools.CinematicTools;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class DolphinLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(CinematicTools.MODID);

    public static void info(String message) {
        LOGGER.info("[CinematicTools] [INFO] " + message);
    }

    public static void warn(String message) {
        LOGGER.warn("[CinematicTools] [WARN] " + message);
    }

    public static void error(String message) {
        LOGGER.error("[CinematicTools] [ERROR] " + message);
    }

    public static void debug(String message) {
        LOGGER.debug("[CinematicTools] [DEBUG] " + message);
    }

    public static void logModInfo() {
        EnvType envType = FabricLoader.getInstance().getEnvironmentType();
        String mode = (envType == EnvType.CLIENT) ? "Client-side" : "Server-side";

        String modVersion = FabricLoader.getInstance().getModContainer(CinematicTools.MODID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("Unknown Version");

        LOGGER.info("[CinematicTools] CinematicTools Mod loaded in {} mode", mode);
        LOGGER.info("[CinematicTools] CinematicTools Mod version: {}", modVersion);
    }
}
