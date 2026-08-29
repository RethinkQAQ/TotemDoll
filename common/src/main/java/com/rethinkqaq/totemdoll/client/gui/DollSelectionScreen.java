/*
 * Totem Doll
 * Copyright (C) 2026 Rethink_QAQ
 *
 * This file is part of Totem Doll.
 *
 * Totem Doll is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, version 3 of the License.
 *
 * Totem Doll is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Totem Doll. If not, see <https://www.gnu.org/licenses/>.
 */

package com.rethinkqaq.totemdoll.client.gui;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBackground;
import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiDialogHost;
import com.rethinkqaq.configui.core.UiGrid;
import com.rethinkqaq.configui.core.UiPageHost;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.core.component.UiButton;
import com.rethinkqaq.configui.core.component.feedback.UiFeedbackType;
import com.rethinkqaq.configui.core.component.UiToggle;
import com.rethinkqaq.configui.core.component.input.UiNumberControl;
import com.rethinkqaq.configui.core.layout.UiHeader;
import com.rethinkqaq.configui.core.layout.UiHeaderStyle;
import com.rethinkqaq.configui.core.layout.UiSection;
import com.rethinkqaq.configui.core.layout.UiTemplate;
import com.rethinkqaq.configui.core.setting.UiNumberSpec;
import com.rethinkqaq.configui.core.setting.UiSetting;
import com.rethinkqaq.configui.minecraft.UiHost;
import com.rethinkqaq.configui.minecraft.MinecraftPreview;
import com.rethinkqaq.configui.minecraft.UiScreen;
import com.rethinkqaq.totemdoll.Constants;
import com.rethinkqaq.totemdoll.client.DollBoneRenderer;
import com.rethinkqaq.totemdoll.utils.DollGuiGraphics;
import com.rethinkqaq.totemdoll.client.TotemDollClient;
import com.rethinkqaq.totemdoll.config.TotemDollConfigRuntime;
import com.rethinkqaq.totemdoll.doll.DollAnimationManager;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.DollStyles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** RCUI-based style and configuration surface for Totem Doll. */
public final class DollSelectionScreen extends UiScreen implements DollScreenParent {
    private final Screen parent;
    private final UiDialogHost dialogs;

    public DollSelectionScreen(Screen parent) {
        this(parent, Tab.TEMPLATES);
    }

    public DollSelectionScreen(Screen parent, Tab tab) {
        super(parent, buildRoot(parent, tab), UiTheme.roseLight(), UiHost.LayoutMode.FULLSCREEN);
        this.parent = parent;
        host().background(UiBackground.translucent(0xE0FFFFFF));
        this.dialogs = (UiDialogHost) host().root();
        DollStyles.all().forEach(style -> DollAnimationManager.trigger(style, "on_screen_open"));
    }

    private static Ui.Node buildRoot(Screen parent, Tab tab) {
        UiPageHost pages = Ui.pageHost()
                .addPage(UiText.translatable("screen.totemdoll.templates"), stylesPage(false))
                .addPage(UiText.translatable("screen.totemdoll.my_styles"), stylesPage(true))
                .addPage(UiText.translatable("screen.totemdoll.settings"), settingsPage());
        pages.select(tab.ordinal());

        UiTemplate template = UiTemplate.topNavigation()
                .header(UiHeader.builder(UiText.translatable("screen.totemdoll.title"))
                        .subtitle(UiText.translatable("screen.totemdoll.subtitle"))
                        .style(UiHeaderStyle.TEXT)
                        .titleScale(1.25f)
                        .subtitleScale(.9f)
                        .responsive(true)
                        .build())
                .navigation(pages.navigation())
                .content(pages)
                .footer(Ui.row().gap(6)
                        .add(Ui.button(UiText.translatable("screen.totemdoll.import_pack"), () -> {
                            Screen current = DollScreenAdapter.currentScreen(Minecraft.getInstance());
                            if (current instanceof DollSelectionScreen selection) DollPackScreen.chooseZipAndImport(selection);
                        }).variant(Ui.ButtonVariant.OUTLINE))
                        .add(Ui.button(UiText.translatable("gui.done"), () -> closeToParent(parent)).variant(Ui.ButtonVariant.PRIMARY)))
                .footerAlignment(UiTemplate.FooterAlignment.END)
                .background(UiBackground.translucent(0xE0FFFFFF))
                .maxContentWidth(1200)
                .regionGap(12)
                .build();

        return Ui.dialogHost(template);
    }

