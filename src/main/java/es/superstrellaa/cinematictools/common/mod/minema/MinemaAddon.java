package es.superstrellaa.cinematictools.common.mod.minema;

import es.superstrellaa.cinematictools.CinematicTools;
import team.creative.creativecore.CreativeCore;

public class MinemaAddon {
    
    public static final String MODID = "minema_resurrection";
    
    private static final boolean installed;
    
    static {
        installed = CreativeCore.loader().isModLoaded(MODID);
    }
    
    public static boolean installed() {
        return installed && CinematicTools.CONFIG.syncMinema;
    }
    
    public static long getVideoTime() {
        return MinemaInteractor.getVideoTime();
    }
    
    public static boolean isCapturing() {
        return installed() && MinemaInteractor.isCapturing();
    }
    
    public static void startCapture() {
        MinemaInteractor.startCapture();
    }
    
    public static void pauseCapture() {
        MinemaInteractor.pauseCapture();
    }
    
    public static void resumeCapture() {
        MinemaInteractor.resumeCapture();
    }
    
    public static void stopCapture() {
        MinemaInteractor.stopCapture();
    }
    
}
