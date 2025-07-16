package wiki.minecraft.heywiki.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.Nullable;
import wiki.minecraft.heywiki.wiki.SearchProvider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static wiki.minecraft.heywiki.HeyWikiClient.id;

public class SuggestionEntryWidget extends ObjectSelectionList.Entry<SuggestionEntryWidget> {
    public final SearchProvider.Suggestion suggestion;
    protected final Minecraft client;
    protected final SuggestionEntryListWidget list;
    private long lastClickTime;

    public SuggestionEntryWidget(SearchProvider.Suggestion suggestion, SuggestionEntryListWidget list) {
        this.suggestion = suggestion;
        this.list = list;
        this.client = Minecraft.getInstance();
    }

    @Override
    public Component getNarration() {
        return Component.literal(this.suggestion.title());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int delta) {
        this.list.select(this);
        var now = System.currentTimeMillis();
        if (now - this.lastClickTime < 250) {
            this.list.parent.searchEntry(this);
            this.lastClickTime = 0;
        } else {
            this.lastClickTime = now;
        }

        return true;
    }

    @Override
    public void render(GuiGraphics GuiGraphics, int index, int y, int x, int rowWidth, int rowHeight, int mouseX,
                       int mouseY, boolean hovered, float delta) {
        int iconSize = 20;

        var icon = this.getIconTexture();
        if (icon != null) {
            GuiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.getIconTexture(),
                             x + 22, y, 0.0F, 0.0F,
                             iconSize, iconSize, iconSize, iconSize,
                             ARGB.white(1.0F));
        }

        MutableComponent name;
        if (suggestion.title().toLowerCase().contains(this.list.parent.getSearchTerm().toLowerCase())) {
            String title = suggestion.title();
            int termIndex = title.toLowerCase().indexOf(this.list.parent.getSearchTerm().toLowerCase());
            name = Component.literal(title.substring(0, termIndex))
                            .append(Component.literal(
                                                     title.substring(termIndex, termIndex + this.list.parent.getSearchTerm().length()))
                                             .setStyle(Style.EMPTY.withUnderlined(true)))
                            .append(Component.literal(
                                    title.substring(termIndex + this.list.parent.getSearchTerm().length())));
        } else {
            name = Component.literal(suggestion.title());
        }
        GuiGraphics.drawString(this.client.font, Language.getInstance().getVisualOrder(name),
                               x + 22 + iconSize + 3, y + 1,
                               0xFFFFFF);

        suggestion.redirectsTo().ifPresent(redirect -> {
            Component redirected = Component.literal(redirect);
            GuiGraphics.drawString(this.client.font, Language.getInstance().getVisualOrder(redirected),
                                   x + 22 + iconSize + 3, y + 1 + 10,
                                   0xAAAAAA);
        });
    }

    public @Nullable ResourceLocation getIconTexture() {
        return this.suggestion.imageUrl().map((imageUrl) -> {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            String hash = Hex.encodeHexString(md.digest(imageUrl.getBytes(StandardCharsets.UTF_8)));
            var identifier = id(hash);

            AbstractTexture texture = client.getTextureManager().getTexture(identifier);
            return texture instanceof DynamicTexture ? identifier : null;
        }).orElse(null);
    }
}
