package wiki.minecraft.heywiki.extension;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface MerchantScreenInterface {
    @Nullable
    ItemStack heywiki$getHoveredStack();
}
