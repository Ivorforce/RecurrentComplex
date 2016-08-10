/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editinventorygen;

import ivorius.ivtoolkit.gui.*;
import ivorius.ivtoolkit.network.PacketGuiAction;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.gui.InventoryWatcher;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.Bounds;
import ivorius.reccomplex.network.PacketEditInventoryGenerator;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.utils.RangeHelper;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import org.lwjgl.input.Keyboard;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 26.05.14.
 */
public class GuiEditInventoryGen extends GuiContainer implements InventoryWatcher, GuiControlListener
{
    public static ResourceLocation textureBackground = new ResourceLocation(RecurrentComplex.MODID, RecurrentComplex.filePathTextures + "guiEditInventoryGen.png");

    public String key;
    private Component inventoryGenerator;

    private GuiTextField nameTextField;
    private GuiTextField inventoryGenIDTextField;
    private GuiButton saveBtn;
    private GuiButton cancelBtn;

    private GuiButton nextPageBtn;
    private GuiButton prevPageBtn;

    private GuiTextField dependencyTextField;
    private GuiValidityStateIndicator dependencyStateIndicator;

    private List<GuiSlider> weightSliders = new ArrayList<>();
    private List<GuiSliderRange> minMaxSliders = new ArrayList<>();

    private int currentColShift;

    public GuiEditInventoryGen(EntityPlayer player, Component generator, String key)
    {
        super(new ContainerEditInventoryGen(player, generator));

        this.inventoryGenerator = generator;
        this.key = key;
        this.xSize = ContainerEditInventoryGen.SEGMENT_WIDTH * ContainerEditInventoryGen.ITEM_COLUMNS + 20;
        this.ySize = 219;
        ((ContainerEditInventoryGen) inventorySlots).inventory.addWatcher(this);
    }

    @Override
    public void initGui()
    {
        super.initGui();

        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        weightSliders.clear();
        minMaxSliders.clear();

        int shiftRight = width / 2 - xSize / 2;
        int shiftTop = height / 2 - ySize / 2;
        int shiftRightPage = shiftRight + ContainerEditInventoryGen.ITEM_COLUMNS * ContainerEditInventoryGen.SEGMENT_WIDTH;

        this.nameTextField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 150, this.height / 2 - 110, 142, 20);
        this.nameTextField.setMaxStringLength(32767);
        this.nameTextField.setFocused(true);
        this.nameTextField.setText(key);
        this.buttonList.add(this.saveBtn = new GuiButton(0, this.width / 2, this.height / 2 - 110, 70, 20, I18n.format("guiGenericInventory.save")));
        this.buttonList.add(this.cancelBtn = new GuiButton(1, this.width / 2 + 75, this.height / 2 - 110, 70, 20, I18n.format("gui.cancel")));

