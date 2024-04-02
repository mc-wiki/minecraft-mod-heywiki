package wiki.minecraft.heywiki.neoforge;

import net.neoforged.fml.common.Mod;
import wiki.minecraft.heywiki.HeyWikiClient;

@Mod(HeyWikiClient.MOD_ID)
public class HeyWikiNeoForge {
    public HeyWikiNeoForge() {
        HeyWikiClient.init();
    }
}
