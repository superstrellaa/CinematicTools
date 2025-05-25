package studio.dolphinproductions.utils.cinematictools;

import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.sync.ConfigSynchronization;

public class CinematicToolsConfig {
    
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public boolean syncMinema = true;
    
}
