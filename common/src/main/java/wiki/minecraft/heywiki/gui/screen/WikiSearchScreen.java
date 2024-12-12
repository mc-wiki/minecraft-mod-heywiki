package wiki.minecraft.heywiki.gui.screen;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.codec.binary.Hex;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.gui.widget.SuggestionEntryListWidget;
import wiki.minecraft.heywiki.gui.widget.SuggestionEntryWidget;
import wiki.minecraft.heywiki.util.CachedDebouncer;
import wiki.minecraft.heywiki.util.HttpUtil;
import wiki.minecraft.heywiki.wiki.SearchProvider;
import wiki.minecraft.heywiki.wiki.WikiFamily;
import wiki.minecraft.heywiki.wiki.WikiIndividual;
import wiki.minecraft.heywiki.wiki.WikiPage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.concurrent.CompletableFuture;

import static wiki.minecraft.heywiki.HeyWikiClient.id;
import static wiki.minecraft.heywiki.HeyWikiClient.openWikiSearchKey;
import static wiki.minecraft.heywiki.util.HttpUtil.encodeUrl;

public class WikiSearchScreen extends Screen {
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final CachedDebouncer<String, SequencedSet<SearchProvider.Suggestion>> debouncer = new CachedDebouncer<>(
            400);
    private SimplePositioningWidget layout = new SimplePositioningWidget(0, 0, this.width, this.height);
    private TextFieldWidget textField;
    private final List<Identifier> textures = new LinkedList<>();
    private SuggestionEntryListWidget entryList;
    private String selectedTitle;
    private String lastSearchTerm;
    private SequencedSet<SearchProvider.Suggestion> suggestions;
    private WikiFamily wikiFamily = MOD.familyManager().getFamily(MOD.config().searchDefaultWikiFamily());
    private WikiIndividual wiki = wikiFamily.getWiki();

    public WikiSearchScreen() {
        super(Text.translatable("gui.heywiki_search.title",
                                Text.literal("minecraft").fillStyle(Style.EMPTY.withUnderline(true))));
    }

    @Override protected void init() {
        super.init();
        this.layout = new SimplePositioningWidget(0, 0, this.width, this.height);
        this.clearChildren();

        var mainLayout = this.layout.add(DirectionalLayoutWidget.vertical().spacing(10));
        mainLayout.getMainPositioner().alignHorizontalCenter();
        mainLayout.add(new TextWidget(Text.translatable("gui.heywiki_search.title",
                                                        Text.translatable(wikiFamily.getTranslationKey())
                                                            .fillStyle(Style.EMPTY.withUnderline(true))),
                                      this.textRenderer));

        var omniboxLayout = mainLayout.add(DirectionalLayoutWidget.vertical().spacing(4));
        omniboxLayout.getMainPositioner().alignHorizontalCenter();

        var textFieldLayout = omniboxLayout.add(DirectionalLayoutWidget.horizontal().spacing(4));

        this.textField = this.textField == null
                ? new TextFieldWidget(this.textRenderer, 175, 20, Text.translatable("gui.heywiki_search.title",
                                                                                    Text.translatable(
                                                                                                wikiFamily.getTranslationKey())
                                                                                        .fillStyle(
                                                                                                Style.EMPTY.withUnderline(
                                                                                                        true))))
                : this.textField;

        this.textField.setPlaceholder(Text.translatable("gui.heywiki_search.placeholder"));
        this.textField.setChangedListener(this::onSearchChange);

        this.setInitialFocus(this.textField);
        textFieldLayout.add(this.textField);

        textFieldLayout.add(ButtonWidget.builder(Text.translatable("gui.heywiki_search.switch_wiki"), button -> {
            assert this.client != null;
            this.client.setScreen(new WikiSelectScreen(this, MOD.familyManager().getAvailableFamilies().stream()
                                                                .filter(family -> family.getWiki().searchProviderType()
                                                                                        .isPresent()).sorted().toList(),
                                                       this.wikiFamily, (family) -> {
                this.wikiFamily = family;
                this.wiki = family.getWiki();
                this.entryList.clearSuggestions();
                this.init();
            }));
        }).width(71).build());

        this.entryList = new SuggestionEntryListWidget(client, 250, 24 * 6, 0, this);

        wiki.searchProvider().ifPresentOrElse(url -> omniboxLayout.add(this.entryList), () -> {
            var layout = omniboxLayout.add(DirectionalLayoutWidget.vertical().spacing(4));
            layout.getMainPositioner().alignHorizontalCenter();
            layout.add(new TextWidget(Text.translatable("gui.heywiki_search.no_suggestions"), this.textRenderer));
            layout.add(ButtonWidget.builder(Text.translatable("gui.heywiki_search.search"), button -> {
                if (wiki.searchUrl().isPresent()) {
                    String url = wiki.searchUrl().orElseThrow().formatted(this.getSearchTerm());
                    Util.getOperatingSystem().open(url);
                }
            }).width(100).build());
        });

        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
        this.onSearchChange(this.textField.getText());
    }

