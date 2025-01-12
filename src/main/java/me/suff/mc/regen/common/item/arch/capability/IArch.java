package me.suff.mc.regen.common.item.arch.capability;

import me.suff.mc.regen.client.skinhandling.SkinInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Created by Swirtzly on 29/02/2020 @ 22:27
 */
public interface IArch extends INBTSerializable<NBTTagCompound> {

    int getRegenAmount();

    void setRegenAmount(int regenAmount);

    ResourceLocation getSavedTrait();

    void setSavedTrait(ResourceLocation savedTrait);

    ArchStatus getArchStatus();

    void setArchStatus(ArchStatus status);

    SkinInfo.SkinType getSkinType();

    void setSkinType(SkinInfo.SkinType skinType);

    String getSkin();

    void setSkin(String encoded);

    enum ArchStatus {
        ARCH_ITEM, NORMAL_ITEM
    }
}
