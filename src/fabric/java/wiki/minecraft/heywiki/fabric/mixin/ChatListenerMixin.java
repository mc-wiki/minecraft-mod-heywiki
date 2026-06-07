/*
 * This file is part of architectury.
 * Copyright (C) 2020, 2021, 2022 architectury
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package wiki.minecraft.heywiki.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wiki.minecraft.heywiki.fabric.platform.FabricHeyWikiPlatform;

import java.util.Objects;

@Mixin(ChatListener.class)
public class ChatListenerMixin {
    @Unique
    ChatType.Bound boundChatType;
    @Unique
    private ThreadLocal<Component> cancelNextChat = new ThreadLocal<>();

    @Inject(method = "handlePlayerChatMessage",
            at = @At(value = "INVOKE", target = "Ljava/time/Instant;now()Ljava/time/Instant;"))
    private void handlePlayerChatMessage(PlayerChatMessage playerChatMessage, GameProfile gameProfile,
                                         ChatType.Bound bound, CallbackInfo ci) {
        this.boundChatType = bound;
    }

    @ModifyVariable(method = "handlePlayerChatMessage", at = @At(value = "INVOKE",
                                                                 target = "Lnet/minecraft/network/chat/PlayerChatMessage;signature()Lnet/minecraft/network/chat/MessageSignature;"))
    private Component modifyMessage(Component value) {
        cancelNextChat.remove();

        return FabricHeyWikiPlatform.chatReceivedHandler.apply(boundChatType, value);
    }

    @Inject(method = "handlePlayerChatMessage", at = @At(value = "INVOKE",
                                                         target = "Lnet/minecraft/client/multiplayer/chat/ChatListener;handleMessage(Lnet/minecraft/network/chat/MessageSignature;Ljava/util/function/BooleanSupplier;)V"),
            cancellable = true)
    private void handleChatPre(PlayerChatMessage playerChatMessage, GameProfile gameProfile, ChatType.Bound bound,
                               CallbackInfo ci,
                               @Local(name = "decoratedMessage") Component component) {
        if (Objects.equals(cancelNextChat.get(), component)) {
            ci.cancel();
        }

        cancelNextChat.remove();
    }
}