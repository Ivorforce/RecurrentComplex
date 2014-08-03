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
import ivorius.reccomplex.network.PacketEditInventoryGenerator;
import ivorius.reccomplex.worldgen.inventory.GenericInventoryGenerator;
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
import net.minecraft.util.WeightedRandomChestContent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

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
    private GenericInventoryGenerator inventoryGenerator;

    private GuiTextField nameTextField;
    private GuiButton saveBtn;
    private GuiButton cancelBtn;

    private GuiButton nextPageBtn;
    private GuiButton prevPageBtn;

    private GuiSliderRange itemNumberRangeSlider;
    private GuiTextField dependencyTextField;
    private GuiValidityStateIndicator dependencyStateIndicator;

    private List<GuiSlider> weightSliders = new ArrayList<>();
    private List<GuiSliderRange> minMaxSliders = new ArrayList<>();

    private int currentColShift;

    public GuiEditInventoryGen(EntityPlayer player, GenericInventoryGenerator generator, String key)
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

        this.nameTextField = new GuiTextField(this.fontRendererObj, this.width / 2 - 150, this.height / 2 - 110, 142, 20);
        this.buttonList.add(this.saveBtn = new GuiButton(0, this.width / 2, this.height / 2 - 110, 70, 20, I18n.format("guiGenericInventory.save")));
        this.buttonList.add(this.cancelBtn = new GuiButton(1, this.width / 2 + 75, this.height / 2 - 110, 70, 20, I18n.format("gui.cancel")));

        this.buttonList.add(this.itemNumberRangeSlider = new GuiSliderRange(4, this.width / 2 - 150, this.height / 2 - 85, 142, 20, ""));
        this.itemNumberRangeSlider.setMinValue(0);
        this.itemNumberRangeSlider.setMaxValue(60);
        this.itemNumberRangeSlider.addListener(this);

        dependencyTextField = new GuiTextField(fontRendererObj, this.width / 2, this.height / 2 - 85, 130, 20);
        dependencyStateIndicator = new GuiValidityStateIndicator(this.width / 2 + 135, this.height / 2 - 80, inventoryGenerator.areDependenciesResolved() ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.SEMI_VALID);

        this.buttonList.add(this.nextPageBtn = new GuiButton(2, shiftRightPage, this.height / 2 - 50, 20, 20, ">"));
        this.buttonList.add(this.prevPageBtn = new GuiButton(3, shiftRightPage, this.height / 2 - 20, 20, 20, "<"));
        this.nameTextField.setMaxStringLength(32767);
        this.nameTextField.setFocused(true);
        this.nameTextField.setText(key);

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

                GuiSlider weightSlider = new GuiSlider(200, baseX + onePart * 2, shiftTop + 48 + row * 18, onePart * 3, 18, I18n.format("guiGenericInventory.weight"));
                weightSlider.addListener(this);
                weightSlider.setMinValue(1);
                weightSlider.setMaxValue(500);
                this.buttonList.add(weightSlider);
                weightSliders.add(weightSlider);
            }
        }

        this.saveBtn.enabled = this.nameTextField.getText().trim().length() > 0;

        this.scrollTo(currentColShift);
        updateGenAmountSliders();
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
        List<WeightedRandomChestContent> chestContents = inventoryGenerator.weightedRandomChestContents;

        for (int i = 0; i < weightSliders.size(); i++)
        {
            GuiSlider weightSlider = weightSliders.get(i);
            GuiSliderRange minMaxSlider = minMaxSliders.get(i);

            int index = i + currentColShift * ContainerEditInventoryGen.ITEM_ROWS;
            weightSlider.id = index + 100;
            minMaxSlider.id = index + 200;
            if (index < chestContents.size())
            {
                WeightedRandomChestContent chestContent = chestContents.get(index);
                minMaxSlider.setRange(new FloatRange(chestContent.theMinimumChanceToGenerateItem, chestContent.theMaximumChanceToGenerateItem));
                minMaxSlider.setMaxValue(chestContent.theItemId.getMaxStackSize());
                minMaxSlider.enabled = true;
                minMaxSlider.displayString = I18n.format("guiGenericInventory.minMax", chestContent.theMinimumChanceToGenerateItem, chestContent.theMaximumChanceToGenerateItem);

                weightSlider.setValue(chestContent.itemWeight);
                weightSlider.enabled = true;
                weightSlider.displayString = I18n.format("guiGenericInventory.weightNumber", "" + MathHelper.floor_float(weightSlider.getValue()));
            }
            else
            {
                minMaxSlider.setRange(new FloatRange(1, 1));
                minMaxSlider.setMaxValue(64);
                minMaxSlider.enabled = false;
                minMaxSlider.displayString = "Min - Max";

                weightSlider.setValue(weightSlider.getMinValue());
                weightSlider.enabled = false;
                weightSlider.displayString = I18n.format("guiGenericInventory.weight");
            }
        }
    }

    private void updateGenAmountSliders()
    {
        itemNumberRangeSlider.setRange(new FloatRange(inventoryGenerator.minItems, inventoryGenerator.maxItems));
        itemNumberRangeSlider.displayString = I18n.format("guiGenericInventory.minMaxItems", inventoryGenerator.minItems, inventoryGenerator.maxItems);
    }

    private void updatePageButtons()
    {
        List<WeightedRandomChestContent> chestContents = inventoryGenerator.weightedRandomChestContents;
        int neededCols = chestContents.size() / ContainerEditInventoryGen.ITEM_ROWS + 1;
        nextPageBtn.enabled = (currentColShift + ContainerEditInventoryGen.ITEM_COLUMNS) < neededCols;
        prevPageBtn.enabled = currentColShift > 0;
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        nameTextField.updateCursorCounter();
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
    protected void keyTyped(char par1, int par2)
    {
        if (!(par2 == Keyboard.KEY_ESCAPE || par2 == this.mc.gameSettings.keyBindInventory.getKeyCode())) // Escape!
        {
            super.keyTyped(par1, par2);
        }

        if (nameTextField.textboxKeyTyped(par1, par2))
        {
            key = nameTextField.getText();
        }
        else if (dependencyTextField.textboxKeyTyped(par1, par2))
        {
            inventoryGenerator.dependencies.clear();
            String[] dependencies = dependencyTextField.getText().split(",");
            if (dependencies.length != 1 || dependencies[0].trim().length() > 0)
            {
                Collections.addAll(inventoryGenerator.dependencies, dependencies);
            }
            dependencyStateIndicator.setState(inventoryGenerator.areDependenciesResolved() ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.SEMI_VALID);
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

        this.saveBtn.enabled = key.trim().length() > 0;

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
    protected void mouseClicked(int par1, int par2, int par3)
    {
        super.mouseClicked(par1, par2, par3);

        this.nameTextField.mouseClicked(par1, par2, par3);
        dependencyTextField.mouseClicked(par1, par2, par3);
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
    {
        this.nameTextField.drawTextBox();
        dependencyTextField.drawTextBox();
        if (StringUtils.isNullOrEmpty(dependencyTextField.getText()))
        {
            drawString(fontRendererObj, "Dependencies (A,B,C...)", this.width / 2 + 5, this.height / 2 - 85 + 7, 0xff888888);
        }
        dependencyStateIndicator.draw();

        mc.getTextureManager().bindTexture(textureBackground);
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
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
        List<WeightedRandomChestContent> chestContents = inventoryGenerator.weightedRandomChestContents;

        if (gui instanceof GuiSlider)
        {
            GuiSlider slider = (GuiSlider) gui;
            int value = MathHelper.floor_float(slider.getValue());
            slider.setValue(value);

            if (slider.id < 200 && slider.id >= 100)
            {
                int stackIndex = slider.id - 100;
                if (stackIndex < chestContents.size())
                {
                    chestContents.get(stackIndex).itemWeight = value;
                    updateAllItemSliders();
                }
            }
        }
        else if (gui instanceof GuiSliderRange)
        {
            GuiSliderRange slider = (GuiSliderRange) gui;

            if (slider.id == 4)
            {
                IntegerRange intRange = new IntegerRange(slider.getRange());
                inventoryGenerator.minItems = intRange.getMin();
                inventoryGenerator.maxItems = intRange.getMax();

                updateGenAmountSliders();
            }
            else if (slider.id < 300)
            {
                int stackIndex = slider.id - 200;

                if (stackIndex < chestContents.size())
                {
                    WeightedRandomChestContent chestContent = chestContents.get(stackIndex);
                    IntegerRange intRange = new IntegerRange(slider.getRange());
                    chestContent.theMinimumChanceToGenerateItem = intRange.getMin();
                    chestContent.theMaximumChanceToGenerateItem = intRange.getMax();

                    slider.setRange(new FloatRange(chestContent.theMinimumChanceToGenerateItem, chestContent.theMaximumChanceToGenerateItem));

                    updateAllItemSliders();
                }
            }
        }
    }

    @Override
    protected void mouseMovedOrUp(int p_146286_1_, int p_146286_2_, int p_146286_3_)
    {
        super.mouseMovedOrUp(p_146286_1_, p_146286_2_, p_146286_3_);

        if (p_146286_3_ == 0)
        {
            for (Object object : buttonList)
            {
                GuiButton button = (GuiButton) object;
                button.mouseReleased(p_146286_1_, p_146286_2_);
            }
        }
    }
}
