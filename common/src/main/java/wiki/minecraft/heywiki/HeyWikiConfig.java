package wiki.minecraft.heywiki;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static dev.architectury.platform.Platform.getConfigFolder;

/**
 * The configuration for the Hey Wiki mod.
 */
public class HeyWikiConfig {
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Codec<HeyWikiConfig> CODEC =
            RecordCodecBuilder
                    .create(instance -> instance
                            .group(
                                    Codec.BOOL.fieldOf("requiresConfirmation")
                                              .orElse(true)
                                              .forGetter(HeyWikiConfig::requiresConfirmation),
                                    Codec.BOOL.fieldOf("requiresConfirmationCommand")
                                              .orElse(false)
                                              .forGetter(HeyWikiConfig::requiresConfirmationCommand),
                                    Codec.DOUBLE.validate(
                                                 value -> {
                                                     var min = 0D;
                                                     var max = 64D;
                                                     return value.compareTo(min) >= 0 &&
                                                            value.compareTo(max) <= 0
                                                             ? DataResult.success(value)
                                                             : DataResult.error(
                                                                     () -> "Value must be within range [" + min +
                                                                           ";" + max + "]: " + value);
                                                 })
                                                .fieldOf("raycastReach")
                                                .orElse(5.2D)
                                                .forGetter(HeyWikiConfig::raycastReach),
                                    Codec.BOOL.fieldOf("raycastAllowFluid")
                                              .orElse(false)
                                              .forGetter(HeyWikiConfig::raycastAllowFluid),
                                    Codec.STRING.fieldOf("language")
                                                .orElse("auto")
                                                .forGetter(HeyWikiConfig::language),
                                    Codec.STRING.fieldOf("zhVariant")
                                                .orElse("auto")
                                                .forGetter(HeyWikiConfig::zhVariant)
                                  )
                            .apply(instance, HeyWikiConfig::new));

    /**
     * Whether the user should be prompted for confirmation before opening a wiki page.
     */
    public boolean requiresConfirmation() {
        return requiresConfirmation;
    }

    private boolean requiresConfirmation;

    /**
     * Whether the user should be prompted for confirmation before opening a wiki page using a command.
     */
    public boolean requiresConfirmationCommand() {
        return requiresConfirmationCommand;
    }

    private boolean requiresConfirmationCommand;

    /**
     * The distance at which the player can raycast to find a {@link wiki.minecraft.heywiki.wiki.Target Target}.
     */
    public double raycastReach() {
        return raycastReach;
    }

    private double raycastReach;

    /**
     * When true, the raycast will hit fluid blocks.
     */
    public boolean raycastAllowFluid() {
        return raycastAllowFluid;
    }

    private boolean raycastAllowFluid;

    /**
     * The language to use for wiki pages.
     */
    public String language() {
        return language;
    }

    private String language;

    /**
     * The variant of Chinese to use for wiki pages.
     */
    public String zhVariant() {
        return zhVariant;
    }

    private String zhVariant;


    private HeyWikiConfig(boolean requiresConfirmation, boolean requiresConfirmationCommand, double raycastReach,
                          boolean raycastAllowFluid, String language, String zhVariant) {
        this.requiresConfirmation = requiresConfirmation;
        this.requiresConfirmationCommand = requiresConfirmationCommand;
        this.raycastReach = raycastReach;
        this.raycastAllowFluid = raycastAllowFluid;
        this.language = language;
        this.zhVariant = zhVariant;
    }

