package wiki.minecraft.heywiki;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import static dev.architectury.platform.Platform.getConfigFolder;

public class HeyWikiConfig {
    public static boolean requiresConfirmation = true;
    public static String language = Language.AUTO.getName();

    public static void load() {
        File configFile = getConfigFolder().resolve("heywiki.json").toFile();
        if (!configFile.exists()) {
            return;
        }
        try {
            JsonParser.parseReader(new Gson().newJsonReader(new FileReader(configFile))).getAsJsonObject().entrySet().forEach(entry -> {
                switch (entry.getKey()) {
                    case "requiresConfirmation":
                        requiresConfirmation = entry.getValue().getAsBoolean();
                        break;
                    case "language":
                        language = entry.getValue().getAsString();
                        break;
                }
            });
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void save() {
        File configFile = getConfigFolder().resolve("heywiki.json").toFile();
        if (!configFile.exists()) {
            return;
        }
        try {
            boolean createFile = configFile.createNewFile();
            if (!createFile) {
                throw new RuntimeException("Failed to create config file");
            }
            new Gson().newJsonWriter(new java.io.FileWriter(configFile)).beginObject()
                      .name("requiresConfirmation").value(requiresConfirmation)
                      .name("language").value(language)
                      .endObject().close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
                // FIXME: Change to a dropdown
                .startEnumSelector(Text.translatable("options.heywiki.language.name"), Language.class, Language.fromName(HeyWikiConfig.language))
                .setDefaultValue(Language.AUTO)
                .setEnumNameProvider(val -> Text.translatable("options.heywiki.language." + ((Language) val).getName()))
                .setTooltip(Text.translatable("options.heywiki.language.description"))
                .setSaveConsumer(newValue -> HeyWikiConfig.language = newValue.getName())
                .build());
        general.addEntry(entryBuilder
                .fillKeybindingField(Text.translatable("key.heywiki.open"), HeyWikiClient.openWikiKey)
                .setTooltip(Text.translatable("options.heywiki.language.description"))
                .build());

        builder.setSavingRunnable(HeyWikiConfig::save);

        return builder.build();
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
