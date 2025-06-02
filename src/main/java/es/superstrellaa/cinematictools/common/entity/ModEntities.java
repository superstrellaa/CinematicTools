package es.superstrellaa.cinematictools.common.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;

public class ModEntities {
    public static final EntityType<NPCEntity> NPC = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation("cinematictools", "npc"),
            EntityType.Builder.of(NPCEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .build("npc")
    );

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(NPC, NPCEntity.createMobAttributes());
    }
}
