package mcjty.xnet.blocks.controller;

import mcjty.lib.gui.events.BlockRenderEvent;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.network.Argument;
import mcjty.lib.network.ArgumentType;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.tools.MinecraftTools;
import mcjty.lib.varia.RedstoneMode;
import mcjty.xnet.api.channels.RSMode;
import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.network.XNetMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEditorPanel implements IEditorGui {

    private final Panel panel;
    private final Minecraft mc;
    private final GuiController gui;
    protected final Map<String, Object> data;
    protected final Map<String, Widget> components = new HashMap<>();

    private int x;
    private int y;

    protected abstract void update(String tag, Object value);

    protected void performUpdate(Argument[] args, int i, String cmd) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object o = entry.getValue();
            if (o instanceof String) {
                args[i++] = new Argument(entry.getKey(), ArgumentType.TYPE_STRING, o);
            } else if (o instanceof Integer) {
                args[i++] = new Argument(entry.getKey(), ArgumentType.TYPE_INTEGER, o);
            } else if (o instanceof Boolean) {
                args[i++] = new Argument(entry.getKey(), ArgumentType.TYPE_BOOLEAN, o);
            } else if (o instanceof Double) {
                args[i++] = new Argument(entry.getKey(), ArgumentType.TYPE_DOUBLE, o);
            } else if (o instanceof ItemStack) {
                args[i++] = new Argument(entry.getKey(), ArgumentType.TYPE_STACK, o);
            } else {
                args[i++] = new Argument(entry.getKey(), ArgumentType.TYPE_STRING, o);
            }
        }

        gui.sendServerCommand(XNetMessages.INSTANCE, cmd, args);
        gui.refresh();
    }

    public AbstractEditorPanel(Panel panel, Minecraft mc, GuiController gui) {
        this.panel = panel;
        this.mc = mc;
        this.gui = gui;
        x = 4;
        y = 3;
        data = new HashMap<>();
    }

    @Override
    public IEditorGui move(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public IEditorGui shift(int x) {
        this.x += x;
        return this;
    }

    private void fitWidth(int w) {
        if (x + w > panel.getBounds().width) {
            nl();
        }
    }

    @Override
    public IEditorGui label(String txt) {
        int w = mc.fontRenderer.getStringWidth(txt)+5;
        fitWidth(w);
        Label label = new Label(mc, gui).setText(txt);
        label.setLayoutHint(new PositionalLayout.PositionalHint(x, y, w, 14));
        panel.addChild(label);
        x += w;
        return this;
    }

    @Override
    public IEditorGui text(String tag, String tooltip, String value) {
        int w = 45;
        fitWidth(w);
        TextField text = new TextField(mc, gui).setText(value)
                .setTooltips(tooltip)
                .setLayoutHint(new PositionalLayout.PositionalHint(x, y, w, 14));
        data.put(tag, value);
        text.addTextEnterEvent((parent, newText) -> update(tag, newText));
        panel.addChild(text);
        components.put(tag, text);
        x += w;
        return this;
    }

    private Integer parseInt(String i) {
        if (i == null || i.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(i);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public IEditorGui integer(String tag, String tooltip, Integer value) {
        int w = 45;
        fitWidth(w);
        TextField text = new TextField(mc, gui).setText(value == null ? "" : value.toString())
                .setTooltips(tag)
                .setLayoutHint(new PositionalLayout.PositionalHint(x, y, w, 14));
        data.put(tag, value);
        text.addTextEnterEvent((parent, newText) -> update(tag, parseInt(newText)));
        panel.addChild(text);
        components.put(tag, text);
        x += w;
        return this;
    }

    private Double parseDouble(String i) {
        if (i == null || i.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(i);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public IEditorGui real(String tag, String tooltip, Double value) {
        int w = 45;
        fitWidth(w);
        TextField text = new TextField(mc, gui).setText(value == null ? "" : value.toString())
                .setTooltips(tooltip)
                .setLayoutHint(new PositionalLayout.PositionalHint(x, y, w, 14));
        data.put(tag, value);
        text.addTextEnterEvent((parent, newText) -> update(tag, parseDouble(newText)));
        panel.addChild(text);
        components.put(tag, text);
        x += w;
        return this;
    }

    @Override
    public IEditorGui toggle(String tag, String tooltip, boolean value) {
        int w = 12;
        fitWidth(w);
        ToggleButton toggle = new ToggleButton(mc, gui).setCheckMarker(true).setPressed(value)
                .setTooltips(tooltip)
                .setLayoutHint(new PositionalLayout.PositionalHint(x, y, w, 14));
        data.put(tag, value);
        toggle.addButtonEvent(parent -> update(tag, toggle.isPressed()));
        panel.addChild(toggle);
        components.put(tag, toggle);
        x += w;
        return this;
    }

    @Override
    public IEditorGui choices(String tag, String tooltip, String current, String... values) {
        int w = 10;
        for (String s : values) {
            w = Math.max(w, mc.fontRenderer.getStringWidth(s) + 15);
        }
        fitWidth(w);
        ChoiceLabel choice = new ChoiceLabel(mc, gui).addChoices(values).setChoice(current)
                .setTooltips(tooltip)
                .setLayoutHint(new PositionalLayout.PositionalHint(x, y, w, 14));
        data.put(tag, current);
        choice.addChoiceEvent((parent, newChoice) -> update(tag, newChoice));
        panel.addChild(choice);
        components.put(tag, choice);
        x += w;
        return this;
    }

    @Override
    public <T extends Enum<T>> IEditorGui choices(String tag, String tooltip, T current, T... values) {
        String[] strings = new String[values.length];
        int i = 0;
        for (T s : values) {
            strings[i++] = StringUtils.capitalize(s.name().toLowerCase());
        }
        return choices(tag, tooltip, StringUtils.capitalize(current.name().toLowerCase()), strings);
    }

    @Override
    public IEditorGui redstoneMode(String tag, RSMode current) {
        int w = 16;
        fitWidth(w);
        ImageChoiceLabel redstoneMode = new ImageChoiceLabel(mc, gui)
                .addChoice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", GuiController.iconGuiElements, 0, 0)
                .addChoice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", GuiController.iconGuiElements, 16, 0)
                .addChoice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", GuiController.iconGuiElements, 32, 0);
        switch (current) {
            case IGNORED:
                redstoneMode.setCurrentChoice(RedstoneMode.REDSTONE_IGNORED.getDescription());
                break;
            case OFF:
                redstoneMode.setCurrentChoice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription());
                break;
            case ON:
                redstoneMode.setCurrentChoice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription());
                break;
        }
        redstoneMode.setLayoutHint(new PositionalLayout.PositionalHint(x, y-1, w, 16));
        data.put(tag, current.name());
        redstoneMode.addChoiceEvent((parent, newChoice) -> update(tag, newChoice));
        panel.addChild(redstoneMode);
        components.put(tag, redstoneMode);
        x += w;
        return this;
    }

    @Override
    public IEditorGui ghostSlot(String tag, ItemStack stack) {
        int w = 16;
        fitWidth(w);
        BlockRender blockRender = new BlockRender(mc, gui)
                .setRenderItem(stack)
                .setDesiredWidth(18).setDesiredHeight(18)
                .setFilledRectThickness(1).setFilledBackground(0xff555555);
        blockRender.addSelectionEvent(new BlockRenderEvent() {
            @Override
            public void select(Widget widget) {
                ItemStack holding = MinecraftTools.getPlayer(Minecraft.getMinecraft()).inventory.getItemStack();
                if (ItemStackTools.isEmpty(holding)) {
                    update(tag, holding);
                    blockRender.setRenderItem(null);
                } else {
                    ItemStack copy = holding.copy();
                    ItemStackTools.setStackSize(copy, 1);
                    blockRender.setRenderItem(copy);
                    update(tag, copy);
                }
            }

            @Override
            public void doubleClick(Widget widget) {

            }
        });
        blockRender.setLayoutHint(new PositionalLayout.PositionalHint(x, y-1, w, 18));
        data.put(tag, stack);
        panel.addChild(blockRender);
        components.put(tag, blockRender);
        x += w;
        return this;
    }

    @Override
    public IEditorGui nl() {
        y += 16;
        x = 4;
        return this;
    }
}