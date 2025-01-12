package me.suff.mc.regen.client.gui;

import me.suff.mc.regen.RegenConfig;
import me.suff.mc.regen.RegenerationMod;
import me.suff.mc.regen.client.gui.parts.BlankContainer;
import me.suff.mc.regen.client.gui.parts.InventoryTabRegeneration;
import me.suff.mc.regen.client.skinhandling.SkinChangingHandler;
import me.suff.mc.regen.common.capability.CapabilityRegeneration;
import me.suff.mc.regen.common.traits.DnaHandler;
import me.suff.mc.regen.common.types.TypeHandler;
import me.suff.mc.regen.network.MessageChangeType;
import me.suff.mc.regen.network.MessageUpdateModel;
import me.suff.mc.regen.network.NetworkHandler;
import micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import java.awt.*;
import java.io.IOException;

public class GuiPreferences extends GuiContainer {

    public static final int ID = 1;
    private static final ResourceLocation BACKGROUND = new ResourceLocation(RegenerationMod.MODID, "textures/gui/pref_back.png");
    private static TypeHandler.RegenType SELECTED_TYPE = CapabilityRegeneration.getForPlayer(Minecraft.getMinecraft().player).getType();
    private static SkinChangingHandler.EnumChoices CHOICES = CapabilityRegeneration.getForPlayer(Minecraft.getMinecraft().player).getPreferredModel();

    public GuiPreferences() {
        super(new BlankContainer());
        xSize = 256;
        ySize = 173;
    }

    @Override
    public void initGui() {
        super.initGui();
        TabRegistry.updateTabValues(guiLeft, guiTop, InventoryTabRegeneration.class);
        TabRegistry.addTabsToList(this.buttonList);
        int cx = (width - xSize) / 2;
        int cy = (height - ySize) / 2;
        final int btnW = 68, btnH = 17;

        GuiButtonExt btnClose = new GuiButtonExt(98, width / 2 - 109, cy + 145, 71, btnH, new TextComponentTranslation("regeneration.gui.close").getFormattedText());
        GuiButtonExt btnRegenType = new GuiButtonExt(44, width / 2 + 50 - 66, cy + 125, btnW * 2, btnH, new TextComponentTranslation("regentype." + SELECTED_TYPE.name().toLowerCase()).getUnformattedComponentText());
        GuiButtonExt btnSkinType = new GuiButtonExt(22, width / 2 + 50 - 66, cy + 85, btnW * 2, btnH, new TextComponentTranslation("regeneration.gui.skintype", new TextComponentTranslation("skintype." + CHOICES.name().toLowerCase())).getUnformattedComponentText());
        btnRegenType.displayString = new TextComponentTranslation("regeneration.gui.type", new TextComponentTranslation("regentype." + SELECTED_TYPE.name().toLowerCase()).getUnformattedComponentText()).getUnformattedComponentText();

        GuiButtonExt btnColor = new GuiButtonExt(99, width / 2 + 50 - 66, cy + 105, btnW * 2, btnH, new TextComponentTranslation("regeneration.gui.color_gui").getUnformattedComponentText());
        GuiButtonExt btnOpenFolder = new GuiButtonExt(100, width / 2 + 50 - 66, cy + 145, btnW * 2, btnH, new TextComponentTranslation("regeneration.gui.skin_choice").getFormattedText());

        buttonList.add(btnRegenType);
        buttonList.add(btnOpenFolder);
        buttonList.add(btnClose);
        buttonList.add(btnColor);
        buttonList.add(btnSkinType);

        SELECTED_TYPE = CapabilityRegeneration.getForPlayer(Minecraft.getMinecraft().player).getType();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        int cx = (width - xSize) / 2;
        int cy = (height - ySize) / 2;

        GlStateManager.pushMatrix();
        // RenderUtil.drawRect(width / 2, height / 2 - 50, width / 2 - 80, height / 2 + 35, 0.0F, 0.0F, 0.0F, 1);
        GuiInventory.drawEntityOnScreen(width / 2 - 75, height / 2 + 45, 55, (float) (guiLeft + 51) - mouseX, (float) (guiTop + 75 - 50) - mouseY, Minecraft.getMinecraft().player);
        GlStateManager.popMatrix();

        drawCenteredString(Minecraft.getMinecraft().fontRenderer, new TextComponentTranslation("regeneration.gui.preferences").getUnformattedComponentText(), width / 2, height / 2 - 80, Color.WHITE.getRGB());

        String str = "Banana Phone";
        int length = mc.fontRenderer.getStringWidth(str);

        if (RegenConfig.infiniteRegeneration)
            str = new TextComponentTranslation("regeneration.gui.infinite_regenerations").getFormattedText(); // TODO this should be optimized
        else
            str = new TextComponentTranslation("regeneration.gui.remaining_regens.status").getFormattedText() + " " + CapabilityRegeneration.getForPlayer(Minecraft.getMinecraft().player).getRegenerationsLeft();

        length = mc.fontRenderer.getStringWidth(str);
        fontRenderer.drawStringWithShadow(str, cx + 170 - length / 2, cy + 21, Color.WHITE.getRGB());

        TextComponentTranslation traitLang = new TextComponentTranslation(DnaHandler.getDnaEntry(CapabilityRegeneration.getForPlayer(mc.player).getDnaType()).getLangKey());
        fontRenderer.drawStringWithShadow(traitLang.getUnformattedText(), cx + 170 - length / 2, cy + 40, Color.WHITE.getRGB());

        TextComponentTranslation traitLangDesc = new TextComponentTranslation(DnaHandler.getDnaEntry(CapabilityRegeneration.getForPlayer(mc.player).getDnaType()).getLocalDesc());
        fontRenderer.drawStringWithShadow(traitLangDesc.getUnformattedText(), cx + 170 - length / 2, cy + 50, Color.WHITE.getRGB());

    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        switch (button.id) {
            case 98:
                Minecraft.getMinecraft().displayGuiScreen(null);
                break;

            case 44:
                if (SELECTED_TYPE.next() != null) {
                    SELECTED_TYPE = (TypeHandler.RegenType) SELECTED_TYPE.next();
                } else {
                    SELECTED_TYPE = TypeHandler.RegenType.FIERY;
                }
                button.displayString = new TextComponentTranslation("regeneration.gui.type", new TextComponentTranslation("regentype." + SELECTED_TYPE.name().toLowerCase()).getUnformattedComponentText()).getUnformattedComponentText();
                NetworkHandler.INSTANCE.sendToServer(new MessageChangeType(SELECTED_TYPE));
                break;
            case 99:
                Minecraft.getMinecraft().player.openGui(RegenerationMod.INSTANCE, GuiCustomizer.ID, Minecraft.getMinecraft().world, 0, 0, 0);
                break;

            case 100:
                Minecraft.getMinecraft().player.openGui(RegenerationMod.INSTANCE, GuiSkinChange.ID, Minecraft.getMinecraft().world, 0, 0, 0);
                break;

            case 22:
                if (CHOICES.next() != null) {
                    CHOICES = (SkinChangingHandler.EnumChoices) CHOICES.next();
                } else {
                    CHOICES = SkinChangingHandler.EnumChoices.ALEX;
                }
                button.displayString = new TextComponentTranslation("regeneration.gui.skintype", new TextComponentTranslation("skintype." + CHOICES.name().toLowerCase())).getUnformattedComponentText();
                NetworkHandler.INSTANCE.sendToServer(new MessageUpdateModel(CHOICES.name()));
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
