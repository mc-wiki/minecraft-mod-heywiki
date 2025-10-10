package wiki.minecraft.heywiki.gui.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
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
    private final CachedDebouncer<String, SequencedSet<SearchProvider.Suggestion>> debouncer = new CachedDebouncer<>(400);
    private final List<ResourceLocation> textures = new LinkedList<>();
    private FrameLayout layout = new FrameLayout(0, 0, this.width, this.height);
    private EditBox textField;
    private SuggestionEntryListWidget entryList;
    private String selectedTitle;
    private String lastSearchTerm;
    private SequencedSet<SearchProvider.Suggestion> suggestions;
    private WikiFamily wikiFamily = MOD.familyManager().getFamily(MOD.config().searchDefaultWikiFamily());
    private WikiIndividual wiki = wikiFamily.getWiki();

    public WikiSearchScreen() {
        super(Component.translatable(
            "gui.heywiki_search.title",
            Component.literal("minecraft").withStyle(Style.EMPTY.withUnderlined(true))
        ));
    }

    public static void onClientTickPost(Minecraft client) {
        if (openWikiSearchKey.consumeClick()) {
            client.setScreen(new WikiSearchScreen());
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        SuggestionEntryWidget selected = this.entryList.getSelected();
        String searchTerm = this.textField.getValue();
        if (keyEvent.key() == GLFW.GLFW_KEY_ENTER) {
            if (searchTerm.isEmpty() || (this.getFocused() != this.entryList && this.getFocused() != this.textField))
                return super.keyPressed(keyEvent);

            searchEntry(selected);
        }
        return super.keyPressed(keyEvent);
    }

    public void searchEntry(SuggestionEntryWidget selected) {
        if (selected != null) {
            if (selected.suggestion.realUrl().isPresent()) {
                Util.getPlatform().openUri(URI.create(selected.suggestion.realUrl().get()));
            } else {
                var page = new WikiPage(selected.suggestion.title(), this.wiki);
                page.openInBrowser(this);
            }
        } else if (this.suggestions != null && !this.suggestions.isEmpty() &&
            this.lastSearchTerm.equalsIgnoreCase(this.suggestions.getFirst().title())) {
            if (this.suggestions.getFirst().realUrl().isPresent()) {
                Util.getPlatform().openUri(URI.create(this.suggestions.getFirst().realUrl().get()));
            } else {
                var page = new WikiPage(this.suggestions.getFirst().title(), this.wiki);
                page.openInBrowser(this);
            }
        } else if (wiki.searchUrl().isPresent()) {
            String url = wiki.searchUrl().get().formatted(encodeUrl(this.lastSearchTerm));
            Util.getPlatform().openUri(url);
        }
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.screenExecutor.execute(() -> this.textures.forEach(this.minecraft.getTextureManager()::release));
        super.onClose();
    }

    @Override
    protected void init() {
        super.init();
        this.layout = new FrameLayout(0, 0, this.width, this.height);
        this.clearWidgets();

        var title = Component.translatable(
            "gui.heywiki_search.title",
            Component.translatable(wikiFamily.getTranslationKey())
                     .withStyle(Style.EMPTY.withUnderlined(true))
        );
        var titleWidget = new StringWidget(title, this.font);
        var mainLayout = this.layout.addChild(LinearLayout.vertical().spacing(10));
        mainLayout.defaultCellSetting().alignHorizontallyCenter();
        mainLayout.addChild(titleWidget);

        var omniboxLayout = mainLayout.addChild(LinearLayout.vertical().spacing(4));
        omniboxLayout.defaultCellSetting().alignHorizontallyCenter();

        var textFieldLayout = omniboxLayout.addChild(LinearLayout.horizontal().spacing(4));

        this.textField = this.textField == null
                         ? new EditBox(this.font, 175, 20, title)
                         : this.textField;

        this.textField.setHint(Component.translatable("gui.heywiki_search.placeholder"));
        this.textField.setResponder(this::onSearchChange);

        this.setInitialFocus(this.textField);
        textFieldLayout.addChild(this.textField);

        textFieldLayout.addChild(Button.builder(
            Component.translatable("gui.heywiki_search.switch_wiki"), button -> {
                assert this.minecraft != null;
                this.minecraft.setScreen(
                    new WikiSelectScreen(
                        this,
                        MOD.familyManager().getAvailableFamilies().stream()
                           .filter(family -> family.getWiki().searchProviderType().isPresent())
                           .sorted().toList(),
                        this.wikiFamily,
                        (family) -> {
                            if (family == this.wikiFamily)
                                return;
                            this.wikiFamily = family;
                            this.wiki = family.getWiki();
                            this.entryList.clearSuggestions();
                            var changedTitle = Component.translatable(
                                "gui.heywiki_search.title",
                                Component.translatable(wikiFamily.getTranslationKey())
                                         .withStyle(Style.EMPTY.withUnderlined(true))
                            );
                            titleWidget.setMessage(changedTitle);
                            this.textField.setMessage(changedTitle);
                            this.selectedTitle = null;
                            String lastTerm = getSearchTerm();
                            if (lastTerm != null && !lastTerm.isEmpty())
                                onSearchChange(lastTerm);
                        }
                    )
                );
            }
        ).width(71).build());

        this.entryList = new SuggestionEntryListWidget(minecraft, 250, 24 * 6, 0, this);

        wiki.searchProvider().ifPresentOrElse(
            url -> omniboxLayout.addChild(this.entryList), () -> {
                var layout = omniboxLayout.addChild(LinearLayout.vertical().spacing(4));
                layout.defaultCellSetting().alignHorizontallyCenter();
                layout.addChild(new StringWidget(
                    Component.translatable("gui.heywiki_search.no_suggestions"),
                    this.font
                ));
                layout.addChild(Button.builder(
                    Component.translatable("gui.heywiki_search.search"), button -> {
                        if (wiki.searchUrl().isPresent()) {
                            String url = wiki.searchUrl().orElseThrow().formatted(this.getSearchTerm());
                            Util.getPlatform().openUri(url);
                        }
                    }
                ).width(100).build());
            }
        );

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
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

        CompletableFuture.runAsync(
            () -> this.fetchSuggestions(term).ifPresent(suggestions -> screenExecutor.execute(() -> {
                this.entryList.replaceSuggestions(suggestions);
                this.suggestions = suggestions;
            })), Util.ioPool()
        );
    }

    public String getSearchTerm() {
        return this.lastSearchTerm;
    }

    private Optional<SequencedSet<SearchProvider.Suggestion>> fetchSuggestions(String term) {
        if (term.isEmpty()) {
            return Optional.empty();
        }
        try {
            var suggestions = debouncer.get(
                wikiFamily.id().toString() + term,
                () -> wiki.searchProvider().orElseThrow().search(term, wiki)
            );
            suggestions.ifPresent(s -> s.forEach(this::fetchImage));

            return suggestions;
        } catch (Exception e) {
            LOGGER.warn("Failed to get suggestions", e);
            return Optional.empty();
        }
    }

    private void fetchImage(SearchProvider.Suggestion suggestion) {
        suggestion.imageUrl().ifPresent(imageUrl -> CompletableFuture.runAsync(
            () -> {
                MessageDigest md;
                try {
                    md = MessageDigest.getInstance("SHA-1");
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                String hash = Hex.encodeHexString(md.digest(imageUrl.getBytes(StandardCharsets.UTF_8)));

                var textureId = id(hash);

                assert this.minecraft != null;

                this.screenExecutor.execute(() -> this.textures.add(textureId));
                byte[] imageArray = HttpUtil.loadAndCacheFile(imageUrl).join();

                this.screenExecutor.execute(() -> {
                    try {
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageArray));
                        if (image == null) {
                            throw new RuntimeException("Failed to load image");
                        }

                        int size = Math.min(image.getWidth(), image.getHeight());
                        image = image.getSubimage(
                            (image.getWidth() - size) / 2, (image.getHeight() - size) / 2, size,
                            size
                        );

                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        ImageIO.write(image, "png", os);
                        InputStream is = new ByteArrayInputStream(os.toByteArray());

                        NativeImage nativeImage = NativeImage.read(is);
                        // noinspection ConstantValue
                        if (nativeImage == null)
                            throw new RuntimeException("Texture is null!");
                        DynamicTexture texture = new DynamicTexture(textureId::toString, nativeImage);
                        this.minecraft.getTextureManager().register(textureId, texture);
                        this.repositionElements();
                    } catch (IOException e) {
                        LOGGER.error("Failed to load image", e);
                    }
                });
            }, Util.ioPool()
        ));
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    public void updateSelectedSuggestion(SearchProvider.Suggestion suggestion) {
        this.selectedTitle = suggestion.title();
        this.textField.setValue(suggestion.title());
    }
}
