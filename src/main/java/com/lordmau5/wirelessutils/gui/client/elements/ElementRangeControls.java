package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.tile.base.IConfigurableRange;
import com.lordmau5.wirelessutils.tile.base.IDirectionalMachine;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public class ElementRangeControls extends ElementContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");

    public static final String INTL_KEY = "btn." + WirelessUtils.MODID + ".range.";

    private final BaseGuiContainer gui;
    private final IConfigurableRange config;

    private final ElementContainedButton decHeight;
    private final ElementContainedButton incHeight;

    private final ElementContainedButton decLength;
    private final ElementContainedButton incLength;

    private final ElementContainedButton decWidth;
    private final ElementContainedButton incWidth;

    public ElementRangeControls(BaseGuiContainer gui, IConfigurableRange config, int posX, int posY) {
        super(gui, posX, posY, 30, 46);
        this.gui = gui;
        this.config = config;

        decHeight = new ElementContainedButton(this, 0, 0, "DecHeight", 176, 0, 176, 14, 176, 28, 14, 14, TEXTURE.toString());
        incHeight = new ElementContainedButton(this, 16, 0, "IncHeight", 190, 0, 190, 14, 190, 28, 14, 14, TEXTURE.toString());

        decLength = new ElementContainedButton(this, 0, 16, "DecLength", 176, 0, 176, 14, 176, 28, 14, 14, TEXTURE.toString());
        incLength = new ElementContainedButton(this, 16, 16, "IncLength", 190, 0, 190, 14, 190, 28, 14, 14, TEXTURE.toString());

        decWidth = new ElementContainedButton(this, 0, 32, "DecWidth", 176, 0, 176, 14, 176, 28, 14, 14, TEXTURE.toString());
        incWidth = new ElementContainedButton(this, 16, 32, "IncWidth", 190, 0, 190, 14, 190, 28, 14, 14, TEXTURE.toString());

        addElement(decHeight);
        addElement(incHeight);

        addElement(decLength);
        addElement(incLength);

        addElement(decWidth);
        addElement(incWidth);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);

        if ( config == null )
            return;

        if ( GuiScreen.isAltKeyDown() ) {
            FontRenderer fontRenderer = getFontRenderer();

            fontRenderer.drawString(StringHelper.localize(INTL_KEY + "volume"), posX - 70, posY + 4, 0x404040);
            drawVolume(config.getRangeHeight(), config.getRangeLength(), config.getRangeWidth(), posX - 62, posY + 14, 0);

            if ( ModConfig.common.area == ModConfig.Common.DirectionalArea.AREA ) {
                fontRenderer.drawString(StringHelper.localize(INTL_KEY + "max_volume"), posX - 70, posY + 28, 0x404040);
                int range = config.getRange();
                drawVolume(range, range, range, posX - 62, posY + 38, 0);
            }

        } else {
            drawRange(config.getRangeHeight(), posX - 22, posY + 4, 0);
            drawRange(config.getRangeLength(), posX - 22, posY + 20, 0);
            drawRange(config.getRangeWidth(), posX - 22, posY + 36, 0);

            gui.drawRightAlignedText(StringHelper.localize(INTL_KEY + "height"), posX - 28, posY + 4, 0x404040);
            gui.drawRightAlignedText(StringHelper.localize(INTL_KEY + "length"), posX - 28, posY + 20, 0x404040);
            gui.drawRightAlignedText(StringHelper.localize(INTL_KEY + "width"), posX - 28, posY + 36, 0x404040);
        }
    }

    protected void drawVolume(int height, int length, int width, int x, int y, int color) {
        height = 1 + (height * 2);
        length = 1 + (length * 2);
        width = 1 + (width * 2);
        getFontRenderer().drawString(StringHelper.formatNumber(height * length * width), x, y, color);
    }

    protected void drawRange(int range, int x, int y, int color) {
        gui.getFontRenderer().drawString(StringHelper.formatNumber((range * 2) + 1), x, y, color);
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        int range = config == null ? 0 : config.getRange();
        setVisible(range != 0);
        if ( range == 0 )
            return;

        int rangeHeight = config.getRangeHeight();
        int rangeLength = config.getRangeLength();
        int rangeWidth = config.getRangeWidth();

        boolean isMulti = GuiScreen.isCtrlKeyDown();
        boolean isAll = GuiScreen.isShiftKeyDown();

        if ( isAll ) {
            int minimum = Math.min(Math.min(rangeHeight, rangeLength), rangeWidth);

            boolean decEnabled = minimum > 0;
            boolean incEnabled = IDirectionalMachine.isRangeValid(range, rangeHeight + 1, rangeLength + 1, rangeWidth + 1);

            String decTooltip = decEnabled ? generateTooltip(false, true, isMulti ? minimum : 1) : null;
            String incTooltip = incEnabled ? generateTooltip(true, true, isMulti ? IDirectionalMachine.getMaximumAllIncrease(range, rangeHeight, rangeLength, rangeWidth) : 1) : null;

            decHeight.setToolTipLocalized(decTooltip).setEnabled(decEnabled);
            decLength.setToolTipLocalized(decTooltip).setEnabled(decEnabled);
            decWidth.setToolTipLocalized(decTooltip).setEnabled(decEnabled);

            incHeight.setToolTipLocalized(incTooltip).setEnabled(incEnabled);
            incLength.setToolTipLocalized(incTooltip).setEnabled(incEnabled);
            incWidth.setToolTipLocalized(incTooltip).setEnabled(incEnabled);

        } else {
            updateButton(decHeight, rangeHeight, false, isMulti);
            updateButton(incHeight, IDirectionalMachine.getMaximumHeightIncrease(range, rangeHeight, rangeLength, rangeWidth), true, isMulti);

            updateButton(decLength, rangeLength, false, isMulti);
            updateButton(incLength, IDirectionalMachine.getMaximumLengthIncrease(range, rangeHeight, rangeLength, rangeWidth), true, isMulti);

            updateButton(decWidth, rangeWidth, false, isMulti);
            updateButton(incWidth, IDirectionalMachine.getMaximumWidthIncrease(range, rangeHeight, rangeLength, rangeWidth), true, isMulti);
        }
    }

    public void updateButton(ElementContainedButton button, int value, boolean increment, boolean isMulti) {
        boolean enabled = value > 0;
        String tooltip = enabled ? generateTooltip(increment, false, isMulti ? value : 1) : null;
        button.setToolTipLocalized(tooltip).setEnabled(enabled);
    }

    public String generateTooltip(boolean increment, boolean all, int amount) {
        return new TextComponentTranslation(
                "btn." + WirelessUtils.MODID + (increment ? ".inc_area." : ".dec_area.") + (all ? "all" : "one"),
                (amount * 2)
        ).getFormattedText();
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        if ( config == null )
            return;

        float pitch = 0.7F;
        int range = config.getRange();
        int rangeHeight = config.getRangeHeight();
        int rangeLength = config.getRangeLength();
        int rangeWidth = config.getRangeWidth();

        if ( GuiScreen.isShiftKeyDown() ) {
            int amount;
            switch (buttonName) {
                case "DecHeight":
                case "DecLength":
                case "DecWidth":
                    amount = -1;
                    if ( GuiScreen.isCtrlKeyDown() )
                        amount = -(Math.min(Math.min(rangeHeight, rangeLength), rangeWidth));
                    break;

                case "IncHeight":
                case "IncLength":
                case "IncWidth":
                    amount = 1;
                    if ( GuiScreen.isCtrlKeyDown() )
                        amount = IDirectionalMachine.getMaximumAllIncrease(range, rangeHeight, rangeLength, rangeWidth);
                    break;
                default:
                    amount = 0;
            }

            config.setRanges(rangeHeight + amount, rangeLength + amount, rangeWidth + amount);

        } else {
            boolean max = GuiScreen.isCtrlKeyDown();

            switch (buttonName) {
                case "DecHeight":
                    config.setRangeHeight(max ? 0 : rangeHeight - 1);
                    break;

                case "IncHeight":
                    config.setRangeHeight(rangeHeight + (max ?
                            IDirectionalMachine.getMaximumHeightIncrease(range, rangeHeight, rangeLength, rangeWidth) :
                            1));
                    break;

                case "DecLength":
                    config.setRangeLength(max ? 0 : rangeLength - 1);
                    break;

                case "IncLength":
                    config.setRangeLength(rangeLength + (max ?
                            IDirectionalMachine.getMaximumLengthIncrease(range, rangeHeight, rangeLength, rangeWidth) :
                            1));
                    break;

                case "DecWidth":
                    config.setRangeWidth(max ? 0 : rangeWidth - 1);
                    break;

                case "IncWidth":
                    config.setRangeWidth(rangeWidth + (max ?
                            IDirectionalMachine.getMaximumWidthIncrease(range, rangeHeight, rangeLength, rangeWidth) :
                            1));
                    break;
            }
        }

        BaseGuiContainer.playClickSound(pitch);
        config.saveRanges();
    }
}
