package es.superstrellaa.cinematictools.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.github.fabricators_of_create.porting_lib.features.CameraSetupCallback;
import io.github.fabricators_of_create.porting_lib.features.FieldOfViewEvents;
import io.github.fabricators_of_create.porting_lib.features.RenderTickStartCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import es.superstrellaa.cinematictools.client.mixin.GameRendererAccessor;
import es.superstrellaa.cinematictools.common.math.interpolation.CamInterpolation;
import es.superstrellaa.cinematictools.common.math.point.CamPoint;
import es.superstrellaa.cinematictools.common.math.point.CamPoints;
import es.superstrellaa.cinematictools.common.scene.CamScene;
import es.superstrellaa.cinematictools.common.scene.attribute.CamAttribute;
import es.superstrellaa.cinematictools.common.scene.mode.OutsideMode;
import es.superstrellaa.cinematictools.common.target.CamTarget;
import team.creative.creativecore.common.util.math.interpolation.Interpolation;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.util.ArrayList;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class CamEventHandlerClient {
    
    public static final Minecraft MC = Minecraft.getInstance();
    
    public static final double ZOOM_STEP = 0.005;
    public static final float ROLL_STEP = 1.5F;
    public static final double MAX_FOV = 170;
    public static final double MIN_FOV = 0.1;
    public static final double FOV_RANGE = MAX_FOV - MIN_FOV;
    public static final double FOV_RANGE_HALF = FOV_RANGE / 2;
    
    public static Entity camera = null;
    
    private static double fov = 0;
    private static float roll = 0;
    private static Consumer<CamTarget> selectingTarget = null;
    
    private static boolean renderingHand = false;
    private static boolean skipFov = false;
    
    public static void startSelectionMode(Consumer<CamTarget> selectingTarget) {
        CamEventHandlerClient.selectingTarget = selectingTarget;
    }
    
    public static void resetRoll() {
        roll = 0;
    }
    
    public static float roll() {
        return roll;
    }
    
    public static void roll(float roll) {
        CamEventHandlerClient.roll = roll;
    }
    
    public static void resetFOV() {
        fov = 0;
    }
    
    public static double fovExactVanilla(float partialTickTime) {
        try {
            skipFov = true;
            return ((GameRendererAccessor) MC.gameRenderer).callGetFov(MC.gameRenderer.getMainCamera(), partialTickTime, true);
        } finally {
            skipFov = false;
        }
    }
    
    public static double fovExact(float partialTickTime) {
        return fovExactVanilla(partialTickTime) + fov;
    }
    
    public static void fov(double fov) {
        CamEventHandlerClient.fov = fov;
    }

    public CamEventHandlerClient() {
        ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);
        RenderTickStartCallback.EVENT.register(this::onRenderTick);
        FieldOfViewEvents.COMPUTE.register(this::fov);
        WorldRenderEvents.AFTER_ENTITIES.register(this::worldRender);

        CameraSetupCallback.EVENT.register(this::cameraRoll);

        UseBlockCallback.EVENT.register(this::onPlayerUseBlock);
        UseEntityCallback.EVENT.register(this::onPlayerUseEntity);
    }

    public void onClientTick(Minecraft MC) {
        if (MC.player != null && MC.level != null && !MC.isPaused() && CinematicToolsClient.isPlaying())
            CinematicToolsClient.gameTickPath(MC.level);
    }
    
    private double calculatePointInCurve(double fov) {
        fov -= MIN_FOV;
        fov /= FOV_RANGE_HALF;
        fov = Mth.clamp(fov, 0, 2);
        return Math.asin(fov - 1) / Math.PI + 0.5;
    }
    
    private double transformFov(double x) {
        if (x <= 0)
            return MIN_FOV;
        if (x >= 1)
            return MAX_FOV;
        return (Math.sin((x - 0.5) * Math.PI) + 1) * FOV_RANGE_HALF + MIN_FOV;
    }

    public void onRenderTick() {
        if (MC.level == null) {
            CinematicToolsClient.resetServerAvailability();
            CinematicToolsClient.resetTargetMarker();
        }

        var renderTickTime = MC.getDeltaFrameTime();

        renderingHand = false;
        
        if (MC.player != null && MC.level != null) {
            if (!MC.isPaused()) {
                if (CinematicToolsClient.isPlaying()) {
                    while (MC.options.keyJump.consumeClick()) {
                        if (CinematicToolsClient.isPlaying() && !CinematicToolsClient.getScene().mode.outside())
                            CinematicToolsClient.getScene().togglePause();
                    }
                    
                    CinematicToolsClient.renderTickPath(MC.level, renderTickTime);
                } else {
                    CinematicToolsClient.noTickPath(MC.level, renderTickTime);
                    double timeFactor = MC.getDeltaFrameTime();
                    double vanillaFov = fovExactVanilla(renderTickTime);
                    double currentFov = vanillaFov + fov;
                    double x = calculatePointInCurve(currentFov);
                    double multiplier = MC.player.isCrouching() ? 5 : 1;
                    
                    /*if (KeyHandler.zoomIn.isDown())
                        fov = transformFov(multiplier * timeFactor * -ZOOM_STEP + x) - vanillaFov;
                    
                    if (KeyHandler.zoomOut.isDown())
                        fov = transformFov(multiplier * timeFactor * ZOOM_STEP + x) - vanillaFov;
                    
                    if (KeyHandler.zoomCenter.isDown())
                        resetFOV();*/
                    
                    /*if (KeyHandler.rollLeft.isDown())
                        roll -= timeFactor * ROLL_STEP;
                    
                    if (KeyHandler.rollRight.isDown())
                        roll += timeFactor * ROLL_STEP;
                    
                    if (KeyHandler.rollCenter.isDown())
                        resetRoll();*/
                    
                    while (KeyHandler.pointKey.consumeClick()) {
                        if (!CinematicToolsClient.isOp)
                            MC.player.sendSystemMessage(Component.translatable("commands.permission.failure"));
                        CamPoint point = CamPoint.createLocal();
                        if (CinematicToolsClient.getScene().posTarget != null) {
                            Vec3d vec = CinematicToolsClient.getTargetMarker();
                            if (vec == null) {
                                MC.player.sendSystemMessage(Component.translatable("scene.follow.no_marker", CinematicToolsClient.getPoints().size()));
                                continue;
                            }
                            point.sub(vec);
                        }
                        CinematicToolsClient.getPoints().add(point);
                        MC.player.sendSystemMessage(Component.translatable("scene.add", CinematicToolsClient.getPoints().size()));
                    }
                }
                
                if (KeyHandler.startStop.consumeClick()) {
                    if (CinematicToolsClient.isPlaying())
                        CinematicToolsClient.stop();
                    else
                        try {
                            if (!CinematicToolsClient.isOp)
                                MC.player.sendSystemMessage(Component.translatable("commands.permission.failure"));
                            CinematicToolsClient.start(CinematicToolsClient.createScene());
                        } catch (SceneException e) {
                            MC.player.sendSystemMessage(Component.translatable(e.getMessage()));
                        }
                }
                
                while (KeyHandler.clearPoint.consumeClick()) {
                    if (!CinematicToolsClient.isOp)
                        MC.player.sendSystemMessage(Component.translatable("commands.permission.failure"));
                    CinematicToolsClient.getPoints().clear();
                    MC.player.sendSystemMessage(Component.translatable("scene.clear"));
                }
            }
        }
    }

    public double fov(GameRenderer renderer, Camera camera, double partialTicks, boolean usedFovSetting, double fov) {
        if (skipFov)
            return fov;

        var newFov = fov;

        if (!renderingHand) {
            if (CinematicToolsClient.isPlaying())
                newFov = fov;
            else
                newFov = fov + CamEventHandlerClient.fov;

            newFov = (Mth.clamp(newFov, MIN_FOV, MAX_FOV));
        }
        renderingHand = !renderingHand;

        return newFov;
    }

    public void worldRender(WorldRenderContext context) {
        if (CinematicToolsClient.isPlaying())
            return;
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        
        Vec3 view = MC.gameRenderer.getMainCamera().getPosition();
        
        RenderSystem.setProjectionMatrix(context.projectionMatrix(), VertexSorting.ORTHOGRAPHIC_Z);
        PoseStack mat = RenderSystem.getModelViewStack();
        mat.pushPose();
        mat.setIdentity();
        mat.mulPoseMatrix(context.matrixStack().last().pose());
        mat.translate(-view.x(), -view.y(), -view.z());
        
        RenderSystem.applyModelViewMatrix();
        
        RenderSystem.depthMask(false);
        
        if (CinematicToolsClient.hasTargetMarker()) {
            CamPoint point = CinematicToolsClient.getTargetMarker();
            renderHitbox(mat, MC.renderBuffers().bufferSource().getBuffer(RenderType.lines()),
                new AABB(point.x - 0.3, point.y - 1.62, point.z - 0.3, point.x + 0.3, point.y + 0.18, point.z + 0.3), MC.player.getEyeHeight(), point, point.calculateViewVector());
        }
        
        boolean shouldRender = false;
        for (CamInterpolation movement : CamInterpolation.REGISTRY.values())
            if (movement.isRenderingEnabled) {
                shouldRender = true;
                break;
            }
        
        PoseStack pose = new PoseStack();
        
        if (shouldRender && CinematicToolsClient.getPoints().size() > 0) {
            for (int i = 0; i < CinematicToolsClient.getPoints().size(); i++) {
                CamPoint point = CinematicToolsClient.getPoints().get(i);
                if (CinematicToolsClient.hasTargetMarker()) {
                    point = point.copy();
                    point.add(CinematicToolsClient.getTargetMarker());
                }
                
                DebugRenderer.renderFilledBox(pose, MC.renderBuffers().bufferSource(), point.x - 0.05, point.y - 0.05, point.z - 0.05, point.x + 0.05, point.y + 0.05,
                    point.z + 0.05, 1, 1, 1, 1);
                DebugRenderer.renderFloatingText(pose, MC.renderBuffers().bufferSource(), (i + 1) + "", point.x + view.x, point.y + 0.2 + view.y, point.z + view.z, -1);
                
                RenderSystem.depthMask(false);
            }
            
            MC.renderBuffers().bufferSource().endLastBatch();
            
            try {
                mat.pushPose();
                //if (CMDCamClient.hasTargetMarker())
                //mat.translate(CMDCamClient.getTargetMarker().x, CMDCamClient.getTargetMarker().y, CMDCamClient.getTargetMarker().z);
                CamScene scene = CinematicToolsClient.createScene();
                for (CamInterpolation movement : CamInterpolation.REGISTRY.values())
                    if (movement.isRenderingEnabled)
                        renderPath(pose, movement, scene);
                    
                mat.popPose();
            } catch (SceneException e) {}
            
        }
        
        mat.popPose();
        
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        
    }
    
    public void renderPath(PoseStack mat, CamInterpolation inter, CamScene scene) {
        double steps = 20 * (scene.points.size() - 1);
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        
        RenderSystem.lineWidth(1.0F);
        Vec3d color = inter.color.toVec();
        bufferbuilder.begin(Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        CamPoints points = new CamPoints(scene.points);
        
        if (scene.lookTarget != null)
            scene.lookTarget.start(MC.level);
        if (scene.posTarget != null)
            scene.posTarget.start(MC.level);
        
        double[] times = points.createTimes(scene);
        Interpolation<Vec3d> interpolation = inter.create(times, scene, null, new ArrayList<Vec3d>(scene.points), null, CamAttribute.POSITION);
        for (int i = 0; i < steps; i++) {
            Vec3d pos = interpolation.valueAt(i / steps);
            if (CinematicToolsClient.hasTargetMarker())
                pos.add(CinematicToolsClient.getTargetMarker());
            bufferbuilder.vertex((float) pos.x, (float) pos.y, (float) pos.z).color((float) color.x, (float) color.y, (float) color.z, 1).endVertex();
        }
        Vec3d last = scene.points.get(scene.points.size() - 1).copy();
        if (CinematicToolsClient.hasTargetMarker())
            last.add(CinematicToolsClient.getTargetMarker());
        bufferbuilder.vertex((float) last.x, (float) last.y, (float) last.z).color((float) color.x, (float) color.y, (float) color.z, 1).endVertex();
        
        tessellator.end();
        
        if (scene.lookTarget != null)
            scene.lookTarget.finish();
        if (scene.posTarget != null)
            scene.posTarget.finish();
    }
    
    private static void renderHitbox(PoseStack pMatrixStack, VertexConsumer pBuffer, AABB aabb, float eyeHeight, Vec3d origin, Vec3d view) {
        LevelRenderer.renderLineBox(pMatrixStack, pBuffer, aabb, 1.0F, 1.0F, 1.0F, 1.0F);
        
        float f = 0.01F;
        LevelRenderer.renderLineBox(pMatrixStack, pBuffer, aabb.minX, aabb.minY + (eyeHeight - f), aabb.minZ, aabb.maxX, aabb.minY + (eyeHeight + f), aabb.maxZ, 1.0F, 0.0F, 0.0F,
            1.0F);
        
        Matrix4f matrix4f = pMatrixStack.last().pose();
        Matrix3f matrix3f = pMatrixStack.last().normal();
        pBuffer.vertex(matrix4f, (float) origin.x, (float) origin.y, (float) origin.z).color(0, 0, 255, 255).normal(matrix3f, (float) view.x, (float) view.y, (float) view.z)
                .endVertex();
        pBuffer.vertex(matrix4f, (float) (origin.x + view.x * 2), (float) (origin.y + view.y * 2), (float) (origin.z + view.z * 2)).color(0, 0, 255, 255).normal(matrix3f,
            (float) view.x, (float) view.y, (float) view.z).endVertex();
    }

    public boolean cameraRoll(CameraSetupCallback.CameraInfo event) {
        event.roll = (roll);
        return false;
    }

    public InteractionResult onPlayerUseEntity(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if (hitResult == null)
            return InteractionResult.PASS;

        interact(player, world, null, hitResult.getEntity());
        return InteractionResult.PASS;
    }

    public InteractionResult onPlayerUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        interact(player, world, hitResult.getBlockPos(), null);
        return InteractionResult.PASS;
    }

    public void interact(Player player, Level level, @Nullable BlockPos pos, @Nullable Entity target) {
        if (selectingTarget == null || !level.isClientSide)
            return;
        
        if (target != null) {
            selectingTarget.accept(new CamTarget.EntityTarget((target)));
            player.sendSystemMessage(Component.translatable("scene.look.target.entity", target.getStringUUID()));
            selectingTarget = null;
        }
        
        if (pos != null) {
            selectingTarget.accept(new CamTarget.BlockTarget(pos));
            player.sendSystemMessage(Component.translatable("scene.look.target.pos", pos.toShortString()));
            selectingTarget = null;
        }
    }
    
    public static void setupMouseHandlerBefore() {
        if (CinematicToolsClient.isPlaying() && CinematicToolsClient.getScene().mode instanceof OutsideMode) {
            camera = MC.cameraEntity;
            MC.cameraEntity = MC.player;
        }
    }
    
    public static void setupMouseHandlerAfter() {
        if (CinematicToolsClient.isPlaying() && CinematicToolsClient.getScene().mode instanceof OutsideMode) {
            MC.cameraEntity = camera;
            camera = null;
        }
    }
}
