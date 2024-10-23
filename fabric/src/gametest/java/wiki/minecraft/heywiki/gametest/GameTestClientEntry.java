package wiki.minecraft.heywiki.gametest;

import com.mojang.serialization.Lifecycle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Util;
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GameTestClientEntry implements ClientModInitializer {
    @Override public void onInitializeClient() {
    }

    public static void onClientStarted(MinecraftClient client) {
        ResourcePackManager dataPackManager = new ResourcePackManager(
                new VanillaDataPackProvider(client.getSymlinkFinder()));
        SaveLoading.DataPacks dataPacks = new SaveLoading.DataPacks(dataPackManager, DataConfiguration.SAFE_MODE, false,
                                                                    true);
        SaveLoading.ServerConfig serverConfig = new SaveLoading.ServerConfig(dataPacks,
                                                                             CommandManager.RegistrationEnvironment.INTEGRATED,
                                                                             2);
        GameRules gameRules = Util.make(new GameRules(FeatureSet.empty()), gr -> {
            gr.get(GameRules.DO_MOB_SPAWNING).set(false, null);
            gr.get(GameRules.DO_WEATHER_CYCLE).set(false, null);
            gr.get(GameRules.RANDOM_TICK_SPEED).set(0, null);
            gr.get(GameRules.DO_FIRE_TICK).set(false, null);
        });
        dataPackManager.scanPacks();
        DataConfiguration dataConfiguration = new DataConfiguration(
                ModResourcePackUtil.createTestServerSettings(new ArrayList<>(dataPackManager.getIds()), List.of()),
                FeatureFlags.FEATURE_MANAGER.getFeatureSet());
        LevelInfo levelInfo = new LevelInfo("Test Level", GameMode.CREATIVE, false, Difficulty.NORMAL, true, gameRules,
                                            dataConfiguration);
        GeneratorOptions generatorOptions = new GeneratorOptions(0L, false, false);

        SaveLoader saveLoader;
        try {
            saveLoader = Util.waitAndApply(executor -> SaveLoading.load(serverConfig, context -> {
                Registry<DimensionOptions> registry = new SimpleRegistry<>(RegistryKeys.DIMENSION,
                                                                           Lifecycle.stable()).freeze();
                DimensionOptionsRegistryHolder.DimensionsConfig dimensionsConfig = context.worldGenRegistryManager()
                                                                                          .getOrThrow(
                                                                                                  RegistryKeys.WORLD_PRESET)
                                                                                          .getOrThrow(WorldPresets.FLAT)
                                                                                          .value()
                                                                                          .createDimensionsRegistryHolder()
                                                                                          .toConfig(registry);
                return new SaveLoading.LoadContext<>(
                        new LevelProperties(levelInfo, generatorOptions, dimensionsConfig.specialWorldProperty(),
                                            dimensionsConfig.getLifecycle()),
                        dimensionsConfig.toDynamicRegistryManager());
            }, SaveLoader::new, Util.getMainWorkerExecutor(), executor)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


        try (var session = client.getLevelStorage().createSession("Test Level")) {
            client.execute(() -> client.createIntegratedServerLoader()
                                       .startNewWorld(session, saveLoader.dataPackContents(),
                                                      saveLoader.combinedDynamicRegistries(),
                                                      saveLoader.saveProperties()));
        } catch (IOException | SymlinkValidationException e) {
            throw new RuntimeException(e);
        }
    }
}
