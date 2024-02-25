package micdoodle8.mods.galacticraft.api.client.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class TabRegistry {

    public static Class<?> clazzMwcInventoryTab = null;
    public static Class<?> clazzNEIConfig = null;
    public static int recipeBookOffset;
    private static ArrayList<AbstractTab> tabList = new ArrayList<>();
    private static Class<?> clazzJEIConfig = null;

    private static Minecraft mc = FMLClientHandler.instance().getClient();

    static {
        try {
            // Checks for JEI by looking for this class instead of a Loader.isModLoaded() check
            clazzJEIConfig = Class.forName("mezz.jei.config.Config");
        } catch (Exception ignore) {
            // no log spam
        }
        // Only activate NEI feature if NEI is standalone
        if (clazzJEIConfig == null) {
            try {
                clazzNEIConfig = Class.forName("codechicken.nei.NEIClientConfig");
            } catch (Exception ignore) {
                // no log spam
            }
        }

        try {
            // Checks for MWC by looking for this class
            clazzMwcInventoryTab = Class.forName("com.paneedah.weaponlib.inventory.InventoryTab");
        } catch (Exception ignore) {
            // no log spam
        }
    }

    public static void registerTab(AbstractTab tab) {
        TabRegistry.tabList.add(tab);
    }

    public static ArrayList<AbstractTab> getTabList() {
        return TabRegistry.tabList;
    }

    // Retained for backwards compatibility with TC pre version 1.6.0d40
    public static void addTabsToInventory(GuiContainer gui) {
    }

    public static void openInventoryGui() {
        TabRegistry.mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.openContainer.windowId));
        GuiInventory inventory = new GuiInventory(TabRegistry.mc.player);
        TabRegistry.mc.displayGuiScreen(inventory);
    }

    public static void updateTabValues(int cornerX, int cornerY, Class<?> selectedButton) {
        int count = 2;

        if (clazzMwcInventoryTab != null) {
            count += 3;
        }

        for (int i = 0; i < TabRegistry.tabList.size(); i++) {
            AbstractTab t = TabRegistry.tabList.get(i);

            if (t.shouldAddToList()) {
                t.id = count;
                t.x = cornerX + (count - 2) * 28;
                t.y = cornerY - 28;
                t.enabled = !t.getClass().equals(selectedButton);
                t.potionOffsetLast = 0;
                count++;
            }
        }
    }

    public static int getPotionOffsetNEI() {
        return 0;
    }

    public static void addTabsToList(List<GuiButton> buttonList) {
        for (AbstractTab tab : TabRegistry.tabList) {
            if (tab.shouldAddToList()) {
                buttonList.add(tab);
            }
        }

    }

    public static int getRecipeBookOffset(GuiInventory gui) {
        boolean widthTooNarrow = gui.width < 379;
        gui.func_194310_f().func_194303_a(gui.width, gui.height, mc, widthTooNarrow, ((ContainerPlayer) gui.inventorySlots).craftMatrix);
        return gui.func_194310_f().updateScreenPosition(widthTooNarrow, gui.width, gui.getXSize()) - (gui.width - 176) / 2;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void guiPostInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiInventory) {
            int guiLeft = (event.getGui().width - 176) / 2;
            int guiTop = (event.getGui().height - 166) / 2;
            recipeBookOffset = getRecipeBookOffset((GuiInventory) event.getGui());
            guiLeft +=  recipeBookOffset;

            TabRegistry.updateTabValues(guiLeft, guiTop, InventoryTabVanilla.class);
            TabRegistry.addTabsToList(event.getButtonList());
        }
    }

}