    private void onSearchChange(String term) {
        if (term.equals(this.selectedTitle)) {
            return;
        }
        this.lastSearchTerm = term;

        if (term.isEmpty()) {
            this.entryList.clearSuggestions();
            return;
        }

        CompletableFuture.runAsync(() -> this.fetchSuggestions(term).ifPresent(suggestions -> executor.execute(() -> {
            this.entryList.replaceSuggestions(suggestions);
            this.suggestions = suggestions;
        })), Util.getDownloadWorkerExecutor());
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        SuggestionEntryWidget selected = this.entryList.getSelectedOrNull();
        String searchTerm = this.textField.getText();
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            if (searchTerm.isEmpty() || (this.getFocused() != this.entryList && this.getFocused() != this.textField))
                return super.keyPressed(keyCode, scanCode, modifiers);

            searchEntry(selected);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void searchEntry(SuggestionEntryWidget selected) {
        if (selected != null) {
            if (selected.suggestion.realUrl().isPresent()) {
                Util.getOperatingSystem().open(URI.create(selected.suggestion.realUrl().get()));
            } else {
                var page = new WikiPage(selected.suggestion.title(), this.wiki);
                page.openInBrowser(this);
            }
        } else if (this.suggestions != null && !this.suggestions.isEmpty() &&
                   this.lastSearchTerm.equalsIgnoreCase(this.suggestions.getFirst().title())) {
            if (this.suggestions.getFirst().realUrl().isPresent()) {
                Util.getOperatingSystem().open(URI.create(this.suggestions.getFirst().realUrl().get()));
            } else {
                var page = new WikiPage(this.suggestions.getFirst().title(), this.wiki);
                page.openInBrowser(this);
            }
        } else if (wiki.searchUrl().isPresent()) {
            String url = wiki.searchUrl().get().formatted(encodeUrl(this.lastSearchTerm));
            Util.getOperatingSystem().open(url);
        }
    }

    @Override public void close() {
        assert this.client != null;
        this.executor.execute(() -> this.textures.forEach(this.client.getTextureManager()::destroyTexture));
        super.close();
    }

    @Override protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        SimplePositioningWidget.setPos(this.layout, this.getNavigationFocus());
    }

    public void updateSelectedSuggestion(SearchProvider.Suggestion suggestion) {
        this.selectedTitle = suggestion.title();
        this.textField.setText(suggestion.title());
    }

    public String getSearchTerm() {
        return this.lastSearchTerm;
    }

    private Optional<SequencedSet<SearchProvider.Suggestion>> fetchSuggestions(String term) {
        if (term.isEmpty()) {
            return Optional.empty();
        }
        try {
            var suggestions = debouncer.get(wikiFamily.id().toString() + term,
                                            () -> wiki.searchProvider().orElseThrow().search(term, wiki));
            suggestions.ifPresent(s -> s.forEach(this::fetchImage));

            return suggestions;
        } catch (Exception e) {
            LOGGER.warn("Failed to get suggestions", e);
            return Optional.empty();
        }
    }

    private void fetchImage(SearchProvider.Suggestion suggestion) {
        suggestion.imageUrl().ifPresent(imageUrl -> CompletableFuture.runAsync(() -> {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            String hash = Hex.encodeHexString(md.digest(imageUrl.getBytes(StandardCharsets.UTF_8)));

            var textureId = id(hash);

            assert this.client != null;
            if (this.client.getTextureManager().getTexture(textureId) != null) return;

            this.executor.execute(() -> this.textures.add(textureId));
            byte[] imageArray = HttpUtil.loadAndCacheFile(imageUrl).join();

            this.executor.execute(() -> {
                try {
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageArray));

                    int size = Math.min(image.getWidth(), image.getHeight());
                    image = image.getSubimage((image.getWidth() - size) / 2, (image.getHeight() - size) / 2, size,
                                              size);

                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", os);
                    InputStream is = new ByteArrayInputStream(os.toByteArray());

                    NativeImage nativeImage = NativeImage.read(is);
                    // noinspection ConstantValue
                    if (nativeImage == null) throw new RuntimeException("Texture is null!");
                    NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
                    this.client.getTextureManager().registerTexture(textureId, texture);
                    this.init();
                } catch (IOException e) {
                    LOGGER.error("Failed to load image", e);
                }
            });
        }, Util.getDownloadWorkerExecutor()));
    }

    public static void onClientTickPost(MinecraftClient client) {
        while (openWikiSearchKey.wasPressed()) {
            client.setScreen(new WikiSearchScreen());
        }
    }
}
