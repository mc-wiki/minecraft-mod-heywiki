package wiki.minecraft.heywiki.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import org.lwjgl.glfw.GLFW;
import wiki.minecraft.heywiki.gui.screen.WikiSearchScreen;

import java.util.SequencedSet;

public class SuggestionEntryListWidget extends AlwaysSelectedEntryListWidget<SuggestionEntryWidget> {
    public final WikiSearchScreen parent;

    public SuggestionEntryListWidget(MinecraftClient client, int width, int height, int y,
                                     WikiSearchScreen parent) {
        super(client, width, height, y, 24);
        this.parent = parent;
    }

    public void select(SuggestionEntryWidget entry) {
        this.setSelected(entry);
    }

    @Override
    protected int getScrollbarX() {
        return this.getX() + this.width;
    }

    @Override
    public int getRowWidth() {
        return this.width - 16;
    }

    @Override
    public int getRowLeft() {
        return this.getX() - 12;
    }

    @Override
    public boolean isFocused() {
        return this.parent.getFocused() == this;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        } else {
            return this.getSelectedOrNull() != null &&
                   this.getSelectedOrNull().keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void setSelected(SuggestionEntryWidget entry) {
        super.setSelected(entry);
        if (entry != null) {
            this.parent.updateSelectedSuggestion(entry.suggestion);
        }
    }

    public void clearSuggestions() {
        this.clearEntries();
    }

    public void replaceSuggestions(SequencedSet<WikiSearchScreen.Suggestion> suggestions) {
        this.replaceEntries(
                suggestions.stream().map(suggestion -> new SuggestionEntryWidget(suggestion, this)).toList());
    }
}
