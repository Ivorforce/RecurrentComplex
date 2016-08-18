/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;

/**
 * Created by lukas on 29.06.14.
 */
public class RCConfigGuiFactory implements IModGuiFactory
{
    @Override
    public void initialize(Minecraft minecraftInstance)
    {

    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass()
    {
        return ConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element)
    {
        return null;
    }

    public static class ConfigGui extends GuiConfig
    {
        public ConfigGui(GuiScreen parentScreen)
        {
            super(parentScreen, getConfigElements(), RecurrentComplex.MOD_ID, false, false, I18n.translateToLocalFormatted("reccomplex.configgui.title"));
        }

        private static List<IConfigElement> getConfigElements()
        {
            List<IConfigElement> list = new ArrayList<>();
            list.add(new DummyCategoryElement("reccomplex.configgui.general", "reccomplex.configgui.ctgy.general", GeneralEntry.class).setRequiresMcRestart(true));
            list.add(new DummyCategoryElement("reccomplex.configgui.balancing", "reccomplex.configgui.ctgy.balancing", BalancingEntry.class));
            list.add(new DummyCategoryElement("reccomplex.configgui.visual", "reccomplex.configgui.ctgy.visual", VisualEntry.class));
            list.add(new DummyCategoryElement("reccomplex.configgui.controls", "reccomplex.configgui.ctgy.controls", ControlsEntry.class));
            return list;
        }

        public static class GeneralEntry extends GuiConfigEntries.CategoryEntry
        {
            public GeneralEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
            {
                super(owningScreen, owningEntryList, prop);
            }

            @Override
            protected GuiScreen buildChildScreen()
            {
                return new GuiConfig(this.owningScreen,
                        (new ConfigElement(RecurrentComplex.config.getCategory(Configuration.CATEGORY_GENERAL))).getChildElements(),
                        this.owningScreen.modID, Configuration.CATEGORY_GENERAL, this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                        this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                        GuiConfig.getAbridgedConfigPath(RecurrentComplex.config.toString()));
            }
        }

        public static class BalancingEntry extends GuiConfigEntries.CategoryEntry
        {
            public BalancingEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
            {
                super(owningScreen, owningEntryList, prop);
            }

            @Override
            protected GuiScreen buildChildScreen()
            {
                return new GuiConfig(this.owningScreen,
                        (new ConfigElement(RecurrentComplex.config.getCategory(RCConfig.CATEGORY_BALANCING))).getChildElements(),
                        this.owningScreen.modID, RCConfig.CATEGORY_BALANCING, this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                        this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                        GuiConfig.getAbridgedConfigPath(RecurrentComplex.config.toString()));
            }
        }

        public static class VisualEntry extends GuiConfigEntries.CategoryEntry
        {
            public VisualEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
            {
                super(owningScreen, owningEntryList, prop);
            }

            @Override
            protected GuiScreen buildChildScreen()
            {
                return new GuiConfig(this.owningScreen,
                        (new ConfigElement(RecurrentComplex.config.getCategory(RCConfig.CATEGORY_VISUAL))).getChildElements(),
                        this.owningScreen.modID, RCConfig.CATEGORY_VISUAL, this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                        this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                        GuiConfig.getAbridgedConfigPath(RecurrentComplex.config.toString()));
            }
        }

        public static class ControlsEntry extends GuiConfigEntries.CategoryEntry
        {
            public ControlsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
            {
                super(owningScreen, owningEntryList, prop);
            }

            @Override
            protected GuiScreen buildChildScreen()
            {
                return new GuiConfig(this.owningScreen,
                        (new ConfigElement(RecurrentComplex.config.getCategory(RCConfig.CATEGORY_CONTROLS))).getChildElements(),
                        this.owningScreen.modID, RCConfig.CATEGORY_CONTROLS, this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                        this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                        GuiConfig.getAbridgedConfigPath(RecurrentComplex.config.toString()));
            }
        }
    }
}
