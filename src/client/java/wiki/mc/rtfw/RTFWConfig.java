package wiki.mc.rtfw;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
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

public class RTFWConfig {
    public static ConfigClassHandler<RTFWConfig> HANDLER = ConfigClassHandler.createBuilder(RTFWConfig.class)
            .id(new Identifier("rtfw", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("rtfw.json"))
                    .build())
            .build();

    @SerialEntry
    public boolean requiresConfirmation = true;

    private static final String[] LANGUAGES = {
            "auto",
            "de",
            "en",
            "es",
            "fr",
            "ja",
            "ko",
            "lzh",
            "pt",
            "ru",
            "th",
            "uk",
            "zh"
    };
    @SerialEntry
    public String language = "auto";

    public static Screen createGui(Screen parent) {
        var instance = RTFWConfig.HANDLER.instance();
        var client = MinecraftClient.getInstance();
        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("options.rtfw.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("options.rtfw.general"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("options.rtfw.requires_confirmation.name"))
                                .description(OptionDescription.of(Text.translatable("options.rtfw.requires_confirmation.description")))
                                .binding(true, () -> instance.requiresConfirmation, newVal -> instance.requiresConfirmation = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("options.rtfw.language.name"))
                                .description(OptionDescription.of(Text.translatable("options.rtfw.language.description")))
                                .binding("auto",
                                        () -> instance.language,
                                        newVal -> instance.language = newVal)
                                .controller(opt -> DropdownStringControllerBuilder.create(opt)
                                        .values(LANGUAGES)
                                )
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("options.rtfw.open_keybinds.name"))
                                .text(Text.literal(""))
                                .description(OptionDescription.of(Text.translatable("options.rtfw.open_keybinds.description")))
                                .action((yaclScreen, option) -> client.setScreen(new KeybindsScreen(yaclScreen, client.options)))
                                .build())
                        .build())
                .save(RTFWConfig.HANDLER::save)
                .build().generateScreen(parent);
    }
}
