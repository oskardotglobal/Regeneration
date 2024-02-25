package me.suff.mc.regen.common.capability;

import me.suff.mc.regen.RegenConfig;
import me.suff.mc.regen.client.skinhandling.SkinChangingHandler;
import me.suff.mc.regen.client.skinhandling.SkinInfo;
import me.suff.mc.regen.common.types.TypeHandler;
import me.suff.mc.regen.util.PlayerUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Created by Sub on 16/09/2018.
 */
public interface IRegeneration extends INBTSerializable<NBTTagCompound> {

    EntityPlayer getPlayer();

    int getRegenerationsLeft();

    /**
     * Only for debug purposes!
     */
    @Deprecated
    void setRegenerationsLeft(int amount);

    void triggerRegeneration();

    void tick();

    void synchronise();

    NBTTagCompound getStyle();

    void setStyle(NBTTagCompound nbt);

    Vec3d getPrimaryColor();

    Vec3d getSecondaryColor();

    /**
     * Returns if the player is currently <i>able to</i> regenerate
     */
    default boolean canRegenerate() {
        return (RegenConfig.infiniteRegeneration || getRegenerationsLeft() > 0) && getPlayer().posY > 0;
    }

    void receiveRegenerations(int amount);

    void extractRegeneration(int amount);

    PlayerUtil.RegenState getState();

    TypeHandler.RegenType getType();

    void setType(TypeHandler.RegenType type);

    IRegenerationStateManager getStateManager();

    String getEncodedSkin();

    void setEncodedSkin(String string);

    SkinInfo.SkinType getSkinType();

    void setSkinType(String skinType);

    SkinChangingHandler.EnumChoices getPreferredModel();

    void setPreferredModel(String skinType);

    boolean areHandsGlowing();

    String getDeathSource();

    void setDeathSource(String source);

    ResourceLocation getDnaType();

    void setDnaType(ResourceLocation resgitryName);

    boolean isDnaActive();

    void setDnaActive(boolean alive);

    int getAnimationTicks();

    void setAnimationTicks(int ticks);

    void setSyncingFromJar(boolean syncing);

    boolean isSyncingToJar();

    String getNextSkin();

    void setNextSkin(String base64);

    SkinInfo.SkinType getNextSkinType();

    void setNextSkinType(SkinInfo.SkinType choices);

    boolean hasDroppedHand();

    void setDroppedHand(boolean droppedHand);

    EnumHandSide getCutoffHand();

    void setCutOffHand(EnumHandSide side);

    float getProgress();

    void setProgress(float progress);
}
