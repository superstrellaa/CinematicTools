package studio.dolphinproductions.utils.cinematictools.common.entity;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import studio.dolphinproductions.utils.cinematictools.CinematicTools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class NPCEntity extends PathfinderMob implements GeoEntity {
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("sprint");
    protected static final RawAnimation JUMP_ANIM = RawAnimation.begin().thenPlay("jump");
    protected static final RawAnimation HANDSBACK_ANIM = RawAnimation.begin().thenLoop("hands_back");
    protected static final RawAnimation TALK_ANIM = RawAnimation.begin().thenLoop("speak");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> IS_RUNNING = SynchedEntityData.defineId(NPCEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_JUMPING = SynchedEntityData.defineId(NPCEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> NAME = SynchedEntityData.defineId(NPCEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> SKIN_URL = SynchedEntityData.defineId(NPCEntity.class, EntityDataSerializers.STRING);

    private int jumpTicks = 0;

    private int animationTicks = 0;
    private boolean animationTriggered = false;
    private boolean isAnimationPaused = false;
    private String lastAnimation = null;

    private ResourceLocation skinTexture;

    public NPCEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.setPersistenceRequired();
    }

    public void setName(String name) {
        this.entityData.set(NAME, name);
    }

    public void setSkinURL(String url) {
        this.entityData.set(SKIN_URL, url);
    }

    public String getSkinURL() {
        return this.entityData.get(SKIN_URL);
    }

    public String getNPCName() {
        return this.entityData.get(NAME);
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        CinematicTools.npcMap.remove(getNPCName());
        if (this.level().isClientSide) {
            this.skinTexture = null;
        }
    }

    public ResourceLocation getSkinTexture() {
        if ("_SUIT".equals(this.getNPCName())) {
            return new ResourceLocation("cinematictools", "textures/entity/npc.suit.png");
        }
        return this.skinTexture;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
    }

    @Override
    public boolean isEffectiveAi() {
        return this.level() != null && !this.level().isClientSide && this.level().getServer() != null;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (this.level().isClientSide && key.equals(SKIN_URL)) {
            this.skinTexture = null;
            this.downloadAndApplySkin();
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(NAME, "");
        this.entityData.define(SKIN_URL, "");
        this.entityData.define(IS_RUNNING, false);
        this.entityData.define(IS_JUMPING, false);
    }

    public void setRunning(boolean running) {
        this.entityData.set(IS_RUNNING, running);
    }

    public boolean isRunning() {
        return this.entityData.get(IS_RUNNING);
    }

    public void setJumping(boolean jumping) {
        if (jumping && !this.isJumping()) {
            if (!animationTriggered) {
                this.triggerAnim("Movement", "jump");
                animationTriggered = true;
                animationTicks = 10;
            }
        }
    }

    public void playAnimation(String animationName) {
        this.lastAnimation = animationName;
        this.isAnimationPaused = false;
        this.triggerAnim("Movement", animationName);
    }

    public void stopAnimation() {
        this.isAnimationPaused = true;
    }

    public void resumeAnimation() {
        this.isAnimationPaused = false;
        if (this.lastAnimation != null) {
            this.triggerAnim("Movement", this.lastAnimation);
        }
    }

    public void downloadAndApplySkin() {
        if (!this.level().isClientSide || this.skinTexture != null) return;

        String skinURL = getSkinURL();
        if (skinURL == null || skinURL.isEmpty()) return;

        CompletableFuture.runAsync(() -> {
            try (InputStream inputStream = new URL(skinURL).openStream()) {
                NativeImage skinImage = NativeImage.read(inputStream);
                DynamicTexture dynamicTexture = new DynamicTexture(skinImage);

                Minecraft.getInstance().execute(() -> {
                    ResourceLocation textureLocation = Minecraft.getInstance()
                            .getTextureManager()
                            .register("npc_skin_" + getId(), dynamicTexture);
                    this.skinTexture = textureLocation;
                });

            } catch (IOException e) {
                System.err.println("[NPCEntity] Failed to load skin from URL: " + skinURL);
                e.printStackTrace();
            }
        });
    }


    public boolean isJumping() {
        return this.entityData.get(IS_JUMPING);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "Movement", 10, this::handleAnimations)
                        .triggerableAnim("jump", JUMP_ANIM)
                        .triggerableAnim("walk", WALK_ANIM)
                        .triggerableAnim("run", RUN_ANIM)
                        .triggerableAnim("hands_back", HANDSBACK_ANIM)
                        .triggerableAnim("talk", TALK_ANIM)
        );
    }

    private <T extends GeoAnimatable> PlayState handleAnimations(AnimationState<T> state) {
        if (this.isAnimationPaused) {
            return PlayState.STOP;
        }

        if (this.isJumping()) {
            return state.setAndContinue(JUMP_ANIM);
        }

        if (state.isMoving()) {
            if (this.isRunning()) {
                return state.setAndContinue(RUN_ANIM);
            } else {
                return state.setAndContinue(WALK_ANIM);
            }
        }

        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getNavigation().isInProgress()) {
            this.getNavigation().tick();
        }

        if (this.isJumping()) {
            jumpTicks--;
            if (jumpTicks <= 0) {
                if (this.onGround()) {
                    this.setJumping(false);
                } else {
                    jumpTicks = 2;
                }
            }
        }

        if (!this.level().isClientSide) {
            this.entityData.set(IS_JUMPING, this.jumpTicks > 0);
        }

        if (this.level().isClientSide && this.skinTexture == null) {
            this.downloadAndApplySkin();
        }

        if (animationTriggered) {
            if (animationTicks > 0) {
                animationTicks--;
            } else {
                if (!this.isJumping()) {
                    this.entityData.set(IS_JUMPING, true);
                    this.jumpTicks = 10;
                    this.setDeltaMovement(this.getDeltaMovement().x, 0.5, this.getDeltaMovement().z);
                    this.hasImpulse = true;
                }
                animationTriggered = false;
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
