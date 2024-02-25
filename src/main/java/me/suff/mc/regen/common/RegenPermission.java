package me.suff.mc.regen.common;

import me.suff.mc.regen.RegenerationMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

/**
 * Created by Swirtzly on 21/11/2019 @ 10:21
 */
@Mod.EventBusSubscriber
public class RegenPermission {

    public static String CAN_REGENERATE = createPerm("can.regenerate");

    public static void registerPermissions() {
        PermissionAPI.registerNode(CAN_REGENERATE, DefaultPermissionLevel.ALL, "This permission determines whether someone can Regenerate");
    }

    public static String createPerm(String perm) {
        String permission = "regeneration." + perm;
        RegenerationMod.LOG.info("Registered Permission: " + permission);
        return permission;
    }
}