    private static Ui.Node stylesPage(boolean local) {
        Ui.Column column = Ui.column().gap(14);
        if (!local && !DollStyles.contains(TotemDollConfigRuntime.selectedStyleId())) {
            column.add(Ui.alert(UiFeedbackType.WARNING, UiText.translatable(
                    "screen.totemdoll.style_unavailable", TotemDollConfigRuntime.selectedStyleId())));
        }
        List<DollStyle> styles = DollStyles.all().stream()
                .filter(style -> style.isLocal() == local)
                .sorted(styleComparator())
                .toList();
        if (styles.isEmpty()) {
            return column.add(Ui.section(UiText.translatable(local ? "screen.totemdoll.my_styles" : "screen.totemdoll.templates"))
                    .add(Ui.label(UiText.translatable(local ? "screen.totemdoll.empty_title" : "screen.totemdoll.empty_hint"))));
        }
        UiGrid grid = Ui.grid().minimumColumnWidth(156).maximumColumnWidth(188).gap(10);
        for (DollStyle style : styles) grid.add(styleCard(style, local));
        return column.add(Ui.section(UiText.translatable(local ? "screen.totemdoll.my_styles" : "screen.totemdoll.templates")).add(grid));
    }

    private static Ui.Node styleCard(DollStyle style, boolean local) {
        Ui.Node preview = new MinecraftPreview((graphics, bounds, clip) ->
                DollGuiPreview.render(
                        DollGuiGraphics.wrap(graphics), style,
                        Math.round(bounds.x()), Math.round(bounds.y()),
                        Math.round(bounds.width()), Math.round(bounds.height()), 37.6F,
                        Math.round(clip.x()), Math.round(clip.y()),
                        Math.round(clip.width()), Math.round(clip.height())))
                .preferredHeight(58);

        Runnable selectStyle = () -> {
            if (!style.isAvailable()) return;
            TotemDollConfigRuntime.select(style.id());
            DollAnimationManager.trigger(style, "on_select");
        };
        UiButton select = Ui.button(UiText.translatable("screen.totemdoll.select"), selectStyle)
                .preferredWidth(84)
                .variant(() -> isSelected(style) ? Ui.ButtonVariant.PRIMARY : Ui.ButtonVariant.OUTLINE);
        select.enabled(style.isAvailable());
        Ui.Row actions = Ui.row().gap(8).equalChildWidths(true).add(select);
        if (local) {
            actions.add(Ui.button(UiText.translatable("screen.totemdoll.manage"), () -> {
                Screen current = DollScreenAdapter.currentScreen(Minecraft.getInstance());
                if (current instanceof DollSelectionScreen selection) {
                    selection.dialogs.show(new DollStyleManageDialog(selection.dialogs, selection, style));
                }
            }).preferredWidth(84).variant(Ui.ButtonVariant.SECONDARY));
        } else if (style.supportsSkin()) {
            actions.add(Ui.button(UiText.translatable("screen.totemdoll.create_short"), () -> {
                Screen current = DollScreenAdapter.currentScreen(Minecraft.getInstance());
                if (current instanceof DollSelectionScreen selection) {
                    selection.dialogs.show(new DollCreateDialog(selection.dialogs, selection, style));
                }
            }).preferredWidth(84).variant(Ui.ButtonVariant.SECONDARY));
        }

        String origin = localizedOrigin(style);
        String availability = style.isAvailable()
                ? localized("screen.totemdoll.available")
                : localized("screen.totemdoll.unavailable_reason", style.invalidReason());
        String capabilities = capabilities(style);
        String sourceStatus = localized("screen.totemdoll.source_status", origin, availability);
        return Ui.tooltip(
                Ui.previewCard(UiText.literal(style.label().getString()), preview)
                        .description(UiText.literal(sourceStatus + "\n" + capabilities))
                        .previewHeight(58)
                        .selected(() -> isSelected(style))
                        .onClick(selectStyle)
                        .action(actions),
                UiText.literal(localized("screen.totemdoll.resource_id", style.id().toString()) + "\n"
                        + sourceStatus + "\n" + capabilities))
                .maxWidth(220)
                .maxLines(5);
    }

