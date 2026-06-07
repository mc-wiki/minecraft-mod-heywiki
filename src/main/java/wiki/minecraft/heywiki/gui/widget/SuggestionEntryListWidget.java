package wiki.minecraft.heywiki.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import wiki.minecraft.heywiki.gui.screen.WikiSearchScreen;
import wiki.minecraft.heywiki.wiki.SearchProvider;

import java.util.SequencedSet;

public class SuggestionEntryListWidget extends ObjectSelectionList<SuggestionEntryWidget> {
    public final WikiSearchScreen parent;

    public SuggestionEntryListWidget(Minecraft client, int width, int height, int y,
                                     WikiSearchScreen parent) {
        super(client, width, height, y, 24);
        this.parent = parent;
    }

    public void select(SuggestionEntryWidget entry) {
        this.setSelected(entry);
    }

    public void setSelected(SuggestionEntryWidget entry) {
        super.setSelected(entry);
        if (entry != null) {
            this.parent.updateSelectedSuggestion(entry.suggestion);
        }
    }

    @Override
    protected int scrollBarX() {
        return this.getX() + this.width;
    }

    @Override
    public int getRowLeft() {
        return this.getX() + 6;
    }

    @Override
    public int getRowWidth() {
        return this.width - 12;
    }

    @Override
    public boolean isFocused() {
        return this.parent.getFocused() == this;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.key() == GLFW.GLFW_KEY_UP || keyEvent.key() == GLFW.GLFW_KEY_DOWN) {
            return super.keyPressed(keyEvent);
        } else {
            return this.getSelected() != null &&
                   this.getSelected().keyPressed(keyEvent);
        }
    }

    public void clearSuggestions() {
        this.clearEntries();
    }

    public void replaceSuggestions(SequencedSet<SearchProvider.Suggestion> suggestions) {
        this.replaceEntries(
                suggestions.stream().map(suggestion -> new SuggestionEntryWidget(suggestion, this)).toList());
    }
}