        inventoryGenIDTextField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 150, this.height / 2 - 85, 142, 20);
        inventoryGenIDTextField.setMaxStringLength(32767);
        inventoryGenIDTextField.setText(inventoryGenerator.inventoryGeneratorID);

        dependencyTextField = new GuiTextField(0, fontRendererObj, this.width / 2, this.height / 2 - 85, 130, 20);
        dependencyTextField.setText(inventoryGenerator.dependencies.getExpression());
        dependencyStateIndicator = new GuiValidityStateIndicator(this.width / 2 + 135, this.height / 2 - 80, TableDataSourceExpression.getValidityState(inventoryGenerator.dependencies));

        this.buttonList.add(this.nextPageBtn = new GuiButton(2, shiftRightPage, this.height / 2 - 50, 20, 20, ">"));
        this.buttonList.add(this.prevPageBtn = new GuiButton(3, shiftRightPage, this.height / 2 - 20, 20, 20, "<"));

        for (int col = 0; col < ContainerEditInventoryGen.ITEM_COLUMNS; ++col)
        {
            for (int row = 0; row < ContainerEditInventoryGen.ITEM_ROWS; ++row)
            {
                int availableSize = ContainerEditInventoryGen.SEGMENT_WIDTH - 22 - 4;
                int baseX = shiftRight + 20 + col * ContainerEditInventoryGen.SEGMENT_WIDTH;
                int onePart = availableSize / 5;

                GuiSliderRange minMaxSlider = new GuiSliderRange(100, baseX, shiftTop + 48 + row * 18, onePart * 2 - 2, 18, "");
                minMaxSlider.addListener(this);
                minMaxSlider.setMinValue(1);
                this.buttonList.add(minMaxSlider);
                minMaxSliders.add(minMaxSlider);

                GuiSlider weightSlider = new GuiSlider(200, baseX + onePart * 2, shiftTop + 48 + row * 18, onePart * 3, 18, IvTranslations.get("structures.gui.random.weight"));
                weightSlider.addListener(this);
                weightSlider.setMinValue(0);
                weightSlider.setMaxValue(10);
                this.buttonList.add(weightSlider);
                weightSliders.add(weightSlider);
            }
        }

        updateSaveButtonEnabled();

        this.scrollTo(currentColShift);
    }

    public void scrollTo(int colShift)
    {
        currentColShift = colShift;
        ((ContainerEditInventoryGen) inventorySlots).scrollTo(colShift);

        updateAllItemSliders();
        updatePageButtons();
    }

    private void updateAllItemSliders()
    {
        List<GenericItemCollection.RandomizedItemStack> chestContents = inventoryGenerator.items;

        for (int i = 0; i < weightSliders.size(); i++)
        {
            GuiSlider weightSlider = weightSliders.get(i);
            GuiSliderRange minMaxSlider = minMaxSliders.get(i);

            int index = i + currentColShift * ContainerEditInventoryGen.ITEM_ROWS;
            weightSlider.id = index + 100;
            minMaxSlider.id = index + 200;
            if (index < chestContents.size())
            {
                GenericItemCollection.RandomizedItemStack chestContent = chestContents.get(index);
                minMaxSlider.setRange(new FloatRange(chestContent.min, chestContent.max));
                minMaxSlider.setMaxValue(chestContent.itemStack.getMaxStackSize());
                minMaxSlider.enabled = true;
                minMaxSlider.displayString = I18n.format("guiGenericInventory.minMax", chestContent.min, chestContent.max);

                weightSlider.setValue((float) chestContent.weight);
                weightSlider.enabled = true;
                weightSlider.displayString = I18n.format("guiGenericInventory.weightNumber", String.format("%.2f", weightSlider.getValue()));
            }
            else
            {
                minMaxSlider.setRange(new FloatRange(1, 1));
                minMaxSlider.setMaxValue(64);
                minMaxSlider.enabled = false;
                minMaxSlider.displayString = "Min - Max";

                weightSlider.setValue(weightSlider.getMinValue());
                weightSlider.enabled = false;
                weightSlider.displayString = I18n.format("structures.gui.random.weight");
            }
        }
    }

    private void updatePageButtons()
    {
        List<GenericItemCollection.RandomizedItemStack> chestContents = inventoryGenerator.items;
        int neededCols = chestContents.size() / ContainerEditInventoryGen.ITEM_ROWS + 1;
        nextPageBtn.enabled = (currentColShift + ContainerEditInventoryGen.ITEM_COLUMNS) < neededCols;
        prevPageBtn.enabled = currentColShift > 0;
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        nameTextField.updateCursorCounter();
        inventoryGenIDTextField.updateCursorCounter();
        dependencyTextField.updateCursorCounter();
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == 1)
            {
                this.mc.thePlayer.closeScreen();
            }
            else if (button.id == 0)
            {
                RecurrentComplex.network.sendToServer(new PacketEditInventoryGenerator(key, inventoryGenerator));

                this.mc.thePlayer.closeScreen();
            }
            else if (button.id == 2)
            {
                scrollTo(currentColShift + 1);
                RecurrentComplex.network.sendToServer(PacketGuiAction.packetGuiAction("igSelectCol", currentColShift));
            }
            else if (button.id == 3)
            {
                scrollTo(currentColShift - 1);
                RecurrentComplex.network.sendToServer(PacketGuiAction.packetGuiAction("igSelectCol", currentColShift));
            }
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException
    {
        if (!(par2 == Keyboard.KEY_ESCAPE || par2 == this.mc.gameSettings.keyBindInventory.getKeyCode())) // Escape!
        {
            super.keyTyped(par1, par2);
        }

        if (nameTextField.textboxKeyTyped(par1, par2))
        {
            key = nameTextField.getText();
        }
        else if (inventoryGenIDTextField.textboxKeyTyped(par1, par2))
        {
            inventoryGenerator.inventoryGeneratorID = inventoryGenIDTextField.getText();
        }
        else if (dependencyTextField.textboxKeyTyped(par1, par2))
        {
            inventoryGenerator.dependencies.setExpression(dependencyTextField.getText());
            dependencyStateIndicator.setState(TableDataSourceExpression.getValidityState(inventoryGenerator.dependencies));
        }
        else
        {
            if (par2 == Keyboard.KEY_LEFT && prevPageBtn.enabled)
            {
                actionPerformed(prevPageBtn);
            }
            else if (par2 == Keyboard.KEY_RIGHT && nextPageBtn.enabled)
            {
                actionPerformed(nextPageBtn);
            }
        }

        updateSaveButtonEnabled();

//        if (par2 != 28 && par2 != 156)
//        {
//            if (par2 == 1)
//            {
//                this.actionPerformed(this.cancelBtn);
//            }
//        }
//        else
//        {
//            this.actionPerformed(this.saveBtn);
//        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException
    {
        super.mouseClicked(par1, par2, par3);

        this.nameTextField.mouseClicked(par1, par2, par3);
        this.inventoryGenIDTextField.mouseClicked(par1, par2, par3);
        dependencyTextField.mouseClicked(par1, par2, par3);
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int mouseX, int mouseY)
    {
        nameTextField.drawTextBox();
        drawPlaceholderString(nameTextField, "Component ID");

        inventoryGenIDTextField.drawTextBox();
        drawPlaceholderString(inventoryGenIDTextField, "Group ID");

        dependencyTextField.drawTextBox();
        drawPlaceholderString(dependencyTextField, "Dependency Expression");
        dependencyStateIndicator.draw();

        mc.getTextureManager().bindTexture(textureBackground);
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        drawTexturedModalRect(width / 2 - 176 / 2 - 20 / 2 - 1, MathHelper.ceiling_float_int(height * 0.5f) + 17, 0, 0, 176, 90);

        for (int i = 0; i < ContainerEditInventoryGen.ITEM_ROWS; i++)
        {
            drawTexturedModalRect(width / 2 - ContainerEditInventoryGen.SEGMENT_WIDTH / 2 - 11, height / 2 - 61 + i * 18, 7, 7, 18, 18);
        }
//        for (int i = 0; i < ContainerEditInventoryGen.ITEM_COLUMNS; i++)
//        {
//            int baseX = width / 2 + i * ContainerEditInventoryGen.SEGMENT_WIDTH;
//            drawCenteredString(fontRendererObj, I18n.format("guiGenericInventory.min"), baseX + 20, this.height / 2 - 75, 0xffffffff);
//            drawCenteredString(fontRendererObj, I18n.format("guiGenericInventory.max"), baseX + 40, this.height / 2 - 75, 0xffffffff);
//            drawCenteredString(fontRendererObj, I18n.format("guiGenericInventory.weight"), baseX + 60, this.height / 2 - 75, 0xffffffff);
//        }

        if (Bounds.fromSize(dependencyStateIndicator.xPosition, dependencyStateIndicator.getWidth(), dependencyStateIndicator.yPosition, dependencyStateIndicator.getHeight()).contains(mouseX, mouseY))
            drawHoveringText(Collections.singletonList(TableDataSourceExpression.parsedString(inventoryGenerator.dependencies)), mouseX, mouseY, fontRendererObj);
    }

    private void drawPlaceholderString(GuiTextField textField, String string)
    {
        if (StringUtils.isNullOrEmpty(textField.getText()))
            drawString(fontRendererObj, string, textField.xPosition + 5, textField.yPosition + 7, 0xff888888);
    }

    @Override
    public void inventoryChanged(IInventory inventory)
    {
        updateAllItemSliders();
        updatePageButtons();
    }

    @Override
    public void valueChanged(Gui gui)
    {
        List<GenericItemCollection.RandomizedItemStack> chestContents = inventoryGenerator.items;

        if (gui instanceof GuiSlider)
        {
            GuiSlider slider = (GuiSlider) gui;

            if (slider.id < 200 && slider.id >= 100)
            {
                int stackIndex = slider.id - 100;
                if (stackIndex < chestContents.size())
                {
                    chestContents.get(stackIndex).weight = slider.getValue();
                    updateAllItemSliders();
                }
            }
        }
        else if (gui instanceof GuiSliderRange)
        {
            GuiSliderRange slider = (GuiSliderRange) gui;

            if (slider.id < 300)
            {
                int stackIndex = slider.id - 200;

                if (stackIndex < chestContents.size())
                {
                    GenericItemCollection.RandomizedItemStack chestContent = chestContents.get(stackIndex);
                    IntegerRange intRange = RangeHelper.roundedIntRange(slider.getRange());
                    chestContent.min = intRange.getMin();
                    chestContent.max = intRange.getMax();

                    slider.setRange(new FloatRange(chestContent.min, chestContent.max));

                    updateAllItemSliders();
                }
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        if (state == 0)
        {
            for (Object object : buttonList)
            {
                GuiButton button = (GuiButton) object;
                button.mouseReleased(mouseX, mouseY);
            }
        }
    }

    public void updateSaveButtonEnabled()
    {
        saveBtn.enabled = key.trim().length() > 0 && inventoryGenerator.inventoryGeneratorID.trim().length() > 0;
    }
}
