package studio.dolphinproductions.utils.cinematictools.common.scene.mode;

import net.minecraft.world.entity.Entity;
import studio.dolphinproductions.utils.cinematictools.common.math.point.CamPoint;
import studio.dolphinproductions.utils.cinematictools.common.scene.CamScene;
import studio.dolphinproductions.utils.cinematictools.common.scene.run.CamRun;
import studio.dolphinproductions.utils.cinematictools.common.target.CamTarget.SelfTarget;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public class DefaultMode extends CamMode {
    
    public DefaultMode(CamScene scene) {
        super(scene);
        if (scene.lookTarget instanceof SelfTarget)
            scene.lookTarget = null;
        if (scene.posTarget instanceof SelfTarget)
            scene.posTarget = null;
    }
    
    @Override
    public void process(CamPoint point) {
//        super.process(point);
//        EnvExecutor.safeRunWhenOn(EnvType.CLIENT, () -> () -> {
//            Minecraft.getInstance().mouseHandler.grabMouse();
//        });
    }
    
    @Override
    public void finished(CamRun run) {
//        super.finished(run);
//        EnvExecutor.safeRunWhenOn(EnvType.CLIENT, () -> () -> {
//            Minecraft mc = Minecraft.getInstance();
//            if (!mc.player.isCreative())
//                mc.player.getAbilities().flying = false;
//        });
    }
    
    @Override
    public Entity getCamera() {
        return null;
//        return EnvExecutor.safeCallWhenOn(EnvType.CLIENT, () -> () -> Minecraft.getInstance().player);
    }
    
    @Override
    public void correctTargetPosition(Vec3d vec) {
//        EnvExecutor.safeRunWhenOn(EnvType.CLIENT, () -> () -> {
//            vec.y -= Minecraft.getInstance().player.getEyeHeight();
//        });
    }
    
    @Override
    public boolean outside() {
        return false;
    }
    
}
