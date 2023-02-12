package com.trainguy9512.animationoverhaul.animation.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.animationoverhaul.access.ModelAccess;
import com.trainguy9512.animationoverhaul.animation.AnimatorDispatcher;
import com.trainguy9512.animationoverhaul.animation.pose.AnimationPose;
import com.trainguy9512.animationoverhaul.animation.pose.BakedAnimationPose;
import com.trainguy9512.animationoverhaul.animation.pose.sample.*;
import com.trainguy9512.animationoverhaul.util.animation.LocatorSkeleton;
import com.trainguy9512.animationoverhaul.util.data.AnimationDataContainer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

import java.util.Random;

public abstract class LivingEntityAnimator<T extends LivingEntity, M extends EntityModel<T>> {

    protected T livingEntity;
    protected M entityModel;
    protected final LocatorSkeleton locatorSkeleton;

    protected AnimationDataContainer entityAnimationData;
    protected final Random random = new Random();

    public LivingEntityAnimator(){
        this.locatorSkeleton = new LocatorSkeleton();
        buildRig(this.locatorSkeleton);
    }

    public void setEntity(T livingEntity){
        this.livingEntity = livingEntity;
    }

    public void setEntityModel(M entityModel){
        this.entityModel = entityModel;
    }

    protected void buildRig(LocatorSkeleton locatorRig){

    }

    public void tick(LivingEntity livingEntity, AnimationDataContainer entityAnimationData){

    }

    protected AnimationPose calculatePose(){
        return null;
    }

    protected void finalizeModelParts(ModelPart rootModelPart){
    }

    protected AnimationDataContainer getEntityAnimationData(){
        return this.entityAnimationData;
    }

    protected <D> AnimationDataContainer.Variable<D> getEntityAnimationVariableObject(AnimationDataContainer.DataKey<D> dataKey){
        return getEntityAnimationData().get(dataKey);
    }

    protected <D> D getEntityAnimationVariable(AnimationDataContainer.DataKey<D> dataKey){
        return getEntityAnimationVariableObject(dataKey).get();
    }

    protected <D> void setEntityAnimationVariable(AnimationDataContainer.DataKey<D> dataKey, D value){
        getEntityAnimationData().setValue(dataKey, value);
    }

    protected AnimationPose sampleAnimationState(SampleableAnimationState sampleableAnimationState){
        return getEntityAnimationData().sampleAnimationState(this.locatorSkeleton, sampleableAnimationState);
    }

    protected AnimationPose sampleAnimationStateFromInputPose(SampleableAnimationState sampleableAnimationState, AnimationPose inputPose){
        return getEntityAnimationData().sampleAnimationStateFromInputPose(inputPose, this.locatorSkeleton, sampleableAnimationState);
    }


    protected AnimationSequencePlayer getAnimationSequencePlayer(AnimationSequencePlayer animationSequencePlayer){
        return getEntityAnimationData().getAnimationSequencePlayer(animationSequencePlayer);
    }

    protected AnimationBlendSpacePlayer getAnimationBlendSpacePlayer(AnimationBlendSpacePlayer animationBlendSpacePlayer){
        return getEntityAnimationData().getAnimationBlendSpacePlayer(animationBlendSpacePlayer);
    }

    protected AnimationStateMachine getAnimationStateMachine(AnimationStateMachine animationStateMachine){
        return getEntityAnimationData().getAnimationStateMachine(animationStateMachine);
    }

    protected AnimationMontageTrack getAnimationMontageTrack(AnimationMontageTrack animationMontageTrack){
        return getEntityAnimationData().getAnimationMontageTrack(animationMontageTrack);
    }

    public void tick(LivingEntity livingEntity){
        BakedAnimationPose bakedPose = AnimatorDispatcher.INSTANCE.getBakedPose(livingEntity.getUUID());
        AnimationDataContainer entityAnimationData = AnimatorDispatcher.INSTANCE.getEntityAnimationData(livingEntity.getUUID());
        this.entityAnimationData = entityAnimationData;
        this.setEntity((T)livingEntity);
        //this.livingEntity = (T)livingEntity;

        this.tick(livingEntity, entityAnimationData);
        getEntityAnimationData().tickAnimationStates();

        if(bakedPose == null){
            bakedPose = new BakedAnimationPose();
        }
        if(!bakedPose.hasPose){
            bakedPose.setPose(new AnimationPose(this.locatorSkeleton));
            bakedPose.hasPose = true;
        }
        bakedPose.pushToOld();

        //this.locatorRig.resetRig();
        AnimationPose animationPose = this.calculatePose();
        if (animationPose == null){
            animationPose = new AnimationPose(this.locatorSkeleton);
        }
        animationPose.applyDefaultPoseOffset();

        bakedPose.setPose(animationPose.getCopy());
        AnimatorDispatcher.INSTANCE.saveBakedPose(livingEntity.getUUID(), bakedPose);
    }

    public void applyBakedPose(T livingEntity, M entityModel, PoseStack poseStack, AnimationDataContainer entityAnimationData, float partialTicks){
        setEntity(livingEntity);
        setEntityModel(entityModel);

        BakedAnimationPose bakedPose = AnimatorDispatcher.INSTANCE.getBakedPose(livingEntity.getUUID());

        ModelPart rootModelPart = getRoot(entityModel);
        bakedPose.bakeToModelParts(rootModelPart, partialTicks);
        finalizeModelParts(rootModelPart);
    }

    protected ModelPart getRoot(M entityModel){
        return ((ModelAccess)entityModel).getRootModelPart();
    }
}
