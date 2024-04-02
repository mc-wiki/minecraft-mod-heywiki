package wiki.minecraft.heywiki.fabric;

import net.fabricmc.api.ClientModInitializer;
import wiki.minecraft.heywiki.HeyWikiClient;

public class HeyWikiFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HeyWikiClient.init();
    }
}
