package net.kroet.tradeshuffler.shared.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import net.kroet.tradeshuffler.common.config.TradeShufflerClientConfig;
import net.kroet.tradeshuffler.common.config.UpdateChannel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

/**
 * TradeShuffler client settings in vanilla options-layout styling, shared by
 * both loaders (Mod Menu on Fabric, the mod list on NeoForge). Each row has an
 * info icon whose tooltip explains the values.
 */
public final class TradeShufflerConfigScreen extends OptionsSubScreen {
    private static final String KEY = "tradeshuffler.config.";
    private static final int ROW_HEIGHT = 20;
    private static final int LABEL_WIDTH = 150;
    private static final int CONTROL_WIDTH = 150;
    private static final int FOOTER_BUTTON_WIDTH = 150;
    private static final int INFO_ICON_SIZE = 10;
    private static final int INFO_ICON_BG_COLOR = 0xAA2E5A88;
    private static final int INFO_ICON_TEXT_COLOR = 0xFFFFFFFF;
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    private static final int SECTION_COLOR = 0xFFE7E7E7;
    private static final int INVALID_TEXT_COLOR = 0xFFFF5555;

    private final Screen parent;

    private boolean shuffleButtonEnabled;
    private boolean previewEnabled;
    private boolean previewLevelStripe;
    private boolean updateCheckEnabled;
    private UpdateChannel updateChannel;
    private final TradeShufflerClientConfig initial;
    // Typed text survives rebuilding the widgets, e.g. when the window is resized.
    private final Map<String, String> fieldTexts = new HashMap<>();
    private @Nullable Button saveButton;

    private @Nullable OptionsList optionsList;
    private @Nullable Button shuffleButtonToggle;
    private @Nullable EditBox offsetXField;
    private @Nullable EditBox offsetYField;
    private @Nullable Button previewToggle;
    private @Nullable EditBox dimAlphaField;
    private @Nullable Button stripeToggle;
    private @Nullable Button updateCheckToggle;
    private @Nullable Button updateChannelButton;

