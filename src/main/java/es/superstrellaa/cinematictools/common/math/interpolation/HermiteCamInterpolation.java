package es.superstrellaa.cinematictools.common.math.interpolation;

import java.util.List;

import es.superstrellaa.cinematictools.common.scene.CamScene;
import es.superstrellaa.cinematictools.common.scene.attribute.CamAttribute;
import team.creative.creativecore.common.util.math.interpolation.HermiteInterpolation;
import team.creative.creativecore.common.util.math.interpolation.Interpolation;
import team.creative.creativecore.common.util.math.vec.VecNd;
import team.creative.creativecore.common.util.type.Color;

public class HermiteCamInterpolation extends CamInterpolation {
    
    public HermiteCamInterpolation() {
        super(new Color(255, 255, 255));
    }
    
    @Override
    public <T extends VecNd> Interpolation<T> create(double[] times, CamScene scene, T before, List<T> points, T after, CamAttribute<T> attribute) {
        return new HermiteInterpolation<T>(times, before, points, after);
    }
    
}
