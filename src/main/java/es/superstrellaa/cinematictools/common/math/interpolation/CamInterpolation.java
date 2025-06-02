package es.superstrellaa.cinematictools.common.math.interpolation;

import java.util.List;

import es.superstrellaa.cinematictools.common.scene.CamScene;
import es.superstrellaa.cinematictools.common.scene.attribute.CamAttribute;
import es.superstrellaa.cinematictools.common.utils.EnvExecutor;
import team.creative.creativecore.common.util.math.interpolation.Interpolation;
import team.creative.creativecore.common.util.math.vec.VecNd;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.creativecore.common.util.type.Color;

public abstract class CamInterpolation {
    
    public static final NamedHandlerRegistry<CamInterpolation> REGISTRY = new NamedHandlerRegistry<>(null);
    
    public static final CamInterpolation HERMITE;
    
    static {
        REGISTRY.register("linear", new LinearCamInterpolation());
        REGISTRY.register("cubic", new CubicCamInterpolation());
        REGISTRY.registerDefault("hermite", HERMITE = new HermiteCamInterpolation());
        REGISTRY.register("cosine", new SmoothCamInterpolation());
        REGISTRY.register("circular", EnvExecutor.safeCallWhenOn(() -> () -> new ClientCircularCamInterpolation(true), () -> () -> new CircularCamInterpolation(true)));
        REGISTRY.register("invcircular", EnvExecutor.safeCallWhenOn(() -> () -> new ClientCircularCamInterpolation(false), () -> () -> new CircularCamInterpolation(false)));
    }
    
    public boolean isRenderingEnabled = false;
    public final Color color;
    
    public CamInterpolation(Color color) {
        this.color = color;
    }
    
    public abstract <T extends VecNd> Interpolation<T> create(double[] times, CamScene scene, T before, List<T> points, T after, CamAttribute<T> attribute);
    
}
