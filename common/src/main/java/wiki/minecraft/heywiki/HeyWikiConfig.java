package wiki.minecraft.heywiki;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.EnumDropdownControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class HeyWikiConfig {
    public static ConfigClassHandler<HeyWikiConfig> HANDLER = ConfigClassHandler
            .createBuilder(HeyWikiConfig.class)
            .id(new Identifier("heywiki", "config"))
            .serializer(config -> GsonConfigSerializerBuilder
                    .create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("heywiki.json"))
                    .build())
            .build();

    @SerialEntry
    public boolean requiresConfirmation = true;
    @SerialEntry
    public String language = "auto";

    public static Screen createGui(Screen parent) {
        var instance = HeyWikiConfig.HANDLER.instance();
        var client = MinecraftClient.getInstance();
        return YetAnotherConfigLib
                .createBuilder()
                .title(Text.translatable("options.heywiki.title"))
                .category(ConfigCategory
                        .createBuilder()
                        .name(Text.translatable("options.heywiki.general"))
                        .option(Option.<Boolean>createBuilder()
                                      .name(Text.translatable("options.heywiki.requires_confirmation.name"))
                                      .description(OptionDescription.of(Text.translatable("options.heywiki.requires_confirmation.description")))
                                      .binding(true, () -> instance.requiresConfirmation, newVal -> instance.requiresConfirmation = newVal)
                                      .controller(TickBoxControllerBuilder::create)
                                      .build())
                        .option(Option.<Language>createBuilder()
                                      .name(Text.translatable("options.heywiki.language.name"))
                                      .description(OptionDescription.of(Text.translatable("options.heywiki.language.description")))
                                      .binding(Language.AUTO,
                                              () -> Language.fromName(instance.language),
                                              newVal -> instance.language = newVal.getName())
                                      .controller(opt -> EnumDropdownControllerBuilder.create(opt).formatValue(
                                              lang -> Text.translatable("options.heywiki.language." + lang.getName())
                                                                                                              ))
                                      .build())
                        .option(ButtonOption.createBuilder()
                                            .name(Text.translatable("options.heywiki.open_keybinds.name"))
                                            .text(Text.literal(""))
                                            .description(OptionDescription.of(Text.translatable("options.heywiki.open_keybinds.description")))
                                            .action((yaclScreen, option) -> client.setScreen(new KeybindsScreen(yaclScreen, client.options)))
                                            .build())
                        .build())
                .save(HeyWikiConfig.HANDLER::save)
                .build().generateScreen(parent);
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
