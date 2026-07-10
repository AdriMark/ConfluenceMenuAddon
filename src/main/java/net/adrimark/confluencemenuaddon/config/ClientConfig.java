package net.adrimark.confluencemenuaddon.config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;

import net.adrimark.confluencemenuaddon.ConfluenceMenuAddon;

// Read live by the render mixin each frame, so edits apply without a restart.
@EventBusSubscriber(modid = ConfluenceMenuAddon.MODID)
public class ClientConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLE_MAIN_MENU;
    private static final ModConfigSpec.BooleanValue ENABLE_WORLD_SELECT_SCREEN;
    private static final ModConfigSpec.BooleanValue ENABLE_MULTIPLAYER_SCREEN;
    private static final ModConfigSpec.BooleanValue ENABLE_OPTIONS_SCREEN;
    private static final ModConfigSpec.BooleanValue ENABLE_CREATE_WORLD_SCREEN;
    private static final ModConfigSpec.BooleanValue ENABLE_MOD_LIST_SCREEN;
    private static final ModConfigSpec.BooleanValue ENABLE_CONFIG_SCREEN;
    private static final ModConfigSpec.BooleanValue ENABLE_ACCESSIBILITY_SCREEN;
    private static final ModConfigSpec.BooleanValue ENABLE_LANGUAGE_SCREEN;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ADDITIONAL_SCREENS;

    private static final ModConfigSpec.DoubleValue BACKGROUND_ANIMATION_SPEED;
    private static final ModConfigSpec.DoubleValue ENVIRONMENT_SCROLL_SPEED;
    private static final ModConfigSpec.EnumValue<ScrollDirection> ENVIRONMENT_SCROLL_DIRECTION;

    private static final ModConfigSpec.BooleanValue ENABLE_SUN_CLICK_LOGO;
    private static final ModConfigSpec.BooleanValue ENABLE_CONFLUENCE_SPLASH_TEXT;

    static {
        BUILDER.comment("Which screens show the animated background.").push("menus");

        ENABLE_MAIN_MENU = BUILDER
                .comment("Show the animated background on the main menu.")
                .define("enableMainMenu", true);

        ENABLE_WORLD_SELECT_SCREEN = BUILDER
                .comment("Show the animated background on the world select screen.")
                .define("enableWorldSelectScreen", true);

        ENABLE_MULTIPLAYER_SCREEN = BUILDER
                .comment("Show the animated background on the multiplayer server list.")
                .define("enableMultiplayerScreen", true);

        ENABLE_OPTIONS_SCREEN = BUILDER
                .comment("Show the animated background on the options screen.")
                .define("enableOptionsScreen", true);

        ENABLE_CREATE_WORLD_SCREEN = BUILDER
                .comment("Show the animated background on the create world screen.")
                .define("enableCreateWorldScreen", true);

        ENABLE_MOD_LIST_SCREEN = BUILDER
                .comment("Show the animated background on the mods list screen.")
                .define("enableModListScreen", true);

        ENABLE_CONFIG_SCREEN = BUILDER
                .comment("Show the animated background on a mod's configuration screen"
                        + " (opened via the \"Config\" button on the mods list).")
                .define("enableConfigScreen", true);

        ENABLE_ACCESSIBILITY_SCREEN = BUILDER
                .comment("Show the animated background on the accessibility settings screen.")
                .define("enableAccessibilityScreen", true);

        ENABLE_LANGUAGE_SCREEN = BUILDER
                .comment("Show the animated background on the language selection screen.")
                .define("enableLanguageScreen", true);

        ADDITIONAL_SCREENS = BUILDER
                .comment(
                        "Other screens to show the background on, listed by their full class name.",
                        "Example: \"net.minecraft.client.gui.screens.PauseScreen\"",
                        "Only works on screens that use the default background; screens with a fully"
                                + " custom background of their own will need extra work to support."
                )
                .defineListAllowEmpty("additionalScreens", List.of(), () -> "", ClientConfig::validateClassName);

        BUILDER.pop();

        BUILDER.comment("How the animated background moves.").push("animation");

        BACKGROUND_ANIMATION_SPEED = BUILDER
                .comment("How fast the background sky, sun, moon, and clouds scroll. 0 keeps it still.")
                .defineInRange("backgroundAnimationSpeed", 1.0D, 0.0D, 10.0D);

        ENVIRONMENT_SCROLL_SPEED = BUILDER
                .comment("How fast the background scenery scrolls sideways. 0 keeps it still.")
                .defineInRange("environmentScrollSpeed", 4.0D, 0.0D, 10.0D);

        ENVIRONMENT_SCROLL_DIRECTION = BUILDER
                .comment("Which direction the background scenery scrolls.")
                .defineEnum("environmentScrollDirection", ScrollDirection.LEFT_TO_RIGHT);

        BUILDER.pop();

        BUILDER.comment("Hidden extras.").push("extra");

        ENABLE_SUN_CLICK_LOGO = BUILDER
                .comment("Clicking the sun or moon on the animated background swaps the main menu's"
                        + " \"MINECRAFT\" logo to an older version until the game restarts.")
                .define("enableSunClickLogo", true);

        ENABLE_CONFLUENCE_SPLASH_TEXT = BUILDER
                .comment("Replace the main menu's splash text with one of Confluence: Otherworld's own"
                        + " randomized window title lines instead of a vanilla Minecraft splash.")
                .define("enableConfluenceSplashText", true);

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    /** Which way the parallax scenery drifts across the screen. */
    public enum ScrollDirection implements TranslatableEnum {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT;

        @Override
        public Component getTranslatedName() {
            return Component.translatable(ConfluenceMenuAddon.MODID
                    + ".configuration.environmentScrollDirection." + name());
        }
    }

    public static volatile boolean enableMainMenu = true;
    public static volatile boolean enableWorldSelectScreen = true;
    public static volatile boolean enableMultiplayerScreen = true;
    public static volatile boolean enableOptionsScreen = true;
    public static volatile boolean enableCreateWorldScreen = true;
    public static volatile boolean enableModListScreen = true;
    public static volatile boolean enableConfigScreen = true;
    public static volatile boolean enableAccessibilityScreen = true;
    public static volatile boolean enableLanguageScreen = true;
    public static volatile double backgroundAnimationSpeed = 1.0D;
    public static volatile double environmentScrollSpeed = 4.0D;
    public static volatile ScrollDirection environmentScrollDirection = ScrollDirection.LEFT_TO_RIGHT;
    public static volatile boolean enableSunClickLogo = true;
    public static volatile boolean enableConfluenceSplashText = true;
    public static volatile Set<String> additionalScreens = Set.of();

    private static boolean validateClassName(final Object obj) {
        return obj instanceof String;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enableMainMenu = ENABLE_MAIN_MENU.get();
        enableWorldSelectScreen = ENABLE_WORLD_SELECT_SCREEN.get();
        enableMultiplayerScreen = ENABLE_MULTIPLAYER_SCREEN.get();
        enableOptionsScreen = ENABLE_OPTIONS_SCREEN.get();
        enableCreateWorldScreen = ENABLE_CREATE_WORLD_SCREEN.get();
        enableModListScreen = ENABLE_MOD_LIST_SCREEN.get();
        enableConfigScreen = ENABLE_CONFIG_SCREEN.get();
        enableAccessibilityScreen = ENABLE_ACCESSIBILITY_SCREEN.get();
        enableLanguageScreen = ENABLE_LANGUAGE_SCREEN.get();
        backgroundAnimationSpeed = BACKGROUND_ANIMATION_SPEED.get();
        environmentScrollSpeed = ENVIRONMENT_SCROLL_SPEED.get();
        environmentScrollDirection = ENVIRONMENT_SCROLL_DIRECTION.get();
        enableSunClickLogo = ENABLE_SUN_CLICK_LOGO.get();
        enableConfluenceSplashText = ENABLE_CONFLUENCE_SPLASH_TEXT.get();
        additionalScreens = ADDITIONAL_SCREENS.get().stream().collect(Collectors.toUnmodifiableSet());
    }
}
