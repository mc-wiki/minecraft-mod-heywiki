package wiki.minecraft.heywiki.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.structure.Structure;
import wiki.minecraft.heywiki.wiki.Target;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static dev.architectury.event.events.client.ClientCommandRegistrationEvent.literal;
import static wiki.minecraft.heywiki.wiki.WikiPage.NO_FAMILY_EXCEPTION;

public class WhatStructureCommand {
    public static final SimpleCommandExceptionType NO_INTEGRATED_SERVER = new SimpleCommandExceptionType(
            Text.translatable("command.whatstructure.no_integrated_server"));
    public static final SimpleCommandExceptionType NO_STRUCTURE = new SimpleCommandExceptionType(
            Text.translatable("command.whatstructure.no_structure"));
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    @SuppressWarnings("UnusedReturnValue")
    public static LiteralCommandNode<ClientCommandRegistrationEvent.ClientCommandSourceStack> register(
            CommandDispatcher<ClientCommandRegistrationEvent.ClientCommandSourceStack> dispatcher) {
        return dispatcher.register(
                literal("whatstructure")
                        .executes(ctx -> {
                            if (CLIENT.player == null || CLIENT.world == null) return 1;

                            var block = CLIENT.player.getBlockPos();

                            if (!CLIENT.isIntegratedServerRunning()) {
                                throw NO_INTEGRATED_SERVER.create();
                            }

                            IntegratedServer server = CLIENT.getServer();
                            UUID playerUuid = CLIENT.player.getUuid();

                            var playerManager = Objects.requireNonNull(server).getPlayerManager();
                            var serverPlayer = playerManager.getPlayer(playerUuid);
                            var serverWorld = Objects.requireNonNull(serverPlayer).getServerWorld();
                            var chunkPos = new ChunkPos(block);
                            Chunk chunk = serverWorld.getChunk(chunkPos.x, chunkPos.z,
                                                               ChunkStatus.STRUCTURE_REFERENCES,
                                                               false);
                            if (chunk == null) {
                                return 1;
                            }

                            Map<Structure, LongSet> references = chunk.getStructureReferences();
                            for (Map.Entry<Structure, LongSet> entry : references.entrySet()) {
                                Structure structure = entry.getKey();
                                LongSet positions = entry.getValue();
                                var startChunkPos = new ChunkPos(positions.toLongArray()[0]);
                                Chunk startChunk = serverWorld.getChunk(startChunkPos.x, startChunkPos.z,
                                                                   ChunkStatus.STRUCTURE_STARTS,
                                                                   false);
                                assert startChunk != null;
                                StructureStart structureStart = startChunk.getStructureStart(structure);
                                assert structureStart != null;
                                BlockBox boundingBox = structureStart.getBoundingBox();
                                if (boundingBox.contains(block)) {
                                    var target = Target.of(structure);
                                    if (target == null) return 1;
                                    var page = WikiPage.fromTarget(target);
                                    if (page == null) {
                                        throw NO_FAMILY_EXCEPTION.create();
                                    }
                                    page.openInBrowser(true);
                                    return 0;
                                }
                            }
                            throw NO_STRUCTURE.create();
                        }));
    }
}
