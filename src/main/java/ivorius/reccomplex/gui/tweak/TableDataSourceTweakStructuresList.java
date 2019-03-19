/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.tweak;

import gnu.trove.map.TObjectFloatMap;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.commands.RCCommands;
import ivorius.reccomplex.gui.GuiHider;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.cell.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.utils.RCStrings;
import ivorius.reccomplex.utils.scale.Scales;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class TableDataSourceTweakStructuresList extends TableDataSourceSegmented
{
    protected TObjectFloatMap<String> tweaks;

    protected List<String> editingIDs = new ArrayList<>();
    protected TableDelegate delegate;

    public TableDataSourceTweakStructuresList(TableDelegate delegate, TObjectFloatMap<String> tweaks)
    {
        this.tweaks = tweaks;
        this.delegate = delegate;

        _search(null, false);
    }

    public void search(@Nullable String search)
    {
        _search(search, true);
    }

    protected void _search(@Nullable String search, boolean reload)
    {
        editingIDs.clear();
        editingIDs.addAll(StructureRegistry.INSTANCE.ids());

        if (search != null) {
            editingIDs.removeIf(id -> !id.contains(search));
        }

        editingIDs.sort(String::compareTo);

        if (reload) {
            delegate.reloadData();
        }
    }

    public static void suggest(GuiChat chat, String command)
    {
        GuiTextField inputField = ReflectionHelper.getPrivateValue(GuiChat.class, chat, "inputField", "field_146415_a");
        inputField.setText(command);
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        String id = editingIDs.get(index);

        return TableCellMultiBuilder.create(null, delegate)
                .addCell(() -> {
                    TableCellFloatNullable cell = new TableCellFloatNullable(null, getTweak(id), 1, 0, 10, "D", "T");
                    cell.setScale(Scales.pow(5));
                    cell.addListener(value -> {
                        if (value == null)
                            tweaks.remove(id);
                        else
                            tweaks.put(id, value);
                    });
                    return cell;
                })
                .addAction(() -> {
                    GuiHider.hideGUI();

                    GuiChat chat = new GuiChat();
                    Minecraft.getMinecraft().displayGuiScreen(chat);

                    String genCommand = String.format("/%s %s", RCCommands.generate.getName(), id);

                    suggest(chat, genCommand);
                }, () -> IvTranslations.get("gui.tweakStructures.generate"), () -> IvTranslations.getLines("gui.tweakStructures.generate.tooltip"))
                .sized(() -> 0.3f)
                .withTitle(id, Collections.singletonList(id))
                .build();
    }

    public Float getTweak(String id)
    {
        return tweaks.containsKey(id)
                ? tweaks.get(id)
                : null;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return editingIDs.size();
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }
}
