package me.sub.common.capability;

import me.sub.common.states.EnumRegenType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Created by Sub
 * on 16/09/2018.
 */
public interface IRegeneration extends INBTSerializable<NBTTagCompound> {


    void update();

    //Regen Ticks
    int getTicksRegenerating();
    void setTicksRegenerating(int ticks);

    //Returns the player
    EntityPlayer getPlayer();

    //Lives left
    int getLivesLeft();
    void setLivesLeft(int left);

    //Times IN TOTAL, NOT PER CYCLE
    int getTimesRegenerated();
    void setTimesRegenerated(int times);

    //TODO handle style stuff
    NBTTagCompound getStyle();
    void setStyle(NBTTagCompound nbt);

    //Sync to clients
    void sync();

    //The type of Regeneration in use
    EnumRegenType getType();

    //Does the player have the ability to regenerate?
    boolean isCapable();
    void setCapable(boolean capable);

    //Regen
    boolean isRegenerating();
    void setRegenerating(boolean regenerating);

    //Grace stuff
    boolean isGlowing();

    void setGlowing(boolean glowing);

    boolean isInGracePeriod();

    void setInGracePeriod(boolean gracePeriod);

    //Solace ticks
    int getSolaceTicks();

    void setSolaceTicks(int ticks);

}