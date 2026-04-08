package es.superstrellaa.cinematictools.client.model;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import es.superstrellaa.cinematictools.common.entity.NPCEntity;

public class NPCEntityModel extends GeoModel<NPCEntity> {
    @Override
    public ResourceLocation getModelResource(NPCEntity object) {
        return ResourceLocation.fromNamespaceAndPath("cinematictools", "geo/npc.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NPCEntity object) {
        return object.getSkinTexture() != null ? object.getSkinTexture() :
                ResourceLocation.fromNamespaceAndPath("cinematictools", "textures/entity/npc.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NPCEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("cinematictools", "animations/npc.animation.json");
    }
}
