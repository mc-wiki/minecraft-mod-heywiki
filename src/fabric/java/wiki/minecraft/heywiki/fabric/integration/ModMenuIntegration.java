package wiki.minecraft.heywiki.fabric.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import wiki.minecraft.heywiki.HeyWikiClient;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (parent) -> HeyWikiClient.getInstance().config().createGui(parent);
    }
}
