package wiki.minecraft.heywiki.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.HeyWikiConfig;

@Mod(HeyWikiClient.MOD_ID)
public class HeyWikiNeoForge {
    public HeyWikiNeoForge(IEventBus ignoredModEventBus) {
        HeyWikiClient.init();
        ModContainer container = ModList.get().getModContainerById(HeyWikiClient.MOD_ID).orElseThrow();
        container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> HeyWikiConfig.createGui(screen)));
    }
}
