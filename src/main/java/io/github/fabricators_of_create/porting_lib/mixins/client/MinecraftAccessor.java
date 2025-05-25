package io.github.fabricators_of_create.porting_lib.mixins.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor("pausePartialTick")
    float port_lib$pausePartialTick();
}
