package me.suff.mc.regen.common.types;

import me.suff.mc.regen.client.rendering.types.TypeLayFadeRenderer;
import me.suff.mc.regen.common.capability.IRegeneration;
import me.suff.mc.regen.handlers.RegenObjects;
import me.suff.mc.regen.util.PlayerUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;

public class TypeLayFade implements IRegenType<TypeLayFadeRenderer> {

    private final SoundEvent[] soundEvents = new SoundEvent[]{RegenObjects.Sounds.HAND_GLOW};

    @Override
    public int getAnimationLength() {
        return 350;
    }

    @Override
    public TypeLayFadeRenderer getRenderer() {
        return TypeLayFadeRenderer.INSTANCE;
    }

    @Override
    public void onStartRegeneration(EntityPlayer player, IRegeneration capability) {

    }

    @Override
    public void onUpdateMidRegen(EntityPlayer player, IRegeneration capability) {
        if (!player.world.isRemote) {
            PlayerUtil.setPerspective((EntityPlayerMP) player, true, false);
        }
    }

    @Override
    public void onFinishRegeneration(EntityPlayer player, IRegeneration capability) {
        PlayerUtil.setPerspective((EntityPlayerMP) player, false, true);
    }

    @Override
    public double getAnimationProgress(IRegeneration cap) {
        return Math.min(1, cap.getAnimationTicks() / (double) getAnimationLength());
    }

    @Override
    public TypeHandler.RegenType getTypeID() {
        return TypeHandler.RegenType.LAY_FADE;
    }

    @Override
    public SoundEvent[] getRegeneratingSounds() {
        return soundEvents;
    }

    @Override
    public Vec3d getDefaultPrimaryColor() {
        return new Vec3d(1, 1, 1);
    }

    @Override
    public Vec3d getDefaultSecondaryColor() {
        return new Vec3d(1, 1, 1);
    }

}
