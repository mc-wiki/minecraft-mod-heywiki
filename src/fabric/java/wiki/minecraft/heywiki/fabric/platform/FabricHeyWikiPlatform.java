package wiki.minecraft.heywiki.fabric.platform;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.ItemStack;
import wiki.minecraft.heywiki.platform.HeyWikiPlatform;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class FabricHeyWikiPlatform implements HeyWikiPlatform {
    public static BiFunction<ChatType.Bound, Component, Component> chatReceivedHandler;

    @Override public void registerAssetReloadListener(Identifier id, PreparableReloadListener listener) {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(id, listener);
    }

    @Override public void registerKeyMapping(KeyMapping keyMapping) {
        KeyMappingHelper.registerKeyMapping(keyMapping);
    }

    @Override
    public void registerClientCommand(
            BiConsumer<CommandDispatcher<SharedSuggestionProvider>, CommandBuildContext> consumer) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            consumer.accept((CommandDispatcher) dispatcher, registryAccess);
        });
    }

    @Override public void onChatReceived(BiFunction<ChatType.Bound, Component, Component> function) {
        chatReceivedHandler = function;
    }

    @Override public void onItemTooltip(BiConsumer<ItemStack, List<Component>> consumer) {
        ItemTooltipCallback.EVENT.register((stack, context, tooltip, lines) -> {
            consumer.accept(stack, lines);
        });
    }

    @Override public void onClientTickPost(Consumer<Minecraft> consumer) {
        ClientTickEvents.END_CLIENT_TICK.register(consumer::accept);
    }

    @Override public Path getConfigFolder() {
        return FabricLoader.getInstance()
                           .getConfigDir()
                           .toAbsolutePath()
                           .normalize();
    }

    @Override public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}