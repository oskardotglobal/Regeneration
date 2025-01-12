package me.suff.mc.regen.client.rendering.types;

import me.suff.mc.regen.client.animation.AnimationContext;
import me.suff.mc.regen.client.animation.AnimationHandler;
import me.suff.mc.regen.client.animation.RenderCallbackEvent;
import me.suff.mc.regen.common.capability.IRegeneration;
import me.suff.mc.regen.common.types.TypeLayFade;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.client.event.RenderPlayerEvent;

public class TypeLayFadeRenderer extends ATypeRenderer<TypeLayFade> {

    public static final TypeLayFadeRenderer INSTANCE = new TypeLayFadeRenderer();

    private TypeLayFadeRenderer() {
    }

    @Override
    protected void onRenderRegeneratingPre(TypeLayFade type, RenderPlayerEvent.Pre event, IRegeneration capability) {

    }

    @Override
    protected void onRenderRegeneratingPost(TypeLayFade type, RenderPlayerEvent.Post event, IRegeneration capability) {

    }

    @Override
    protected void onRenderLayer(TypeLayFade type, RenderLivingBase<?> renderLivingBase, IRegeneration capability, EntityPlayer entityPlayer, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        TypeFieryRenderer.renderOverlay(entityPlayer, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, null);
    }

    @Override
    public boolean onAnimateRegen(AnimationContext animationContext) {
        ModelBiped modelBiped = animationContext.getModelBiped();

        modelBiped.bipedHead.rotateAngleX = (float) Math.toRadians(0);
        modelBiped.bipedHead.rotateAngleY = (float) Math.toRadians(0);
        modelBiped.bipedHead.rotateAngleZ = (float) Math.toRadians(0);

        modelBiped.bipedLeftLeg.rotateAngleZ = (float) -Math.toRadians(5);
        modelBiped.bipedRightLeg.rotateAngleZ = (float) Math.toRadians(5);

        modelBiped.bipedLeftArm.rotateAngleZ = (float) -Math.toRadians(5);
        modelBiped.bipedRightArm.rotateAngleZ = (float) Math.toRadians(5);
        return AnimationHandler.copyAndReturn(modelBiped, true);
    }

    @Override
    public void renderHand(EntityPlayer player, EnumHandSide handSide, RenderLivingBase<?> render) {

    }

    @Override
    public void onRenderCallBack(RenderCallbackEvent event) {
        GlStateManager.rotate(-90, 1, 0, 0);
        GlStateManager.translate(0, 1, 0);
    }

}
