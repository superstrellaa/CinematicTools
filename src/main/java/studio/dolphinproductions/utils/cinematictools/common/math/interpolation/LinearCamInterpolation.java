package studio.dolphinproductions.utils.cinematictools.common.math.interpolation;

import java.util.List;

import studio.dolphinproductions.utils.cinematictools.common.scene.CamScene;
import studio.dolphinproductions.utils.cinematictools.common.scene.attribute.CamAttribute;
import team.creative.creativecore.common.util.math.interpolation.Interpolation;
import team.creative.creativecore.common.util.math.interpolation.LinearInterpolation;
import team.creative.creativecore.common.util.math.vec.VecNd;
import team.creative.creativecore.common.util.type.Color;

public class LinearCamInterpolation extends CamInterpolation {
    
    public LinearCamInterpolation() {
        super(new Color(0, 0, 255));
    }
    
    @Override
    public <T extends VecNd> Interpolation<T> create(double[] times, CamScene scene, T before, List<T> points, T after, CamAttribute<T> attribute) {
        return new LinearInterpolation<T>(times, points);
    }
}
