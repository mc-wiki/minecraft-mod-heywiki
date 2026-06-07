package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DebugScreenEntries.class)
public interface DebugScreenEntriesMixin {
    @Invoker("register")
    static Identifier invokeRegister(Identifier name, DebugScreenEntry entry) {
        throw new AssertionError();
    }
}
