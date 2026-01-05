package wiki.minecraft.heywiki.gui.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
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
 * @see net.minecraft.client.gui.screens.ConfirmLinkScreen
 */
public class ConfirmWikiPageScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final TextureManager textureManager = Minecraft.getInstance().getTextureManager();
    protected final BooleanConsumer callback;
    private final String link;
    private final Component message;
    private final WikiPage page;
    private FrameLayout layout = new FrameLayout(0, 0, this.width, this.height);
    private Identifier textureId = Identifier.fromNamespaceAndPath("minecraft",
                                                                   "textures/misc/unknown_server.png");
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
        this(callback, Component.translatable("gui.heywiki_confirm_link.title"),
             Component.literal(URLDecoder.decode(link, StandardCharsets.UTF_8)), link,
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
    private ConfirmWikiPageScreen(BooleanConsumer callback, Component title, Component message, String link,
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
        assert this.minecraft != null;

        excerpt.thenAccept(result -> {
            if (result == null) {
                hasExcerpt = false;
                this.minecraft.execute(this::init);
            }
            this.excerpt = result;

            if (this.excerpt.imageUrl() != null) {
                HttpUtil.loadAndCacheFile(this.excerpt.imageUrl()).thenAccept(image -> {
                    this.image = image;
                    this.minecraft.execute(this::init);
                });
            }
            this.minecraft.execute(this::init);
        });
    }

    private ImageWidget createImageWidget() {
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

        assert minecraft != null;
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
                DynamicTexture texture = new DynamicTexture(() -> this.textureId.toString(),
                                                            NativeImage.read(is));

                textureManager.register(this.textureId, texture);
            } catch (Exception e) {
                LOGGER.error("Failed to load image", e);
            }
        }

        return ImageWidget.texture(width, height, this.textureId, width, height);
    }

    protected LinearLayout createButtonLayout() {
        LinearLayout layout = LinearLayout.horizontal().spacing(8);

        layout.addChild(Button.builder(Component.translatable("chat.link.open"), button -> this.callback.accept(true))
                              .width(100).build());
        layout.addChild(Button.builder(Component.translatable("chat.copy"), button -> {
            this.copyToClipboard();
            this.callback.accept(false);
        }).width(100).build());
        layout.addChild(
                Button.builder(CommonComponents.GUI_CANCEL, button -> this.callback.accept(false)).width(100).build());

        return layout;
    }

    public void copyToClipboard() {
        assert this.minecraft != null;
        this.minecraft.keyboardHandler.setClipboard(this.link);
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
        Minecraft client = Minecraft.getInstance();
        client.setScreen(new ConfirmWikiPageScreen((confirmed) -> {
            if (confirmed) {
                Util.getPlatform().openUri(url);
                if (client.level != null) {
                    CallbackGameMenuScreen.openWithParent(parent, true);
                }
            } else {
                client.setScreen(parent);
            }
        }, url, excerpt, page));
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinLines(super.getNarrationMessage(), this.message);
    }

    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.key() == GLFW.GLFW_KEY_ENTER || openWikiKey.matches(keyEvent)) {
            this.callback.accept(true);
            return true;
        } else if (keyEvent.key() == GLFW.GLFW_KEY_C && keyEvent.hasControlDown() && !keyEvent.hasShiftDown() &&
                   !keyEvent.hasAltDown()) {
            this.callback.accept(false);
            this.copyToClipboard();
            return false;
        } else if (keyEvent.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.callback.accept(false);
            return true;
        }

        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        if (!this.textureId.equals(
                Identifier.fromNamespaceAndPath("minecraft", "textures/misc/unknown_server.png"))) {
            textureManager.release(this.textureId);
        }
        super.onClose();
    }

    @Override
    protected synchronized void init() {
        super.init();
        this.layout = new FrameLayout(0, 0, this.width, this.height);
        this.clearWidgets();

        LinearLayout mainLayout = this.layout.addChild(LinearLayout.vertical().spacing(10));
        mainLayout.defaultCellSetting().alignHorizontallyCenter();

        var pageTitle = Component.literal(this.excerpt != null ? this.excerpt.title() : this.page.pageName())
                                 .copy().withStyle(style -> style.withBold(true).withUnderlined(true));

        if (!hasExcerpt) {
            mainLayout.addChild(new StringWidget(pageTitle, this.font));
            mainLayout
                    .addChild(FocusableTextWidget.builder(
                                                         this.message.copy().setStyle(
                                                                 Style.EMPTY.withColor(ChatFormatting.GRAY)
                                                                            .withUnderlined(true)),
                                                         this.font)
                                                 .alwaysShowBorder(false)
//                                                 .maxWidth(this.width)
                                                 .backgroundFill(FocusableTextWidget.BackgroundFill.ALWAYS)
                                                 .build(),
                              positioner -> positioner.paddingVertical(3))
                    .setCentered(false);
        } else {
            LinearLayout excerptLayout = mainLayout.addChild(LinearLayout.horizontal().spacing(8));

            ImageWidget iconWidget = createImageWidget();
            int imageWidth = iconWidget.getWidth();
            excerptLayout.addChild(iconWidget, positioner -> positioner.padding(5));

            LinearLayout excerptTextLayout = excerptLayout.addChild(
                    LinearLayout.vertical().spacing(8));
            excerptTextLayout.addChild(new StringWidget(pageTitle, this.font));
            excerptTextLayout
                    .addChild(FocusableTextWidget.builder(
                                                         this.message.copy().setStyle(
                                                                 Style.EMPTY.withColor(ChatFormatting.GRAY)
                                                                            .withUnderlined(true)),
                                                         this.font, 0)
                                                 .alwaysShowBorder(false)
                                                 .maxWidth(this.width - 65 - (imageWidth + 13))
                                                 .backgroundFill(FocusableTextWidget.BackgroundFill.NEVER)
                                                 .build()
                             )
                    .setCentered(false);
            excerptTextLayout
                    .addChild(
                              FocusableTextWidget.builder(
                                                         this.excerpt != null
                                                                 ? Component.literal(this.excerpt.excerpt().replace("\u200B", ""))
                                                                 : Component.translatable("gui.heywiki_confirm_link.loading_excerpt"),
                                                         this.font)
//                                                 .alwaysShowBorder(false)
                                                 .maxWidth(this.width - 65 - (imageWidth + 13))
//                                                 .backgroundFill(FocusableTextWidget.BackgroundFill.ALWAYS)
                                                 .build()).setCentered(false);
        }

        LinearLayout buttonLayout = mainLayout.addChild(LinearLayout.vertical().spacing(8));
        buttonLayout.defaultCellSetting().alignHorizontallyCenter();

        buttonLayout.addChild(this.createButtonLayout(), positioner -> positioner.paddingBottom(20));

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override protected void repositionElements() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }
}
