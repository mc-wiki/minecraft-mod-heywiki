package wiki.minecraft.heywiki.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import wiki.minecraft.heywiki.HeyWikiClient;

@Mod(HeyWikiClient.MOD_ID)
public class HeyWikiNeoForge {
    public HeyWikiNeoForge(IEventBus ignoredModEventBus) {
        var mod = new HeyWikiClient();
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () ->
                (minecraft, screen) -> mod.config().createGui(screen));
    }
}
