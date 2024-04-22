package wiki.minecraft.heywiki.wiki;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

public class Target {
    Identifier identifier;
    String translationKey;

    public Target(Identifier identifier, String translationKey) {
        this.identifier = identifier;
        this.translationKey = translationKey;
    }

    public static Target of(Block block) {
        if (block instanceof AirBlock) return null;
        return new Target(block.arch$registryName(), block.getTranslationKey());
    }

    public static Target of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return new Target(stack.getItem().arch$registryName(), stack.getTranslationKey());
    }

    public static Target of(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getStack();
            return Target.of(stack);
        }

        return new Target(entity.getType().arch$registryName(), entity.getType().getTranslationKey());
    }

    public static Target of(RegistryEntry<Biome> biomeRegistryEntry) {
        var key = biomeRegistryEntry.getKey();
        if (key.isEmpty()) return null;
        Identifier identifier = key.get().getValue();

        return new Target(identifier, identifier.toTranslationKey("biome"));
    }
}
