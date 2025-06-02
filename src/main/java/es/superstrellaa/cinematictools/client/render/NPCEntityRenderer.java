package es.superstrellaa.cinematictools.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import es.superstrellaa.cinematictools.common.entity.NPCEntity;
import es.superstrellaa.cinematictools.client.model.NPCEntityModel;

public class NPCEntityRenderer extends GeoEntityRenderer<NPCEntity> {
    public NPCEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new NPCEntityModel());
    }

    @Override
    public ResourceLocation getTextureLocation(NPCEntity entity) {
        return entity.getSkinTexture() != null ? entity.getSkinTexture() :
                new ResourceLocation("cinematictools", "textures/entity/npc.png");
    }
}
