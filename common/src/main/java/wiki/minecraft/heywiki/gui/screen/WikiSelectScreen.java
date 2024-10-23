package wiki.minecraft.heywiki.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import wiki.minecraft.heywiki.HeyWikiClient;
import wiki.minecraft.heywiki.wiki.WikiFamily;

import java.util.function.Consumer;

public class WikiSelectScreen extends Screen {
    private static final HeyWikiClient MOD = HeyWikiClient.getInstance();
    private final Screen parent;
    private final Iterable<WikiFamily> wikis;
    private final WikiFamily selected;
    private final Consumer<WikiFamily> onSelection;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private WikiOptionListWidget optionList;

    public WikiSelectScreen(Screen parent, Iterable<WikiFamily> wikis, WikiFamily selected,
                            Consumer<WikiFamily> onSelection) {
        super(Text.translatable("gui.heywiki_search.switch_wiki"));
        this.parent = parent;
        this.wikis = wikis;
        this.selected = selected;
        this.onSelection = onSelection;
    }

    @Override
    protected void init() {
        this.clearChildren();
        layout.addHeader(Text.translatable("gui.heywiki_search.switch_wiki"), textRenderer);

        var bodyLayout = DirectionalLayoutWidget.vertical().spacing(5);
        this.optionList = bodyLayout.add(
                new WikiOptionListWidget(this.client, this.width, this.height - 33 * 2, 0, 18, wikis, selected));

        layout.addBody(bodyLayout);
        layout.addFooter(
                ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close()).width(200).build());

        this.layout.refreshPositions();
        layout.forEachChild(this::addDrawableChild);
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
    }

    @Override
    public void close() {
        var selected = optionList.getSelectedOrNull();
        if (selected != null) {
            this.onSelection.accept(selected.wiki);
            MOD.config().setSearchDefaultWikiFamily(selected.wiki.id());
            Util.getIoWorkerExecutor().execute(() -> MOD.config().save(false));
        }
        assert this.client != null;
        this.client.setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            this.close();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public static class WikiOptionListWidget extends AlwaysSelectedEntryListWidget<WikiOptionWidget> {
        public WikiOptionListWidget(MinecraftClient client, int width, int height, int y, int entryHeight,
                                    Iterable<WikiFamily> wikis, WikiFamily selected) {
            super(client, width, height, y, entryHeight);

            for (WikiFamily wiki : wikis) {
                var entry = new WikiOptionWidget(wiki);
                this.addEntry(entry);

                if (wiki == selected) {
                    this.setSelected(entry);
                }
            }
        }
    }

    public static class WikiOptionWidget extends AlwaysSelectedEntryListWidget.Entry<WikiOptionWidget> {
        private final WikiFamily wiki;
        private final MinecraftClient client;

        public WikiOptionWidget(WikiFamily wiki) {
            this.wiki = wiki;
            this.client = MinecraftClient.getInstance();
        }

        @Override
        public Text getNarration() {
            return Text.literal("Wiki: " + this.wiki.id());
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
                           int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(client.textRenderer, Text.translatable(this.wiki.getTranslationKey()),
                                       x + 14, y + 2, 0xFFFFFFFF);
        }
    }
}