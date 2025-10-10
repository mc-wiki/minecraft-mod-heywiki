package wiki.minecraft.heywiki.entrypoint;

import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import wiki.minecraft.heywiki.wiki.WikiPage;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class HeyWikiDebugEntry implements DebugScreenEntry {

    @Override
    public void display(DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk,
                        @Nullable LevelChunk serverChunk) {
        var target = Raycast.raycast();
        if (target == null) {
            displayer.addLine("heywiki: null");
            return;
        }
        var page = WikiPage.fromTarget(target);
        if (page == null) {
            displayer.addLine("heywiki: null");
            return;
        }
        displayer.addLine("heywiki: " + URLDecoder.decode(page.getUri().toString(), StandardCharsets.UTF_8));
    }

}
