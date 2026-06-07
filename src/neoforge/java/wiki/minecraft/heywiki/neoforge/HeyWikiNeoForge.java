package wiki.minecraft.heywiki.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.neoforge.platform.NeoforgeHeyWikiPlatform;
import wiki.minecraft.heywiki.platform.HeyWikiPlatformImplHolder;

@Mod(value = HeyWikiClient.MOD_ID, dist = Dist.CLIENT)
public class HeyWikiNeoForge {
    public HeyWikiNeoForge(IEventBus modEventBus) {
        HeyWikiPlatformImplHolder.setImpl(new NeoforgeHeyWikiPlatform(modEventBus));
        var mod = new HeyWikiClient();
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () ->
                (minecraft, screen) -> mod.config().createGui(screen));
    }
}