    private static Ui.Node settingsPage() {
        Ui.Column column = Ui.column().gap(14);
        UiSection section = Ui.section(UiText.translatable("screen.totemdoll.settings"))
                        .titleScale(1.25f);

        UiToggle enabled = Ui.toggle(UiText.translatable("screen.totemdoll.skin_layer_3d.enabled"),
                UiBinding.of(TotemDollConfigRuntime::skinLayer3dEnabled, value -> {
                    TotemDollConfigRuntime.setSkinLayer3dEnabled(value);
                    DollBoneRenderer.clearSkinLayerCache();
                }));
        section.add(Ui.formField(UiText.translatable("screen.totemdoll.skin_layer_3d.enabled"), enabled)
                .description(UiText.translatable("screen.totemdoll.skin_layer_3d.description")));

        UiSetting<Float> thicknessSetting = UiSetting.of(
                UiBinding.of(TotemDollConfigRuntime::skinLayer3dThickness, TotemDollConfigRuntime::setSkinLayer3dThickness),
                0.5F);
        UiNumberSpec<Float> thicknessSpec = UiNumberSpec.builder(UiNumberSpec.FLOAT)
                .range(TotemDollConfigRuntime.MIN_SKIN_LAYER_THICKNESS, TotemDollConfigRuntime.MAX_SKIN_LAYER_THICKNESS)
                .step(0.05)
                .formatter(value -> String.format(Locale.ROOT, "%.2f", value))
                .build();
        UiNumberControl<Float> thickness = Ui.numberControl(thicknessSetting, thicknessSpec);
        section.add(Ui.formField(UiText.translatable("screen.totemdoll.skin_layer_3d.thickness"), thickness)
                .description(UiText.translatable("screen.totemdoll.skin_layer_3d.thickness_hint")));

        UiSetting<Float> distanceSetting = UiSetting.of(
                UiBinding.of(TotemDollConfigRuntime::skinLayer3dDistance, TotemDollConfigRuntime::setSkinLayer3dDistance),
                TotemDollConfigRuntime.MIN_SKIN_LAYER_DISTANCE);
        UiNumberSpec<Float> distanceSpec = UiNumberSpec.builder(UiNumberSpec.FLOAT)
                .range(TotemDollConfigRuntime.MIN_SKIN_LAYER_DISTANCE, TotemDollConfigRuntime.MAX_SKIN_LAYER_DISTANCE)
                .step(1)
                .formatter(value -> String.format(Locale.ROOT, "%.0f", value))
                .build();
        UiNumberControl<Float> distance = Ui.numberControl(distanceSetting, distanceSpec);
        section.add(Ui.formField(UiText.translatable("screen.totemdoll.skin_layer_3d.distance"), distance)
                .description(UiText.translatable("screen.totemdoll.skin_layer_3d.distance_hint")));

        return column.add(section);
    }

    private static Comparator<DollStyle> styleComparator() {
        return Comparator.comparingInt((DollStyle style) -> style.id().equals(DollStyles.VANILLA_ID) ? 0 : 1)
                .thenComparing(style -> style.label().getString(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(style -> style.id().toString());
    }

    private static String capabilities(DollStyle style) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        if (style.hasDynamicTextures()) values.add(localized("screen.totemdoll.dynamic_texture"));
        if (style.hasDynamicModel()) values.add(localized("screen.totemdoll.dynamic_model"));
        if (style.hasAnimations()) values.add(localized("screen.totemdoll.animations"));
        if (style.supportsSkin()) values.add(localized("screen.totemdoll.skin_supported"));
        return values.isEmpty() ? localized("screen.totemdoll.capability_static") : String.join(" · ", values);
    }

    private static String localizedOrigin(DollStyle style) {
        return switch (style.origin()) {
            case BUILTIN -> localized("screen.totemdoll.origin_builtin");
            case RESOURCE_PACK -> localized("screen.totemdoll.origin_resource_pack");
            case IMPORTED -> localized("screen.totemdoll.origin_imported");
            case LOCAL -> localized("screen.totemdoll.origin_local");
        };
    }

    private static String localized(String key) {
        return net.minecraft.client.Minecraft.getInstance().font == null ? key
                : Component.translatable(key).getString();
    }

    private static String localized(String key, Object... args) {
        return net.minecraft.client.Minecraft.getInstance().font == null ? key
                : Component.translatable(key, args).getString();
    }

    private static boolean isSelected(DollStyle style) {
        return style.id().equals(TotemDollConfigRuntime.selectedStyleId());
    }

    private static void closeToParent(Screen parent) {
        TotemDollConfigRuntime.flush();
        DollScreenAdapter.setScreen(Minecraft.getInstance(), DollScreenAdapter.rootParent(parent));
    }

    void showImportError(Component message) {
        Constants.LOG.warn("Totem Doll style import failed: {}", message.getString());
        dialogs.show(new DollMessageDialog(dialogs,
                UiText.translatable("screen.totemdoll.import_error_title"), UiText.literal(message.getString())));
    }

    void reloadStyles(Tab tab) {
        TotemDollClient.reloadGeneratedStyles().thenRun(() -> Minecraft.getInstance().execute(() ->
                DollScreenAdapter.setScreen(Minecraft.getInstance(), new DollSelectionScreen(rootParent(), tab))));
    }

    @Override public void onClose() {
        closeToParent(parent);
    }

    @Override public Screen rootParent() {
        return DollScreenAdapter.rootParent(parent);
    }

    public enum Tab {
        TEMPLATES,
        MY_STYLES,
        SETTINGS
    }
}