    public TradeShufflerConfigScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.translatable(KEY + "title"));
        this.parent = parent;
        this.initial = ClientConfigHolder.current();
        this.shuffleButtonEnabled = initial.shuffleButtonEnabled();
        this.previewEnabled = initial.previewEnabled();
        this.previewLevelStripe = initial.previewLevelStripe();
        this.updateCheckEnabled = initial.updateCheckEnabled();
        this.updateChannel = initial.updateChannel();
    }

    @Override
    protected void addContents() {
        this.optionsList = this.layout.addToContents(new OptionsList(this.minecraft, this.width, this));
        this.list = this.optionsList;
        addOptions();
    }

    @Override
    protected void addOptions() {
        if (optionsList == null) {
            return;
        }

        addSectionRow(Component.translatable(KEY + "section.shuffle"));
        shuffleButtonToggle = toggleButton(button -> {
            shuffleButtonEnabled = !shuffleButtonEnabled;
            refresh();
        });
        addLabeledRow("shuffle_button.enabled", onOffTooltip("shuffle_button.enabled"), shuffleButtonToggle);
        offsetXField = numberField("shuffle_button.offset_x", initial.shuffleButtonOffsetX(), 3);
        addLabeledRow("shuffle_button.offset_x", rangeTooltip(TradeShufflerClientConfig.MIN_BUTTON_OFFSET, TradeShufflerClientConfig.MAX_BUTTON_OFFSET,
                "shuffle_button.offset_x"), offsetXField);
        offsetYField = numberField("shuffle_button.offset_y", initial.shuffleButtonOffsetY(), 3);
        addLabeledRow("shuffle_button.offset_y", rangeTooltip(TradeShufflerClientConfig.MIN_BUTTON_OFFSET, TradeShufflerClientConfig.MAX_BUTTON_OFFSET,
                "shuffle_button.offset_y"), offsetYField);

        addSectionRow(Component.translatable(KEY + "section.preview"));
        previewToggle = toggleButton(button -> {
            previewEnabled = !previewEnabled;
            refresh();
        });
        addLabeledRow("preview.enabled", onOffTooltip("preview.enabled"), previewToggle);
        dimAlphaField = numberField("preview.dim_alpha", initial.previewDimAlpha(), 3);
        addLabeledRow("preview.dim_alpha", rangeTooltip(TradeShufflerClientConfig.MIN_PREVIEW_DIM_ALPHA, TradeShufflerClientConfig.MAX_PREVIEW_DIM_ALPHA,
                "preview.dim_alpha"), dimAlphaField);
        stripeToggle = toggleButton(button -> {
            previewLevelStripe = !previewLevelStripe;
            refresh();
        });
        addLabeledRow("preview.level_stripe", onOffTooltip("preview.level_stripe"), stripeToggle);

        addSectionRow(Component.translatable(KEY + "section.updates"));
        updateCheckToggle = toggleButton(button -> {
            updateCheckEnabled = !updateCheckEnabled;
            refresh();
        });
        addLabeledRow("update.enabled", onOffTooltip("update.enabled"), updateCheckToggle);
        updateChannelButton = toggleButton(button -> {
            updateChannel = nextChannel(updateChannel);
            refresh();
        });
        addLabeledRow("update.channel", List.of(
                tooltipLine(Component.translatable(KEY + "update.channel.stable"), KEY + "update.channel.tooltip.stable.desc"),
                tooltipLine(Component.translatable(KEY + "update.channel.beta"), KEY + "update.channel.tooltip.beta.desc"),
                tooltipLine(Component.translatable(KEY + "update.channel.alpha"), KEY + "update.channel.tooltip.alpha.desc")),
                updateChannelButton);

        refresh();
    }

    @Override
    protected void addFooter() {
        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(Component.translatable(KEY + "button.cancel"), button -> onClose()).width(FOOTER_BUTTON_WIDTH).build());
        saveButton = footer.addChild(Button.builder(Component.translatable(KEY + "button.save_close"), button -> saveAndClose()).width(FOOTER_BUTTON_WIDTH).build());
        refresh();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }

    private void saveAndClose() {
        ClientConfigHolder.save(new TradeShufflerClientConfig(
                shuffleButtonEnabled,
                parse(offsetXField, initial.shuffleButtonOffsetX(), TradeShufflerClientConfig::clampButtonOffset),
                parse(offsetYField, initial.shuffleButtonOffsetY(), TradeShufflerClientConfig::clampButtonOffset),
                previewEnabled,
                parse(dimAlphaField, initial.previewDimAlpha(), TradeShufflerClientConfig::clampPreviewDimAlpha),
                previewLevelStripe,
                updateCheckEnabled,
                updateChannel));
        onClose();
    }

    private void refresh() {
        setMessage(shuffleButtonToggle, onOff(shuffleButtonEnabled));
        setEnabled(offsetXField, shuffleButtonEnabled);
        setEnabled(offsetYField, shuffleButtonEnabled);
        setMessage(previewToggle, onOff(previewEnabled));
        setEnabled(dimAlphaField, previewEnabled);
        setMessage(stripeToggle, onOff(previewLevelStripe));
        if (stripeToggle != null) {
            stripeToggle.active = previewEnabled;
        }
        setMessage(updateCheckToggle, onOff(updateCheckEnabled));
        if (updateChannelButton != null) {
            updateChannelButton.setMessage(updateCheckEnabled
                    ? Component.translatable(KEY + "update.channel." + updateChannel.name().toLowerCase(Locale.ROOT))
                    : Component.translatable(KEY + "update.channel.disabled"));
            updateChannelButton.active = updateCheckEnabled;
        }
        boolean valid = markValidity(offsetXField) & markValidity(offsetYField) & markValidity(dimAlphaField);
        if (saveButton != null) {
            saveButton.active = valid;
        }
    }

    private static void setMessage(@Nullable Button button, Component message) {
        if (button != null) {
            button.setMessage(message);
        }
    }

    private static void setEnabled(@Nullable EditBox field, boolean enabled) {
        if (field != null) {
            field.active = enabled;
            field.setEditable(enabled);
        }
    }

    private Button toggleButton(Button.OnPress onPress) {
        return Button.builder(Component.empty(), onPress).bounds(0, 0, CONTROL_WIDTH, ROW_HEIGHT).build();
    }

    private EditBox numberField(String setting, int value, int maxLength) {
        EditBox field = new EditBox(this.font, 0, 0, CONTROL_WIDTH, ROW_HEIGHT, Component.translatable(KEY + setting));
        field.setMaxLength(maxLength);
        field.setValue(fieldTexts.getOrDefault(setting, Integer.toString(value)));
        field.setResponder(text -> {
            fieldTexts.put(setting, text);
            refresh();
        });
        return field;
    }

    private static boolean isNumber(@Nullable EditBox field) {
        if (field == null) {
            return true;
        }
        try {
            Integer.parseInt(field.getValue().trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Marks an invalid number red; returns whether the field is valid. */
    private static boolean markValidity(@Nullable EditBox field) {
        boolean valid = isNumber(field);
        if (field != null) {
            field.setTextColor(valid ? EditBox.DEFAULT_TEXT_COLOR : INVALID_TEXT_COLOR);
        }
        return valid;
    }

    private void addLabeledRow(String setting, List<Component> tooltip, AbstractWidget control) {
        control.setWidth(CONTROL_WIDTH);
        if (optionsList != null) {
            optionsList.addSmall(new InfoLabelWidget(Component.translatable(KEY + setting), tooltip), control);
        }
    }

    private void addSectionRow(Component text) {
        if (optionsList != null) {
            optionsList.addSmall(new SectionLabelWidget(text), new SpacerWidget());
        }
    }

    private static List<Component> onOffTooltip(String setting) {
        return List.of(
                tooltipLine(onOff(true), KEY + setting + ".tooltip.on.desc"),
                tooltipLine(onOff(false), KEY + setting + ".tooltip.off.desc"));
    }

    private static List<Component> rangeTooltip(int min, int max, String setting) {
        List<Component> lines = new ArrayList<>();
        lines.add(tooltipLine(Component.literal(min + " – " + max), KEY + setting + ".tooltip.range.desc"));
        return lines;
    }

    private static Component tooltipLine(Component value, String descriptionKey) {
        MutableComponent line = Component.empty();
        line.append(value.copy().withStyle(ChatFormatting.BOLD));
        line.append(Component.literal(": "));
        line.append(Component.translatable(descriptionKey));
        return line;
    }

    private static Component onOff(boolean value) {
        return Component.translatable(KEY + (value ? "value.on" : "value.off"));
    }

    private static int parse(@Nullable EditBox field, int fallback, IntUnaryOperator clamp) {
        if (field == null) {
            return fallback;
        }
        try {
            return clamp.applyAsInt(Integer.parseInt(field.getValue().trim()));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static UpdateChannel nextChannel(UpdateChannel current) {
        return switch (current) {
            case STABLE -> UpdateChannel.BETA;
            case BETA -> UpdateChannel.ALPHA;
            case ALPHA -> UpdateChannel.STABLE;
        };
    }

    private final class InfoLabelWidget extends AbstractWidget {
        private final List<Component> tooltip;

        InfoLabelWidget(Component label, List<Component> tooltip) {
            super(0, 0, LABEL_WIDTH, ROW_HEIGHT, label);
            this.tooltip = tooltip;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int textY = getY() + (height - 9) / 2;
            graphics.text(font, getMessage(), getX(), textY, LABEL_COLOR, true);

            int iconX = getX() + width - INFO_ICON_SIZE;
            int iconY = getY() + (height - INFO_ICON_SIZE) / 2;
            graphics.fill(iconX, iconY, iconX + INFO_ICON_SIZE, iconY + INFO_ICON_SIZE, INFO_ICON_BG_COLOR);
            graphics.text(font, Component.literal("i"), iconX + 3, iconY + 1, INFO_ICON_TEXT_COLOR, false);

            boolean iconHovered = mouseX >= iconX && mouseX < iconX + INFO_ICON_SIZE && mouseY >= iconY && mouseY < iconY + INFO_ICON_SIZE;
            if (iconHovered && !tooltip.isEmpty()) {
                graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private final class SectionLabelWidget extends AbstractWidget {
        SectionLabelWidget(Component label) {
            super(0, 0, LABEL_WIDTH, ROW_HEIGHT, label);
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int centeredX = TradeShufflerConfigScreen.this.width / 2 - font.width(getMessage()) / 2;
            graphics.text(font, getMessage(), centeredX, getY() + (height - 9) / 2, SECTION_COLOR, true);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private static final class SpacerWidget extends AbstractWidget {
        SpacerWidget() {
            super(0, 0, CONTROL_WIDTH, ROW_HEIGHT, Component.empty());
            this.active = false;
            this.visible = false;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
        }
    }
}
