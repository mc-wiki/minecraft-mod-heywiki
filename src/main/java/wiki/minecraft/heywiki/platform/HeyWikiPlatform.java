package wiki.minecraft.heywiki.platform;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface HeyWikiPlatform {
    static HeyWikiPlatform get() {
        return HeyWikiPlatformImplHolder.impl;
    }

    void registerAssetReloadListener(Identifier id, PreparableReloadListener listener);

    void registerKeyMapping(KeyMapping keyMapping);

    static LiteralArgumentBuilder<SharedSuggestionProvider> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    static <T> RequiredArgumentBuilder<SharedSuggestionProvider, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    void registerClientCommand(BiConsumer<CommandDispatcher<SharedSuggestionProvider>, CommandBuildContext> consumer);

    void onChatReceived(BiFunction<ChatType.Bound, Component, Component> function);

    void onItemTooltip(BiConsumer<ItemStack, List<Component>> consumer);

    void onClientTickPost(Consumer<Minecraft> consumer);

    Path getConfigFolder();
    
    boolean isModLoaded(String modId);
}