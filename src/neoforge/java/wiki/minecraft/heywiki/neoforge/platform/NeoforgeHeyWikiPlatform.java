package wiki.minecraft.heywiki.neoforge.platform;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import wiki.minecraft.heywiki.platform.HeyWikiPlatform;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public record NeoforgeHeyWikiPlatform(IEventBus modEventBus) implements HeyWikiPlatform {
    @Override
    public void registerAssetReloadListener(Identifier id, PreparableReloadListener listener) {
        modEventBus.<AddClientReloadListenersEvent>addListener(e -> {
            e.addListener(id, listener);
        });
    }

    @Override public void registerKeyMapping(KeyMapping keyMapping) {
        modEventBus.<RegisterKeyMappingsEvent>addListener(e -> {
            e.register(keyMapping);
        });
    }

    @Override
    public void registerClientCommand(
            BiConsumer<CommandDispatcher<SharedSuggestionProvider>, CommandBuildContext> consumer) {
        NeoForge.EVENT_BUS.<RegisterClientCommandsEvent>addListener(e -> {
            consumer.accept((CommandDispatcher) e.getDispatcher(), e.getBuildContext());
        });
    }

    @Override public void onChatReceived(BiFunction<ChatType.Bound, Component, Component> function) {
        NeoForge.EVENT_BUS.<ClientChatReceivedEvent>addListener(e -> {
            e.setMessage(function.apply(e.getBoundChatType(), e.getMessage()));
        });
    }

    @Override public void onItemTooltip(BiConsumer<ItemStack, List<Component>> consumer) {
        NeoForge.EVENT_BUS.<ItemTooltipEvent>addListener(e -> {
            consumer.accept(e.getItemStack(), e.getToolTip());
        });
    }

    @Override public void onClientTickPost(Consumer<Minecraft> consumer) {
        NeoForge.EVENT_BUS.<ClientTickEvent.Post>addListener(e -> {
            consumer.accept(Minecraft.getInstance());
        });
    }

    @Override public Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}