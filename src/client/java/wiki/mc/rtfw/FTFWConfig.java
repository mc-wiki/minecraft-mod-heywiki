package wiki.mc.rtfw;

import dev.isxander.yacl3.api.*;
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

public class FTFWConfig {
    public static ConfigClassHandler<FTFWConfig> HANDLER = ConfigClassHandler.createBuilder(FTFWConfig.class)
            .id(new Identifier("ftfw", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("ftfw.json"))
                    .build())
            .build();

    @SerialEntry
    public boolean requiresConfirmation = true;

    public static Screen createGui(Screen parent) {
        var instance = FTFWConfig.HANDLER.instance();
        var client = MinecraftClient.getInstance();
        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("options.ftfw.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("options.ftfw.general"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("options.ftfw.requires_confirmation.name"))
                                .description(OptionDescription.of(Text.translatable("options.ftfw.requires_confirmation.description")))
                                .binding(true, () -> instance.requiresConfirmation, newVal -> instance.requiresConfirmation = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("options.ftfw.open_keybinds.name"))
                                .text(Text.literal(""))
                                .description(OptionDescription.of(Text.translatable("options.ftfw.open_keybinds.description")))
                                .action((yaclScreen, option) -> client.setScreen(new KeybindsScreen(yaclScreen, client.options)))
                                .build())
                        .build())
                .save(FTFWConfig.HANDLER::save)
                .build().generateScreen(parent);
    }
}
