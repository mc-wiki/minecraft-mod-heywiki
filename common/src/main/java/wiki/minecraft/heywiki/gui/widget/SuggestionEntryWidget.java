package wiki.minecraft.heywiki.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget.Entry;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.Nullable;
import wiki.minecraft.heywiki.wiki.SearchProvider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static wiki.minecraft.heywiki.HeyWikiClient.id;

public class SuggestionEntryWidget extends Entry<SuggestionEntryWidget> {
    public final SearchProvider.Suggestion suggestion;
    protected final MinecraftClient client;
    protected final SuggestionEntryListWidget list;
    private long lastClickTime;

    public SuggestionEntryWidget(SearchProvider.Suggestion suggestion, SuggestionEntryListWidget list) {
        this.suggestion = suggestion;
        this.list = list;
        this.client = MinecraftClient.getInstance();
    }

    @Override
    public Text getNarration() {
        return Text.literal(this.suggestion.title());
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
    public void render(DrawContext DrawContext, int index, int y, int x, int rowWidth, int rowHeight, int mouseX,
                       int mouseY, boolean hovered, float delta) {
        int iconSize = 20;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        var icon = this.getIconTexture();
        if (icon != null) {
            RenderSystem.enableBlend();
            DrawContext.drawTexture(this.getIconTexture(), x + 22, y, 0.0F, 0.0F, iconSize, iconSize, iconSize,
                                    iconSize);
            RenderSystem.disableBlend();
        }

        MutableText name;
        if (suggestion.title().toLowerCase().contains(this.list.parent.getSearchTerm().toLowerCase())) {
            String title = suggestion.title();
            int termIndex = title.toLowerCase().indexOf(this.list.parent.getSearchTerm().toLowerCase());
            name = Text.literal(title.substring(0, termIndex))
                       .append(Text.literal(
                                           title.substring(termIndex, termIndex + this.list.parent.getSearchTerm().length()))
                                   .setStyle(Style.EMPTY.withUnderline(true)))
                       .append(Text.of(title.substring(termIndex + this.list.parent.getSearchTerm().length())));
        } else {
            name = Text.literal(suggestion.title());
        }
        DrawContext.drawTextWithShadow(this.client.textRenderer, Language.getInstance().reorder(name),
                                       x + 22 + iconSize + 3, y + 1,
                                       0xFFFFFF);

        suggestion.redirectsTo().ifPresent(redirect -> {
            Text redirected = Text.literal(redirect);
            DrawContext.drawTextWithShadow(this.client.textRenderer, Language.getInstance().reorder(redirected),
                                           x + 22 + iconSize + 3, y + 1 + 10,
                                           0xAAAAAA);
        });
    }

    public @Nullable Identifier getIconTexture() {
        return this.suggestion.imageUrl().map((imageUrl) -> {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            String hash = Hex.encodeHexString(md.digest(imageUrl.getBytes(StandardCharsets.UTF_8)));
            var identifier = id(hash);

            AbstractTexture texture = client.getTextureManager().getOrDefault(identifier, null);
            return texture != null ? identifier : null;
        }).orElse(null);
    }
}
