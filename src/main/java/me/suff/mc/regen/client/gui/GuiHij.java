package me.suff.mc.regen.client.gui;

import me.suff.mc.regen.RegenerationMod;
import me.suff.mc.regen.client.gui.parts.HIJContainer;
import me.suff.mc.regen.common.tiles.TileEntityHandInJar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

@SideOnly(Side.CLIENT)
public class GuiHij extends GuiContainer {
    public static final int ID = 77;
    private static final ResourceLocation TEXTURE = new ResourceLocation(RegenerationMod.MODID, "textures/gui/hij.png");
    private final IInventory handInventory;
    private final TileEntityHandInJar jar;

    public GuiHij(IInventory playerInv, IInventory handInv, TileEntityHandInJar jar) {
        super(new HIJContainer(playerInv, handInv, Minecraft.getMinecraft().player));
        this.handInventory = handInv;
        this.allowUserInput = false;
        this.ySize = 133;
        this.jar = jar;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.handInventory.getDisplayName().getUnformattedText(), 8, 6, Color.BLACK.getRGB());
        this.fontRenderer.drawString("Lindos Energy: " + jar.getLindosAmont(), 8, this.ySize - 96 + 2, Color.BLACK.getRGB());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
