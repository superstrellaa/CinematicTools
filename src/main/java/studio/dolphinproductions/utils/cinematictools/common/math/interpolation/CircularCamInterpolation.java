package studio.dolphinproductions.utils.cinematictools.common.math.interpolation;

import java.util.List;

import studio.dolphinproductions.utils.cinematictools.common.scene.CamScene;
import studio.dolphinproductions.utils.cinematictools.common.scene.attribute.CamAttribute;
import team.creative.creativecore.common.util.math.interpolation.Interpolation;
import team.creative.creativecore.common.util.math.vec.VecNd;
import team.creative.creativecore.common.util.type.Color;

public class CircularCamInterpolation extends CamInterpolation {
    
    public final boolean clockwise;
    
    public CircularCamInterpolation(boolean clockwise) {
        super(new Color(255, 255, 0));
        this.clockwise = clockwise;
    }

    @Override
    public <T extends VecNd> Interpolation<T> create(double[] times, CamScene scene, T before, List<T> points, T after, CamAttribute<T> attribute) {
        return null;
    }
}
