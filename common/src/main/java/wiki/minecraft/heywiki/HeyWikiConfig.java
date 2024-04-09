package wiki.minecraft.heywiki;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import static dev.architectury.platform.Platform.getConfigFolder;

public class HeyWikiConfig {
    public static boolean requiresConfirmation = true;
    public static double raycastMaxReach = 5.2D; // Use creative mode reach distance
    public static boolean raycastAllowFluid = false;
    public static String language = Language.AUTO.getName();

    public static Screen createGui(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                                             .setParentScreen(parent)
                                             .setTitle(Text.translatable("options.heywiki.title"));

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("options.heywiki.general"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        general.addEntry(entryBuilder
                .startBooleanToggle(Text.translatable("options.heywiki.requires_confirmation.name"), HeyWikiConfig.requiresConfirmation)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("options.heywiki.requires_confirmation.description"))
                .setSaveConsumer(newValue -> HeyWikiConfig.requiresConfirmation = newValue)
                .build());
        general.addEntry(entryBuilder
                .startDoubleField(Text.translatable("options.heywiki.raycast_max_reach.name"), HeyWikiConfig.raycastMaxReach)
                .setDefaultValue(5.2D)
                .setTooltip(Text.translatable("options.heywiki.raycast_max_reach.description"))
                .setSaveConsumer(newValue -> HeyWikiConfig.raycastMaxReach = newValue)
                .build());
        general.addEntry(entryBuilder
                .startBooleanToggle(Text.translatable("options.heywiki.raycast_allow_fluid.name"), HeyWikiConfig.raycastAllowFluid)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("options.heywiki.raycast_allow_fluid.description"))
                .setSaveConsumer(newValue -> HeyWikiConfig.raycastAllowFluid = newValue)
                .build());
        general.addEntry(entryBuilder
                .startDropdownMenu(Text.translatable("options.heywiki.language.name"),
                        DropdownMenuBuilder.TopCellElementBuilder.of(language, Language::fromNameOrTranslated, lang -> Text.translatable("options.heywiki.language." + lang.toString().toLowerCase())),
                        DropdownMenuBuilder.CellCreatorBuilder.of(lang -> Text.translatable("options.heywiki.language." + ((Language) lang).getName())))
                .setSelections(Arrays.stream(Language.values()).collect(Collectors.toList()))
                .setDefaultValue(Language.AUTO)
                .setSuggestionMode(false)
                .setTooltip(Text.translatable("options.heywiki.language.description"))
                .setSaveConsumer(newValue -> HeyWikiConfig.language = ((Language) newValue).getName())
                .build());
        general.addEntry(entryBuilder
                .fillKeybindingField(Text.translatable("key.heywiki.open"), HeyWikiClient.openWikiKey)
                .setTooltip(Text.translatable("options.heywiki.language.description"))
                .build());

        builder.setSavingRunnable(HeyWikiConfig::save);

        return builder.build();
    }

    public static void save() {
        MinecraftClient.getInstance().reloadResourcesConcurrently();
        Path configPath = getConfigFolder().resolve("heywiki.json");
        try {
            Files.createDirectories(configPath.getParent());
            BufferedWriter writer = Files.newBufferedWriter(configPath);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonWriter jsonWriter = gson.newJsonWriter(writer);
            try {
                jsonWriter.beginObject()
                          .name("requiresConfirmation").value(requiresConfirmation)
                          .name("language").value(language)
                          .endObject().close();
            } catch (IOException e) {
                jsonWriter.close();
                throw new RuntimeException("Failed to write config file", e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config file", e);
        }
    }

    public static void load() {
        Path configPath = getConfigFolder().resolve("heywiki.json");
        if (!configPath.toFile().exists()) {
            return;
        }
        try {
            JsonParser.parseReader(new Gson().newJsonReader(Files.newBufferedReader(configPath))).getAsJsonObject().entrySet().forEach(entry -> {
                switch (entry.getKey()) {
                    case "requiresConfirmation":
                        requiresConfirmation = entry.getValue().getAsBoolean();
                        break;
                    case "language":
                        language = entry.getValue().getAsString();
                        break;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file", e);
        }
    }

    public enum Language {
        AUTO("auto"),
        DE("de"),
        EN("en"),
        ES("es"),
        FR("fr"),
        JA("ja"),
        KO("ko"),
        LZH("lzh"),
        PT("pt"),
        RU("ru"),
        TH("th"),
        UK("uk"),
        ZH("zh");

        private final String name;

        Language(String name) {
            this.name = name;
        }

        public static Language fromNameOrTranslated(String string) {
            String name = string.toLowerCase().split(":")[0];
            for (Language lang : values()) {
                if (lang.getName().equals(name)) {
                    return lang;
                }
            }
            return AUTO;
        }

        @SuppressWarnings("unused")
        public static Language fromName(String name) {
            for (Language lang : values()) {
                if (lang.getName().equals(name)) {
                    return lang;
                }
            }
            return AUTO;
        }

        public String getName() {
            return name;
        }
    }
}
