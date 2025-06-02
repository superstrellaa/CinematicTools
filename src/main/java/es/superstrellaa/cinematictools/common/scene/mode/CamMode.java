package es.superstrellaa.cinematictools.common.scene.mode;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import es.superstrellaa.cinematictools.client.CamEventHandlerClient;
import es.superstrellaa.cinematictools.common.math.point.CamPoint;
import es.superstrellaa.cinematictools.common.scene.CamScene;
import es.superstrellaa.cinematictools.common.scene.run.CamRun;
import es.superstrellaa.cinematictools.common.utils.EnvExecutor;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.registry.NamedTypeRegistry;

public abstract class CamMode {
    
    public static final NamedTypeRegistry<CamMode> REGISTRY = new NamedTypeRegistry<CamMode>().addConstructorPattern(CamScene.class);
    
    static {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){
            REGISTRY.register("default", ClientDefaultMode.class);
        }else{
            REGISTRY.register("default", DefaultMode.class);
        }
        REGISTRY.register("outside", OutsideMode.class);
    }
    
    public final CamScene scene;
    
    public CamMode(CamScene scene) {
        this.scene = scene;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void started(CamRun run) {}
    
    @OnlyIn(Dist.CLIENT)
    public void finished(CamRun run) {
        EnvExecutor.safeRunWhenOn(EnvType.CLIENT, () -> () -> {
            CamEventHandlerClient.resetFOV();
            CamEventHandlerClient.resetRoll();
        });
    }
    
    @OnlyIn(Dist.CLIENT)
    public abstract Entity getCamera();
    
    @OnlyIn(Dist.CLIENT)
    public void process(CamPoint point) {
        EnvExecutor.safeRunWhenOn(EnvType.CLIENT, () -> () -> {
            CamEventHandlerClient.roll((float) point.roll);
            CamEventHandlerClient.fov(point.zoom);

            Entity camera = getCamera();
            if (camera instanceof Player)
                ((Player) camera).getAbilities().flying = true;

            camera.absMoveTo(point.x, point.y - camera.getEyeHeight(), point.z, (float) point.rotationYaw, (float) point.rotationPitch);
            camera.yRotO = (float) point.rotationYaw;
            camera.xRotO = (float) point.rotationPitch;
            camera.moveTo(point.x, point.y - camera.getEyeHeight(), point.z, (float) point.rotationYaw, (float) point.rotationPitch);
        });
    }
    
    public abstract boolean outside();
    
    @OnlyIn(Dist.CLIENT)
    public void correctTargetPosition(Vec3d vec) {}
    
}