    /**
     * Creates a GUI for the configuration.
     *
     * @param parent The parent screen.
     * @return The configuration screen.
     */
    public Screen createGui(Screen parent) {
        AtomicReference<Boolean> requireReload = new AtomicReference<>(false);

        List<String> languages = new ArrayList<>(MOD.familyManager().getAllAvailableLanguages());
        languages.addFirst("auto");

        ConfigBuilder builder = ConfigBuilder.create()
                                             .setParentScreen(parent)
                                             .setTitle(Text.translatable("options.heywiki.title"));

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("options.heywiki.general"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        general.addEntry(entryBuilder
                                 .startBooleanToggle(Text.translatable("options.heywiki.requires_confirmation.name"),
                                                     this.requiresConfirmation())
                                 .setDefaultValue(true)
                                 .setTooltip(Text.translatable("options.heywiki.requires_confirmation.description"))
                                 .setSaveConsumer(newValue -> this.requiresConfirmation = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .startBooleanToggle(
                                         Text.translatable("options.heywiki.requires_confirmation_command.name"),
                                         this.requiresConfirmationCommand())
                                 .setDefaultValue(false)
                                 .setTooltip(
                                         Text.translatable("options.heywiki.requires_confirmation_command.description"))
                                 .setSaveConsumer(newValue -> this.requiresConfirmationCommand = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .startDoubleField(Text.translatable("options.heywiki.raycast_reach.name"),
                                                   this.raycastReach())
                                 .setDefaultValue(5.2D)
                                 .setMin(0D)
                                 .setMax(64D)
                                 .setTooltip(Text.translatable("options.heywiki.raycast_reach.description"))
                                 .setSaveConsumer(newValue -> this.raycastReach = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .startBooleanToggle(Text.translatable("options.heywiki.raycast_allow_fluid.name"),
                                                     this.raycastAllowFluid())
                                 .setDefaultValue(false)
                                 .setTooltip(Text.translatable("options.heywiki.raycast_allow_fluid.description"))
                                 .setSaveConsumer(newValue -> this.raycastAllowFluid = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .startDropdownMenu(Text.translatable("options.heywiki.language.name"),
                                                    DropdownMenuBuilder.TopCellElementBuilder.of(this.language,
                                                                                                 HeyWikiConfig::normalizeLanguageName,
                                                                                                 HeyWikiConfig::languageDescription),
                                                    DropdownMenuBuilder.CellCreatorBuilder.of(
                                                            HeyWikiConfig::languageDescription))
                                 .setSelections(languages)
                                 .setDefaultValue("auto")
                                 .setSuggestionMode(false)
                                 .setTooltip(Text.translatable("options.heywiki.language.description"))
                                 .setSaveConsumer(newValue -> {
                                     if (!(newValue).equals(this.language()))
                                         requireReload.set(true);
                                     this.language = newValue;
                                 })
                                 .build());
        general.addEntry(entryBuilder
                                 .startDropdownMenu(Text.translatable("options.heywiki.zh_variant.name"),
                                                    DropdownMenuBuilder.TopCellElementBuilder.of(this.zhVariant,
                                                                                                 HeyWikiConfig::normalizeLanguageName,
                                                                                                 HeyWikiConfig::zhVariantDescription),
                                                    DropdownMenuBuilder.CellCreatorBuilder.of(
                                                            HeyWikiConfig::zhVariantDescription))
                                 .setDisplayRequirement(() -> {
                                     for (var wiki : MOD.familyManager().activeWikis().values()) {
                                         if (wiki.language().wikiLanguage().startsWith("zh")) {
                                             return true;
                                         }
                                     }
                                     return false;
                                 })
                                 .setSelections(List.of("auto", "zh", "zh-cn", "zh-tw", "zh-hk"))
                                 .setDefaultValue("auto")
                                 .setSuggestionMode(false)
                                 .setTooltip(Text.translatable("options.heywiki.zh_variant.description"))
                                 .setSaveConsumer(newValue -> this.zhVariant = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .fillKeybindingField(Text.translatable("key.heywiki.open"), HeyWikiClient.openWikiKey)
                                 .setTooltip(Text.translatable("options.heywiki.open_key.description"))
                                 .build());
        general.addEntry(entryBuilder
                                 .fillKeybindingField(Text.translatable("key.heywiki.open_search"),
                                                      HeyWikiClient.openWikiSearchKey)
                                 .setTooltip(Text.translatable("options.heywiki.open_key.description"))
                                 .build());

        builder.setSavingRunnable(() -> save(requireReload.get()));

        return builder.build();
    }

    private static String normalizeLanguageName(String name) {
        return name.split(":")[0];
    }

    private static Text languageDescription(String lang) {
        if (lang.equals("auto")) return Text.translatable("options.heywiki.language.auto");

        return Text.literal(lang + ": " + getLanguageName(lang));
    }

    private static Text zhVariantDescription(String lang) {
        return switch (lang) {
            case "auto" -> Text.translatable("options.heywiki.language.auto");
            case "zh" -> Text.literal("zh: 不转换");
            case "zh-cn" -> Text.literal("zh-cn: 大陆简体");
            case "zh-tw" -> Text.literal("zh-tw: 臺灣正體");
            case "zh-hk" -> Text.literal("zh-hk: 香港繁體");
            default -> throw new IllegalStateException("Unexpected value: " + lang);
        };
    }

    private static String getLanguageName(String lang) {
        Map<String, String> languageNames =
                Map.of(
                        "lzh", "文言"
                      );
        if (languageNames.containsKey(lang)) return languageNames.get(lang);

        var locale = Locale.of(lang);
        return locale.getDisplayLanguage(locale);
    }

    /**
     * Saves the configuration to a file.
     *
     * @param requireReload Whether the game should be reloaded after saving.
     */
    public void save(Boolean requireReload) {
        if (requireReload) MinecraftClient.getInstance().reloadResourcesConcurrently();
        Path configPath = getConfigFolder().resolve("heywiki.json");
        try {
            Files.createDirectories(configPath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
                JsonWriter jsonWriter = new JsonWriter(writer);
                jsonWriter.setIndent("  ");

                JsonOps.INSTANCE
                        .withEncoder(HeyWikiConfig.CODEC)
                        .apply(this)
                        .result()
                        .map(JsonElement::toString)
                        .ifPresent(s -> {
                            try {
                                writer.write(s);
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to write config file", e);
                            }
                        });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config file", e);
        }
    }

    /**
     * Loads the configuration from a file.
     *
     * @throws RuntimeException if the file cannot be read
     */
    public static HeyWikiConfig load() {
        Path configPath = getConfigFolder().resolve("heywiki.json");

        String json = "{}";
        if (Files.exists(configPath)) {
            try {
                json = Files.readString(configPath);
            } catch (IOException e) {
                LOGGER.error("Failed to read config file, resetting config", e);
            }
        }

        JsonElement jsonElement;
        try {
            jsonElement = JsonParser.parseString(json);
        } catch (Exception e) {
            LOGGER.error("Failed to parse config file, resetting config", e);
            jsonElement = JsonParser.parseString("{}");
        }

        return HeyWikiConfig.CODEC.decode(JsonOps.INSTANCE, jsonElement).result().orElseThrow()
                                  .getFirst();
    }
}
