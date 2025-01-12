package me.suff.mc.regen.common.traits.positive;

import me.suff.mc.regen.RegenerationMod;
import me.suff.mc.regen.common.capability.IRegeneration;
import me.suff.mc.regen.common.traits.DnaHandler;
import me.suff.mc.regen.util.PlayerUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Suffril on 25/01/2019.
 */
public class DnaSwimmer extends DnaHandler.IDna {

    private ResourceLocation ID = new ResourceLocation(RegenerationMod.MODID, "swimmer");

    public DnaSwimmer() {
        super("swimmer");
    }

    @Override
    public void onUpdate(IRegeneration cap) {
        EntityPlayer player = cap.getPlayer();
        if (player.isInWater()) {
            PlayerUtil.applyPotionIfAbsent(player, MobEffects.WATER_BREATHING, 100, 1, true, false);
        }
    }

    @Override
    public void onAdded(IRegeneration cap) {

    }

    @Override
    public void onRemoved(IRegeneration cap) {
        EntityPlayer player = cap.getPlayer();
        player.removeActivePotionEffect(MobEffects.WATER_BREATHING);
    }
}
