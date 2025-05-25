package studio.dolphinproductions.utils.cinematictools.common.mod.minema;

public class MinemaInteractor {
    
    private static boolean STARTED = false;
    
    public static boolean isCapturing() {
        return STARTED;
    }
    
    public static void startCapture() {
        STARTED = true;
        //CaptureSession.singleton.startCapture();
    }
    
    public static void pauseCapture() {
        //CaptureSession.singleton.isPaused = true;
    }
    
    public static void resumeCapture() {
        //CaptureSession.singleton.isPaused = false;
    }
    
    public static void stopCapture() {
        STARTED = false;
        //CaptureSession.singleton.stopCapture();
    }
    
    public static long getVideoTime() {
        //return CaptureSession.singleton.getTime().getVideoTime();
        return 0;
    }
}
