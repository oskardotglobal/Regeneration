package me.suff.mc.regen.handlers;

import com.google.common.base.Predicate;
import me.suff.mc.regen.RegenConfig;
import me.suff.mc.regen.RegenerationMod;
import me.suff.mc.regen.common.advancements.RegenTriggers;
import me.suff.mc.regen.common.capability.CapabilityRegeneration;
import me.suff.mc.regen.common.capability.IRegeneration;
import me.suff.mc.regen.common.capability.RegenerationProvider;
import me.suff.mc.regen.common.item.ItemHand;
import me.suff.mc.regen.common.traits.DnaHandler;
import me.suff.mc.regen.network.MessageRemovePlayer;
import me.suff.mc.regen.network.NetworkHandler;
import me.suff.mc.regen.util.PlayerUtil;
import me.suff.mc.regen.util.RegenUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

import static me.suff.mc.regen.util.PlayerUtil.RegenState.GRACE;
import static me.suff.mc.regen.util.PlayerUtil.RegenState.POST;

/**
 * Created by Sub on 16/09/2018.
 */
@Mod.EventBusSubscriber(modid = RegenerationMod.MODID)
public class RegenEventHandler {

    // =========== CAPABILITY HANDLING =============

    @SubscribeEvent
    public static void onPlayerUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            IRegeneration data = CapabilityRegeneration.getForPlayer(player);
            data.tick();

