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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.target.Target;

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
import static wiki.minecraft.heywiki.HeyWikiClient.id;

/**
 * The configuration for the Hey Wiki mod.
 */
public class HeyWikiConfig {
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
                                                .forGetter(HeyWikiConfig::zhVariant),
                                    Identifier.CODEC.fieldOf("searchDefaultWikiFamily")
                                                          .orElse(id("minecraft"))
                                                          .forGetter(HeyWikiConfig::searchDefaultWikiFamily),
                                    Codec.BOOL.fieldOf("prefixSearch")
                                              .orElse(true)
                                              .forGetter(HeyWikiConfig::prefixSearch),
                                    Codec.BOOL.fieldOf("itemTooltip")
                                              .orElse(true)
                                              .forGetter(HeyWikiConfig::itemTooltip)
                                  )
                            .apply(instance, HeyWikiConfig::new));
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean requiresConfirmation;
    private boolean requiresConfirmationCommand;
    private double raycastReach;
    private boolean raycastAllowFluid;
    private String language;
    private String zhVariant;
    private Identifier searchDefaultWikiFamily;
    private boolean prefixSearch;
    private boolean itemTooltip;

    private HeyWikiConfig(boolean requiresConfirmation, boolean requiresConfirmationCommand, double raycastReach,
                          boolean raycastAllowFluid, String language, String zhVariant,
                          Identifier searchDefaultWikiFamily, boolean prefixSearch, boolean itemTooltip) {
        this.requiresConfirmation = requiresConfirmation;
        this.requiresConfirmationCommand = requiresConfirmationCommand;
        this.raycastReach = raycastReach;
        this.raycastAllowFluid = raycastAllowFluid;
        this.language = language;
        this.zhVariant = zhVariant;
        this.searchDefaultWikiFamily = searchDefaultWikiFamily;
        this.prefixSearch = prefixSearch;
        this.itemTooltip = itemTooltip;
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

    /**
     * The variant of Chinese to use for wiki pages.
     */
    public String zhVariant() {
        return zhVariant;
    }

    /**
     * The default wiki family to use for the search wiki screen.
     */
    public Identifier searchDefaultWikiFamily() {
        return searchDefaultWikiFamily;
    }

    public void setSearchDefaultWikiFamily(Identifier searchDefaultWikiFamily) {
        this.searchDefaultWikiFamily = searchDefaultWikiFamily;
    }

    public boolean prefixSearch() {
        return prefixSearch;
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
                                             .setTitle(Component.translatable("options.heywiki.title"));

        ConfigCategory general = builder.getOrCreateCategory(
                Component.translatable("options.heywiki.category.general"));
        ConfigCategory search = builder.getOrCreateCategory(Component.translatable("options.heywiki.category.search"));
        ConfigCategory language = builder.getOrCreateCategory(
                Component.translatable("options.heywiki.category.language"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        general.addEntry(entryBuilder
                                 .startBooleanToggle(
                                         Component.translatable("options.heywiki.requires_confirmation.name"),
                                         this.requiresConfirmation())
                                 .setDefaultValue(true)
                                 .setTooltip(
                                         Component.translatable("options.heywiki.requires_confirmation.description"))
                                 .setSaveConsumer(newValue -> this.requiresConfirmation = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .startBooleanToggle(
                                         Component.translatable("options.heywiki.requires_confirmation_command.name"),
                                         this.requiresConfirmationCommand())
                                 .setDefaultValue(false)
                                 .setTooltip(
                                         Component.translatable(
                                                 "options.heywiki.requires_confirmation_command.description"))
                                 .setSaveConsumer(newValue -> this.requiresConfirmationCommand = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .startDoubleField(Component.translatable("options.heywiki.raycast_reach.name"),
                                                   this.raycastReach())
                                 .setDefaultValue(5.2D)
                                 .setMin(0D)
                                 .setMax(64D)
                                 .setTooltip(Component.translatable("options.heywiki.raycast_reach.description"))
                                 .setSaveConsumer(newValue -> this.raycastReach = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .startBooleanToggle(Component.translatable("options.heywiki.raycast_allow_fluid.name"),
                                                     this.raycastAllowFluid())
                                 .setDefaultValue(false)
                                 .setTooltip(Component.translatable("options.heywiki.raycast_allow_fluid.description"))
                                 .setSaveConsumer(newValue -> this.raycastAllowFluid = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .startBooleanToggle(Component.translatable("options.heywiki.item_tooltip.name"),
                                                     this.itemTooltip())
                                 .setDefaultValue(true)
                                 .setTooltip(Component.translatable("options.heywiki.item_tooltip.description"))
                                 .setSaveConsumer(newValue -> this.itemTooltip = newValue)
                                 .build());
        general.addEntry(entryBuilder
                                 .fillKeybindingField(Component.translatable("key.heywiki.open"),
                                                      HeyWikiClient.openWikiKey)
                                 .setTooltip(Component.translatable("options.heywiki.open_key.description"))
                                 .build());

        language.addEntry(entryBuilder
                                  .startDropdownMenu(Component.translatable("options.heywiki.language.name"),
                                                     DropdownMenuBuilder.TopCellElementBuilder.of(this.language,
                                                                                                  HeyWikiConfig::normalizeLanguageName,
                                                                                                  HeyWikiConfig::languageDescription),
                                                     DropdownMenuBuilder.CellCreatorBuilder.of(
                                                             HeyWikiConfig::languageDescription))
                                  .setSelections(languages)
                                  .setDefaultValue("auto")
                                  .setSuggestionMode(false)
                                  .setTooltip(Component.translatable("options.heywiki.language.description"))
                                  .setSaveConsumer(newValue -> {
                                      if (!(newValue).equals(this.language()))
                                          requireReload.set(true);
                                      this.language = newValue;
                                  })
                                  .build());
        language.addEntry(entryBuilder
                                  .startDropdownMenu(Component.translatable("options.heywiki.zh_variant.name"),
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
                                  .setTooltip(Component.translatable("options.heywiki.zh_variant.description"))
                                  .setSaveConsumer(newValue -> this.zhVariant = newValue)
                                  .build());

        search.addEntry(entryBuilder
                                .startBooleanToggle(Component.translatable("options.heywiki.prefix_search.name"),
                                                    this.raycastAllowFluid())
                                .setDefaultValue(true)
                                .setTooltip(Component.translatable("options.heywiki.prefix_search.description"))
                                .setSaveConsumer(newValue -> this.prefixSearch = newValue)
                                .build());

        search.addEntry(entryBuilder
                                .fillKeybindingField(Component.translatable("key.heywiki.open_search"),
                                                     HeyWikiClient.openWikiSearchKey)
                                .setTooltip(Component.translatable("options.heywiki.open_search_key.description"))
                                .build());

        builder.setSavingRunnable(() -> save(requireReload.get()));

        return builder.build();
    }

    /**
     * Whether the user should be prompted for confirmation before opening a wiki page.
     */
    public boolean requiresConfirmation() {
        return requiresConfirmation;
    }

    /**
     * Whether the user should be prompted for confirmation before opening a wiki page using a command.
     */
    public boolean requiresConfirmationCommand() {
        return requiresConfirmationCommand;
    }

    /**
     * The distance at which the player can raycast to find a {@link Target Target}.
     */
    public double raycastReach() {
        return raycastReach;
    }

    /**
     * When true, the raycast will hit fluid blocks.
     */
    public boolean raycastAllowFluid() {
        return raycastAllowFluid;
    }

    public boolean itemTooltip() {
        return itemTooltip;
    }

    private static String normalizeLanguageName(String name) {
        if (name.equals(I18n.get("options.heywiki.language.auto"))) return "auto";

        return name.split(":")[0];
    }

    private static Component languageDescription(String lang) {
        if (lang.equals("auto")) return Component.translatable("options.heywiki.language.auto");

        return Component.literal(lang + ": " + getLanguageName(lang));
    }

    /**
     * The language to use for wiki pages.
     */
    public String language() {
        return language;
    }

    private static Component zhVariantDescription(String lang) {
        return switch (normalizeLanguageName(lang)) {
            case "auto" -> Component.translatable("options.heywiki.language.auto");
            case "zh" -> Component.literal("zh: 不转换");
            case "zh-cn" -> Component.literal("zh-cn: 大陆简体");
            case "zh-tw" -> Component.literal("zh-tw: 臺灣正體");
            case "zh-hk" -> Component.literal("zh-hk: 香港繁體");
            default -> throw new IllegalStateException("Unexpected value: " + lang);
        };
    }

    /**
     * Saves the configuration to a file.
     *
     * @param requireReload Whether the game should be reloaded after saving.
     */
    public void save(Boolean requireReload) {
        if (requireReload) Minecraft.getInstance().delayTextureReload();
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

    private static String getLanguageName(String lang) {
        Map<String, String> languageNames =
                Map.of(
                        "lzh", "文言"
                      );
        if (languageNames.containsKey(lang)) return languageNames.get(lang);

        var locale = Locale.of(lang);
        return locale.getDisplayLanguage(locale);
    }
}
