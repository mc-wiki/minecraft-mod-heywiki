package wiki.minecraft.heywiki.gui.screen;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import wiki.minecraft.heywiki.util.HttpUtil;
import wiki.minecraft.heywiki.wiki.PageExcerpt;
import wiki.minecraft.heywiki.wiki.WikiPage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static wiki.minecraft.heywiki.HeyWikiClient.id;
import static wiki.minecraft.heywiki.HeyWikiClient.openWikiKey;

/**
 * A screen that asks the user to confirm opening a link. It also shows a preview of the linked page.
 *
 * @see net.minecraft.client.gui.screen.ConfirmLinkScreen
 */
public class ConfirmWikiPageScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
    protected final BooleanConsumer callback;
    private final String link;
    private final Text message;
    private final WikiPage page;
    private SimplePositioningWidget layout = new SimplePositioningWidget(0, 0, this.width, this.height);
    private Identifier textureId = Identifier.of("minecraft", "textures/misc/unknown_server.png");
    private volatile PageExcerpt excerpt;
    private volatile boolean hasExcerpt = false;
    private volatile byte[] image = null;

    /**
     * Creates a new screen.
     *
     * @param callback The callback to run when the user confirms the link.
     * @param link     The link to open.
     * @param excerpt  The excerpt of the page.
     * @param page     The wiki page.
     */
    public ConfirmWikiPageScreen(BooleanConsumer callback, String link,
                                 Optional<CompletableFuture<PageExcerpt>> excerpt,
                                 WikiPage page) {
        this(callback, Text.translatable("gui.heywiki_confirm_link.title"),
             Text.literal(URLDecoder.decode(link, StandardCharsets.UTF_8)), link,
             excerpt, page);
    }

    /**
     * Creates a new screen.
     *
     * @param callback The callback to run when the user confirms the link.
     * @param title    The title of the screen.
     * @param message  The message to display.
     * @param link     The link to open.
     * @param excerpt  The excerpt of the page.
     * @param page     The wiki page.
     */
    private ConfirmWikiPageScreen(BooleanConsumer callback, Text title, Text message, String link,
                                  Optional<CompletableFuture<PageExcerpt>> excerpt, WikiPage page) {
        super(title);
        this.callback = callback;
        this.message = message;
        this.link = link;
        this.page = page;

        if (excerpt.isPresent()) {
            this.hasExcerpt = true;
            loadImage(excerpt.get());
        }
    }

    private void loadImage(@NotNull CompletableFuture<PageExcerpt> excerpt) {
        assert this.client != null;

        excerpt.thenAccept(result -> {
            if (result == null) {
                hasExcerpt = false;
                this.client.execute(this::init);
            }
            this.excerpt = result;

            if (this.excerpt.imageUrl() != null) {
                HttpUtil.loadAndCacheFile(this.excerpt.imageUrl()).thenAccept(image -> {
                    this.image = image;
                    this.client.execute(this::init);
                });
            }
            this.client.execute(this::init);
        });
    }

    /**
     * Opens the screen. When confirmed, the link will be opened in the user's browser.
     *
     * @param parent  The parent screen.
     * @param url     The URL to open.
     * @param excerpt The excerpt of the page.
     * @param page    The wiki page.
     */
    public static void open(Screen parent, String url, Optional<CompletableFuture<PageExcerpt>> excerpt,
                            WikiPage page) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new ConfirmWikiPageScreen((confirmed) -> {
            if (confirmed) {
                Util.getOperatingSystem().open(url);
                if (client.world != null) {
                    CallbackGameMenuScreen.openWithParent(parent, true);
                }
            } else {
                client.setScreen(parent);
            }
        }, url, excerpt, page));
    }

    @Override
    protected synchronized void init() {
        super.init();
        this.layout = new SimplePositioningWidget(0, 0, this.width, this.height);
        this.clearChildren();

        DirectionalLayoutWidget mainLayout = this.layout.add(DirectionalLayoutWidget.vertical().spacing(10));
        mainLayout.getMainPositioner().alignHorizontalCenter();

        var pageTitle = Text.of(this.excerpt != null ? this.excerpt.title() : this.page.pageName())
                            .copy().styled(style -> style.withBold(true).withUnderline(true));

        if (!hasExcerpt) {
            mainLayout.add(new TextWidget(pageTitle, this.textRenderer));
            mainLayout
                    .add(new NarratedMultilineTextWidget(this.width,
                                                         this.message.copy()
                                                                     .setStyle(
                                                                             Style.EMPTY.withColor(Formatting.GRAY)
                                                                                        .withUnderline(true)),
                                                         this.textRenderer, false, false, 3),
                         positioner -> positioner.marginY(3))
                    .setCentered(false);
        } else {
            DirectionalLayoutWidget excerptLayout = mainLayout.add(DirectionalLayoutWidget.horizontal().spacing(8));

            IconWidget iconWidget = createImageWidget();
            int imageWidth = iconWidget.getWidth();
            excerptLayout.add(iconWidget, positioner -> positioner.margin(5));

            DirectionalLayoutWidget excerptTextLayout = excerptLayout.add(
                    DirectionalLayoutWidget.vertical().spacing(8));
            excerptTextLayout.add(new TextWidget(pageTitle, this.textRenderer));
            excerptTextLayout
                    .add(new NarratedMultilineTextWidget(this.width,
                                                         this.message.copy()
                                                                     .setStyle(Style.EMPTY.withColor(Formatting.GRAY)
                                                                                          .withUnderline(true)),
                                                         this.textRenderer, false, false, 3))
                    .setCentered(false);
            excerptTextLayout
                    .add(new NarratedMultilineTextWidget(
                                 this.width - 65 - (imageWidth + 13),
                                 this.excerpt != null
                                         ? Text.of(this.excerpt.excerpt().replace("\u200B", ""))
                                         : Text.translatable("gui.heywiki_confirm_link.loading_excerpt"),
                                 this.textRenderer, 5),
                         positioner -> positioner.margin(5)).setCentered(false);
        }

        DirectionalLayoutWidget buttonLayout = mainLayout.add(DirectionalLayoutWidget.vertical().spacing(8));
        buttonLayout.getMainPositioner().alignHorizontalCenter();

        buttonLayout.add(this.createButtonLayout(), positioner -> positioner.marginBottom(20));

        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
    }

    private IconWidget createImageWidget() {
        int height = 100;
        int realWidth = this.excerpt != null ? this.excerpt.imageWidth() : 0;
        int realHeight = this.excerpt != null ? this.excerpt.imageHeight() : 0;
        var aspectRatio = (double) realWidth / realHeight;
        final int maxWidth = 200;
        int width = (int) (height * aspectRatio);
        int newWidth = -1;
        if (width > maxWidth) {
            width = maxWidth;
            newWidth = realHeight * (maxWidth / height);
        }

        assert client != null;
        if (this.image != null) {
            try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(this.image));

                if (newWidth != -1) {
                    int x = (realWidth - newWidth) / 2;
                    image = image.getSubimage(x, 0, newWidth, realHeight);
                }

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(image, "png", os);
                InputStream is = new ByteArrayInputStream(os.toByteArray());

                this.textureId = id(String.valueOf(this.link.hashCode()));
                NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> this.textureId.toString(),
                                                                                NativeImage.read(is));

                textureManager.registerTexture(this.textureId, texture);
            } catch (Exception e) {
                LOGGER.error("Failed to load image", e);
            }
        }

        return IconWidget.create(width, height, this.textureId, width, height);
    }

    protected DirectionalLayoutWidget createButtonLayout() {
        DirectionalLayoutWidget layout = DirectionalLayoutWidget.horizontal().spacing(8);

        layout.add(ButtonWidget.builder(Text.translatable("chat.link.open"), button -> this.callback.accept(true))
                               .width(100).build());
        layout.add(ButtonWidget.builder(Text.translatable("chat.copy"), button -> {
            this.copyToClipboard();
            this.callback.accept(false);
        }).width(100).build());
        layout.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.callback.accept(false)).width(100).build());

        return layout;
    }

    @Override
    public void close() {
        if (!this.textureId.equals(Identifier.of("minecraft", "textures/misc/unknown_server.png"))) {
            textureManager.destroyTexture(this.textureId);
        }
        super.close();
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        SimplePositioningWidget.setPos(this.layout, this.getNavigationFocus());
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(super.getNarratedTitle(), this.message);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || openWikiKey.matchesKey(keyCode, scanCode)) {
            this.callback.accept(true);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_C && hasControlDown() && !hasShiftDown() && !hasAltDown()) {
            this.callback.accept(false);
            this.copyToClipboard();
            return false;
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.callback.accept(false);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void copyToClipboard() {
        assert this.client != null;
        this.client.keyboard.setClipboard(this.link);
    }
}
