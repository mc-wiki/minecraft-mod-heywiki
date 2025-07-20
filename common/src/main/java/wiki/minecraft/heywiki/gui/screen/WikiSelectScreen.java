package wiki.minecraft.heywiki.gui.screen;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private WikiOptionListWidget optionList;

    public WikiSelectScreen(Screen parent, Iterable<WikiFamily> wikis, WikiFamily selected,
                            Consumer<WikiFamily> onSelection) {
        super(Component.translatable("gui.heywiki_search.switch_wiki"));
        this.parent = parent;
        this.wikis = wikis;
        this.selected = selected;
        this.onSelection = onSelection;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            this.onClose();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void onClose() {
        var selected = optionList.getSelected();
        if (selected != null) {
            this.onSelection.accept(selected.wiki);
            MOD.config().setSearchDefaultWikiFamily(selected.wiki.id());
            Util.nonCriticalIoPool().execute(() -> MOD.config().save(false));
        }
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void init() {
        this.clearWidgets();
        layout.addTitleHeader(Component.translatable("gui.heywiki_search.switch_wiki"), font);

        var bodyLayout = LinearLayout.vertical().spacing(5);
        this.optionList = bodyLayout.addChild(
                new WikiOptionListWidget(this.minecraft, this.width, this.height - 33 * 2, 0, 18, wikis, selected));

        layout.addToContents(bodyLayout);
        layout.addToFooter(
                Button.builder(Component.translatable("gui.done"), button -> this.onClose()).width(200).build());

        this.layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    public static class WikiOptionListWidget extends ObjectSelectionList<WikiOptionWidget> {
        public WikiOptionListWidget(Minecraft client, int width, int height, int y, int entryHeight,
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

    public static class WikiOptionWidget extends ObjectSelectionList.Entry<WikiOptionWidget> {
        private final WikiFamily wiki;
        private final Minecraft client;

        public WikiOptionWidget(WikiFamily wiki) {
            this.wiki = wiki;
            this.client = Minecraft.getInstance();
        }

        @Override
        public Component getNarration() {
            return Component.literal("Wiki: " + this.wiki.id());
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
                           int mouseY, boolean hovered, float tickDelta) {
            context.drawString(client.font, Component.translatable(this.wiki.getTranslationKey()),
                               x + 14, y + 2, 0xFFFFFFFF);
        }
    }
}