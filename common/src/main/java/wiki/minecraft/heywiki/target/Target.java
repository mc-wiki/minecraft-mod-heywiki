package wiki.minecraft.heywiki.target;

import com.mojang.logging.LogUtils;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import wiki.minecraft.heywiki.wiki.WikiPage;

import static wiki.minecraft.heywiki.HeyWikiClient.experimentalWarning;

/**
 * Represents an object, e.g., a block/item/entity, that can be linked to a wiki page.
 *
 * <p>A target must be able to be resolved to a {@linkplain #namespace() namespace} and a {@linkplain #title() title}.
 *
 * <p>The purpose of a target is usually to create a {@link WikiPage} from it.
 */
public interface Target {
    /**
     * Gets the namespace of the target.
     *
     * @return The namespace.
     */
    String namespace();

    /**
     * Gets the title of the target.
     *
     * @return The title.
     */
    String title();

    /**
     * Creates a target from a block.
     *
     * @param block The block.
     * @return The target.
     */
    static Target of(Block block) {
        if (block instanceof AirBlock) return null;
        return new IdentifierTarget(Registries.BLOCK.getId(block), block.getTranslationKey());
    }

    /**
     * Creates a target from an entity.
     *
     * @param entity The entity.
     * @return The target.
     */
    static Target of(Entity entity) {
        switch (entity) {
            case ItemEntity itemEntity -> {
                ItemStack stack = itemEntity.getStack();
                return Target.of(stack);
            }
            case ItemFrameEntity itemFrameEntity -> {
                ItemStack stack = itemFrameEntity.getHeldItemStack();
                if (stack.isEmpty())
                    return new IdentifierTarget(Registries.ENTITY_TYPE.getId(entity.getType()),
                                                entity.getType().getTranslationKey());
                return Target.of(stack);
            }
            default -> {
                return new IdentifierTarget(Registries.ENTITY_TYPE.getId(entity.getType()),
                                            entity.getType().getTranslationKey());
            }
        }
    }

    /**
     * Creates a target from an item stack.
     *
     * @param stack The item stack.
     * @return The target.
     */
    static Target of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.getComponents().get(DataComponentTypes.CREATIVE_SLOT_LOCK) != null) return null;

        @Nullable NbtComponent customData = stack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            var target = customData.get(IdentifierTarget.CODEC).result().orElse(null);
            if (target != null) {
                experimentalWarning(LogUtils.getLogger(), "Custom item based on custom_data or NBT");
                return target;
            }
        }

        return new IdentifierTarget(Registries.ITEM.getId(stack.getItem()), stack.getTranslationKey());
    }

    /**
     * Creates a target from a status effect instance.
     *
     * @param effect The status effect instance.
     * @return The target.
     */
    static Target of(StatusEffectInstance effect) {
        RegistryEntry<StatusEffect> effectEntry = effect.getEffectType();
        var key = effectEntry.getKey();
        if (key.isEmpty()) return null;
        Identifier identifier = key.get().getValue();

        return new IdentifierTarget(identifier, effect.getTranslationKey());
    }

    /**
     * Creates a target from a registry entry.
     *
     * <p>This is useful to create a target for other objects that cannot be represented by an internal game object.
     * For example, a target for biome/structure.
     *
     * @param registryEntry        The registry entry.
     * @param translationKeyPrefix The translation key prefix.
     * @return The target.
     */
    static Target of(RegistryEntry<?> registryEntry, String translationKeyPrefix) {
        var key = registryEntry.getKey();
        if (key.isEmpty()) return null;
        Identifier identifier = key.get().getValue();

        return new IdentifierTarget(identifier, identifier.toTranslationKey(translationKeyPrefix));
    }
}
