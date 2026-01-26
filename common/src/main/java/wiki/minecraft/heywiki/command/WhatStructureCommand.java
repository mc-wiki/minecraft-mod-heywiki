package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import wiki.minecraft.heywiki.target.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;
import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_EXCEPTION;

public class WhatStructureCommand {
    public static final SimpleCommandExceptionType NO_INTEGRATED_SERVER = new SimpleCommandExceptionType(
            Component.translatable("commands.whatstructure.no_integrated_server"));
    public static final SimpleCommandExceptionType NO_STRUCTURE = new SimpleCommandExceptionType(
            Component.translatable("commands.whatstructure.no_structure"));
    private static final Minecraft CLIENT = Minecraft.getInstance();

    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<ClientCommandRegistrationEvent.ClientCommandSourceStack> register(
            CommandDispatcher<ClientCommandRegistrationEvent.ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(literal("whatstructure").executes(ctx -> {
            if (CLIENT.player == null || CLIENT.level == null) return -1;

            var block = CLIENT.player.getOnPos();

            if (!CLIENT.hasSingleplayerServer()) {
                throw NO_INTEGRATED_SERVER.create();
            }

            IntegratedServer server = CLIENT.getSingleplayerServer();
            UUID playerUuid = CLIENT.player.getUUID();

            var playerManager = Objects.requireNonNull(server).getPlayerList();
            var serverPlayer = playerManager.getPlayer(playerUuid);
            var serverWorld = Objects.requireNonNull(serverPlayer).level();
            var chunkPos = new ChunkPos(block);
            ChunkAccess chunk = serverWorld.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_REFERENCES, false);
            if (chunk == null) {
                return -1;
            }

            Map<Structure, LongSet> references = chunk.getAllReferences();
            for (Map.Entry<Structure, LongSet> entry : references.entrySet()) {
                Structure structure = entry.getKey();
                LongSet positions = entry.getValue();
                var startChunkPos = new ChunkPos(positions.toLongArray()[0]);
                ChunkAccess startChunk = serverWorld.getChunk(startChunkPos.x, startChunkPos.z,
                                                              ChunkStatus.STRUCTURE_STARTS,
                                                              false);
                assert startChunk != null;
                StructureStart structureStart = startChunk.getStartForStructure(structure);
                assert structureStart != null;
                BoundingBox boundingBox = structureStart.getBoundingBox();
                if (boundingBox.isInside(block)) {
                    var structureRegistryEntry = serverWorld.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                                                            .wrapAsHolder(structure);
                    var target = Target.of(structureRegistryEntry, "structure");
                    if (target == null) return -1;
                    var page = WikiPage.fromTarget(target);
                    if (page == null) {
                        throw NO_FAMILY_EXCEPTION.create();
                    }
                    page.openInBrowserCommand(null);
                    return Command.SINGLE_SUCCESS;
                }
            }
            throw NO_STRUCTURE.create();
        }));
    }
}
