package com.lcm.regeneration.superpower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.lcm.regeneration.RegenerationMod;
import com.lcm.regeneration.traits.negative.INegativeTrait;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.karma.KarmaHandler;
import lucraft.mods.lucraftcore.karma.KarmaStat;
import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.SuperpowerPlayerHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.capabilities.CapabilitySuperpower;
import lucraft.mods.lucraftcore.superpowers.capabilities.ISuperpowerCapability;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.block.BlockFire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Created by AFlyingGrayson on 8/7/17 */
@Mod.EventBusSubscriber
public class TimelordSuperpowerHandler extends SuperpowerPlayerHandler {
	public int regenerationsLeft, timesRegenerated, regenTicks;
	public boolean regenerating = false, positionSet = false;
	public double pX, pY, pZ;
	
	public final String[] LOCKED_KEYS = { //TODO configurable
		"forward", "left", "right", "back", "jump", "sneak", "drop", "attack", "inventory", "sprint", "swapHands", "togglePerspective", "useItem"
	};
	public HashMap<KeyBinding, Integer> keylockMap = new HashMap<>();
	public float prevMouseSensitivity;
	
	public TimelordSuperpowerHandler(ISuperpowerCapability cap, Superpower superpower) { super(cap, superpower); }
	
	@Override
	public void onUpdate(TickEvent.Phase phase) {
		if (phase.equals(TickEvent.Phase.END)) return;
		
		EntityPlayer player = cap.getPlayer();
		if (!player.world.isRemote) {
			// Server Behavior
			if (regenTicks > 0 && regenTicks < 200) { //regenerating
				regenTicks++;
				player.extinguish();
				player.setArrowCountInEntity(0);
				
				if (regenTicks > 100) { //explosion phase
					if (player.world.getBlockState(player.getPosition()).getBlock() instanceof BlockFire) player.world.setBlockToAir(player.getPosition());
					
					double x = player.posX + player.getRNG().nextGaussian() * 2;
					double y = player.posY + 0.5 + player.getRNG().nextGaussian() * 2;
					double z = player.posZ + player.getRNG().nextGaussian() * 2;
					
					player.world.newExplosion(player, x, y, z, 1, true, false);
					for (BlockPos bs : BlockPos.getAllInBox(player.getPosition().north().west(), player.getPosition().south().east())) {
						if (player.world.getBlockState(bs).getBlock() instanceof BlockFire) player.world.setBlockToAir(bs);
					}
				}
			} else if (regenTicks >= 200) {
				player.setHealth(player.getMaxHealth());
				player.addPotionEffect(new PotionEffect(Potion.getPotionById(10), 180*20, 3, false, false)); //180 seconds of 20 ticks of Regeneration 4

				regenerating = false;
				regenTicks = 0;
				regenerationsLeft--;
				timesRegenerated++;
				TimelordSuperpowerHandler.randomizeTraits(this);
				cap.syncToAll();
			} else if (regenTicks == 0 && regenerating) regenTicks = 1;
		} else {
			// Client Behavior
			GameSettings settings = Minecraft.getMinecraft().gameSettings;
			
			if (regenTicks == 0 && regenerating) {
				regenTicks = 1;
				
				player.setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw, 0);
				prevMouseSensitivity = settings.mouseSensitivity;
				settings.mouseSensitivity = -(1F/3F);
				
				for (String key : LOCKED_KEYS) try {
					Class<? extends GameSettings> setCls = settings.getClass();
					key = key.substring(0, 1).toUpperCase() + key.substring(1);
					
					KeyBinding cBind = (KeyBinding)setCls.getField("keyBind"+key).get(settings);
					keylockMap.put(cBind, cBind.getKeyCode());
					cBind.setKeyCode(0);
				} catch (NoSuchFieldException ex) { //TODO when configurable, delete offending key from array to prevent log spam (also config?) TODO list all possible variable names in the config file
					System.err.println("Keybinding "+key+" does not exist!");
					continue;
				} catch (IllegalAccessException ex) { throw new RuntimeException("Minecraft changed the name of the keybinding variables", ex); }
				KeyBinding.resetKeyBindingArrayAndHash();
				KeyBinding.unPressAllKeys();
				KeyBinding.updateKeyBindState();
				settings.saveOptions();
			}
			
			if (regenerating && positionSet == false) {
				pX = player.posX;
				pY = player.posY;
				pZ = player.posZ;
				positionSet = true;
			}
			
			if (regenTicks > 0) {
				if (Minecraft.getMinecraft().player.getUniqueID() == player.getUniqueID()) Minecraft.getMinecraft().gameSettings.thirdPersonView = 2;
				regenTicks++;
				
				if (pY > player.posY) pY = player.posY;
				player.setLocationAndAngles(pX, pY, pZ, player.rotationYaw, player.rotationPitch);
			}
			
			if (regenTicks >= 200 && !regenerating) {
				if (Minecraft.getMinecraft().player.getUniqueID() == player.getUniqueID()) Minecraft.getMinecraft().gameSettings.thirdPersonView = 0;
				regenTicks = 0;
				
				keylockMap.forEach((key,oCode)->{
					key.setKeyCode(oCode);
				});
				keylockMap.clear();
				
				settings.mouseSensitivity = prevMouseSensitivity;
				prevMouseSensitivity = 0;
				
				KeyBinding.resetKeyBindingArrayAndHash();
				KeyBinding.unPressAllKeys();
				KeyBinding.updateKeyBindState();
				settings.saveOptions();
				
				pX = -1;
				pY = -1;
				pZ = -1;
				positionSet = false;
			}
		}
	}
	
	@Override
	public void onApplyPower() {
		this.getAbilities().clear();
		TimelordSuperpower.INSTANCE.addDefaultAbilities(this.getPlayer(), this.getAbilities());
		TimelordSuperpowerHandler.randomizeTraits(this);
		this.regenerationsLeft = 12;
		SuperpowerHandler.syncToAll(this.getPlayer());
	}
	
	protected static void randomizeTraits(SuperpowerPlayerHandler handler) {
		// Reset Karma
		if (LCConfig.modules.karma) for (KarmaStat karmaStat : KarmaStat.getKarmaStats())
			KarmaHandler.setKarmaStat(handler.getPlayer(), karmaStat, 0);
		if (RegenerationMod.getConfig().disableTraits) return;
		
		handler.getAbilities().forEach(ability -> ability.setUnlocked(false));
		
		for (int i = 0; i < 2; i++) {
			Ability a = null;
			while (a == null || a instanceof INegativeTrait || a.isUnlocked())
				a = handler.getAbilities().get(handler.getPlayer().getRNG().nextInt(handler.getAbilities().size()));
			a.setUnlocked(true);
		}
		
		for (int i = 0; i < 2; i++) {
			Ability a = null;
			while (a == null || a.isUnlocked() || !(a instanceof INegativeTrait) || TimelordSuperpowerHandler.isAbilityUnlocked(handler, ((INegativeTrait) a).getPositiveTrait()))
				a = handler.getAbilities().get(handler.getPlayer().getRNG().nextInt(handler.getAbilities().size()));
			a.setUnlocked(true);
		}
		
		String s = "";
		for (Ability ability : handler.getAbilities()) {
			if (ability.isUnlocked()) {
				if (s.equals(""))
					s = ability.getDisplayName().substring(7);
				else
					s = s + ", " + ability.getDisplayName().substring(7);
			}
		}
		handler.getPlayer().sendStatusMessage(new TextComponentString(StringHelper.translateToLocal("lcm-regen.messages.newLife", s)), true);
	}
	
	protected static boolean isAbilityUnlocked(SuperpowerPlayerHandler handler, Class<? extends Ability> ability) {
		for (Ability ability1 : handler.getAbilities())
			if (ability.equals(ability1.getClass())) return ability1.isUnlocked();
		return false;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		compound.setInteger("regenerationsLeft", regenerationsLeft);
		compound.setInteger("timesRegenerated", timesRegenerated);
		compound.setBoolean("regenerating", regenerating);
		return compound;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		regenerationsLeft = compound.getInteger("regenerationsLeft");
		timesRegenerated = compound.getInteger("timesRegenerated");
		regenerating = compound.getBoolean("regenerating");
		
		ArrayList<Ability> abilities = new ArrayList<>();
		abilities.addAll(getAbilities());
		abilities.removeAll(getAbilities().stream().filter(ability -> !ability.isUnlocked()).collect(Collectors.toCollection(ArrayList::new)));
		
		this.getAbilities().clear();
		((CapabilitySuperpower) getPlayer().getCapability(CapabilitySuperpower.SUPERPOWER_CAP, null)).superpowerData.getCompoundTag(TimelordSuperpower.INSTANCE.getRegistryName().toString()).removeTag("Abilities");
		
		this.getAbilities().addAll(abilities);
	}
}