            if (data.hasDroppedHand() && !player.getHeldItemOffhand().isEmpty()) {
                player.dropItem(player.getHeldItemOffhand(), false);
                player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(Items.AIR));
            }
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(CapabilityRegeneration.CAP_REGEN_ID, new RegenerationProvider(new CapabilityRegeneration((EntityPlayer) event.getObject())));
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        IStorage<IRegeneration> storage = CapabilityRegeneration.CAPABILITY.getStorage();

        IRegeneration oldCap = CapabilityRegeneration.getForPlayer(event.getOriginal());
        IRegeneration newCap = CapabilityRegeneration.getForPlayer(event.getEntityPlayer());

        NBTTagCompound nbt = (NBTTagCompound) storage.writeNBT(CapabilityRegeneration.CAPABILITY, oldCap, null);
        storage.readNBT(CapabilityRegeneration.CAPABILITY, newCap, null, nbt);
        CapabilityRegeneration.getForPlayer(event.getEntityPlayer()).synchronise();
    }

    @SubscribeEvent
    public static void playerTracking(PlayerEvent.StartTracking event) {
        CapabilityRegeneration.getForPlayer(event.getEntityPlayer()).synchronise();
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!RegenConfig.firstStartGiftOnly)
            CapabilityRegeneration.getForPlayer(event.player).receiveRegenerations(RegenConfig.freeRegenerations);

        CapabilityRegeneration.getForPlayer(event.player).synchronise();
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        CapabilityRegeneration.getForPlayer(event.player).synchronise();
    }

    @SubscribeEvent
    public static void onDeathEvent(LivingDeathEvent e) {
        if (e.getEntityLiving() instanceof EntityPlayer) {
            CapabilityRegeneration.getForPlayer((EntityPlayer) e.getEntityLiving()).synchronise();
        }
    }

    // ============ USER EVENTS ==========

    @SubscribeEvent
    public static void onPunchBlock(PlayerInteractEvent.LeftClickBlock e) {
        if (e.getEntityPlayer().world.isRemote) return;
        CapabilityRegeneration.getForPlayer(e.getEntityPlayer()).getStateManager().onPunchBlock(e);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void painless(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) event.getEntity();
        IRegeneration cap = CapabilityRegeneration.getForPlayer(player);

        if (cap.getState() == PlayerUtil.RegenState.REGENERATING && RegenConfig.regenFireImmune && event.getSource().isFireDamage() || cap.getState() == PlayerUtil.RegenState.REGENERATING && event.getSource().isExplosion()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onHurt(LivingHurtEvent event) {
        Entity trueSource = event.getSource().getTrueSource();

        if (trueSource instanceof EntityPlayer && event.getEntityLiving() instanceof EntityLiving) {
            EntityPlayer player = (EntityPlayer) trueSource;
            CapabilityRegeneration.getForPlayer(player).getStateManager().onPunchEntity(event);
            return;
        }

        if (!(event.getEntity() instanceof EntityPlayer) || event.getSource() == RegenObjects.REGEN_DMG_CRITICAL || event.getSource() == RegenObjects.REGEN_DMG_KILLED)
            return;

        EntityPlayer player = (EntityPlayer) event.getEntity();
        IRegeneration cap = CapabilityRegeneration.getForPlayer(player);

        cap.setDeathSource(event.getSource().getDeathMessage(player).getUnformattedText());

        if (cap.getState() == POST && player.posY > 0) {
            if (event.getSource() == DamageSource.FALL) {
                PlayerUtil.applyPotionIfAbsent(player, MobEffects.NAUSEA, 200, 4, false, false);
                if (event.getAmount() > 8.0F) {
                    if (player.world.getGameRules().getBoolean("mobGriefing") && RegenConfig.postRegen.genGreator) {
                        RegenUtil.genCrater(player.world, player.getPosition(), 3);
                    }
                    event.setAmount(0.5F);
                    PlayerUtil.sendMessage(player, new TextComponentTranslation("regeneration.messages.fall_dmg"), true);
                    return;
                }
            } else {
                if (!player.world.isRemote) {
                    if (trueSource instanceof EntityLiving) {
                        EntityLiving living = (EntityLiving) trueSource;
                        if (RegenUtil.isSharp(living.getHeldItemMainhand()) & player.world.rand.nextBoolean() && !cap.hasDroppedHand()) {
                            ItemStack hand = new ItemStack(RegenObjects.Items.HAND);
                            ItemHand.setTextureString(hand, cap.getEncodedSkin());
                            ItemHand.setSkinType(hand, cap.getSkinType().name());
                            ItemHand.setOwner(hand, player.getUniqueID());
                            ItemHand.setTimeCreated(hand, System.currentTimeMillis());
                            ItemHand.setTrait(hand, cap.getDnaType().toString());
                            cap.setDroppedHand(true);
                            RegenTriggers.HAND.trigger((EntityPlayerMP) player);
                            if (player.getPrimaryHand() == EnumHandSide.LEFT) {
                                cap.setCutOffHand(EnumHandSide.RIGHT);
                            } else {
                                cap.setCutOffHand(EnumHandSide.LEFT);
                            }
                            InventoryHelper.spawnItemStack(player.world, player.posX, player.posY, player.posZ, hand);
                        }
                    }
                }
                event.setAmount(0.5F);
                PlayerUtil.sendMessage(player, new TextComponentTranslation("regeneration.messages.reduced_dmg"), true);
            }
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void adMortemInimicusButForGrace(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer))
            return;
        EntityPlayer player = (EntityPlayer) event.getEntity();
        IRegeneration cap = CapabilityRegeneration.getForPlayer(player);
        if ((cap.getState() == GRACE) && player.getHealth() - event.getAmount() < 0) {
            //uh oh, we're dying in grace. Forcibly regenerate before all (?) death prevention mods
            boolean notDead = cap.getStateManager().onKilled(event.getSource());
            event.setCanceled(notDead);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void adMortemInimicus(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer))
            return;
        EntityPlayer player = (EntityPlayer) event.getEntity();
        IRegeneration cap = CapabilityRegeneration.getForPlayer(player);
        if ((event.getSource() == RegenObjects.REGEN_DMG_CRITICAL || event.getSource() == RegenObjects.REGEN_DMG_KILLED) && !player.world.isRemote) {
            cap.setDnaType(DnaHandler.DNA_BORING.getRegistryName());
            if (RegenConfig.loseRegensOnDeath) {
                cap.extractRegeneration(cap.getRegenerationsLeft());
            }
            cap.synchronise();
            return;
        }
        boolean notDead = cap.getStateManager().onKilled(event.getSource());
        event.setCanceled(notDead);
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            if (CapabilityRegeneration.getForPlayer((EntityPlayer) event.getEntityLiving()).getState() == PlayerUtil.RegenState.REGENERATING) {
                event.setCanceled(true);
            }
        }
    }

    // ================ OTHER ==============
    @SubscribeEvent
    public static void onLogin(PlayerLoggedInEvent event) {
        if (event.player.world.isRemote) return;

        NBTTagCompound nbt = event.player.getEntityData(), persist = nbt.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if (!persist.getBoolean("loggedInBefore"))
            CapabilityRegeneration.getForPlayer(event.player).receiveRegenerations(RegenConfig.freeRegenerations);
        persist.setBoolean("loggedInBefore", true);
        nbt.setTag(EntityPlayer.PERSISTED_NBT_TAG, persist);
    }

    /**
     * Update checker thing, tells the player that the mods out of date if they're on a old build
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent e) {
        EntityPlayer player = e.player;
        if (!player.world.isRemote && RegenConfig.enableUpdateChecker) {
            ForgeVersion.CheckResult version = ForgeVersion.getResult(Loader.instance().activeModContainer());
            if (version.status.equals(ForgeVersion.Status.OUTDATED)) {
                TextComponentString url = new TextComponentString(TextFormatting.AQUA + TextFormatting.BOLD.toString() + "UPDATE");
                url.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://minecraft.curseforge.com/projects/regeneration"));
                url.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Open URL")));

                player.sendMessage(new TextComponentString(TextFormatting.GOLD + "[Regeneration] : ").appendSibling(url));
                String changes = version.changes.get(version.target);
                player.sendMessage(new TextComponentString(TextFormatting.GOLD + "Changes: " + TextFormatting.BLUE + changes));
            }
        }
    }

    @SubscribeEvent
    public static void addRunAwayTask(EntityJoinWorldEvent e) {
        if (e.getEntity().world.isRemote) return;
        if (e.getEntity() instanceof EntityCreature) {
            EntityCreature living = (EntityCreature) e.getEntity();
            Predicate<Entity> pred = entity -> {

                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    IRegeneration data = CapabilityRegeneration.getForPlayer(player);
                    return data.getState() == PlayerUtil.RegenState.REGENERATING || data.areHandsGlowing();
                }
                return false;
            };

            living.tasks.addTask(0, new EntityAIAvoidEntity(living, EntityPlayer.class, pred, 6.0F, 1.0D, 1.2D));
        }

        if (e.getEntity() instanceof EntityPlayer) {
            NetworkHandler.INSTANCE.sendToAll(new MessageRemovePlayer(e.getEntity().getUniqueID()));
        }
    }

    @SubscribeEvent
    public static void onCut(PlayerInteractEvent.RightClickItem event) {
        if (RegenUtil.isSharp(event.getItemStack())) {
            EntityPlayer player = event.getEntityPlayer();
            IRegeneration cap = CapabilityRegeneration.getForPlayer(player);
            if (!player.world.isRemote && cap.getState() == POST && player.isSneaking() && !cap.hasDroppedHand()) {
                ItemStack hand = new ItemStack(RegenObjects.Items.HAND);
                ItemHand.setTextureString(hand, cap.getEncodedSkin());
                ItemHand.setSkinType(hand, cap.getSkinType().name());
                ItemHand.setOwner(hand, player.getUniqueID());
                ItemHand.setTimeCreated(hand, System.currentTimeMillis());
                ItemHand.setTrait(hand, cap.getDnaType().toString());
                cap.setDroppedHand(true);
                RegenTriggers.HAND.trigger((EntityPlayerMP) player);
                if (player.getPrimaryHand() == EnumHandSide.LEFT) {
                    cap.setCutOffHand(EnumHandSide.RIGHT);
                } else {
                    cap.setCutOffHand(EnumHandSide.LEFT);
                }
                InventoryHelper.spawnItemStack(player.world, player.posX, player.posY, player.posZ, hand);
            }
        }
    }

}
