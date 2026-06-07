package wiki.minecraft.heywiki.fabric;

import net.fabricmc.api.ClientModInitializer;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.fabric.platform.FabricHeyWikiPlatform;
import wiki.minecraft.heywiki.platform.HeyWikiPlatformImplHolder;

public class HeyWikiFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HeyWikiPlatformImplHolder.setImpl(new FabricHeyWikiPlatform());
        new HeyWikiClient();
    }
}
