package wiki.minecraft.heywiki.target;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
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
     * Creates a target from a block.
     *
     * @param block The block.
     * @return The target.
     */
    static Target of(Block block) {
        if (block instanceof AirBlock) return null;
        return new IdentifierTarget(BuiltInRegistries.BLOCK.getKey(block), block.getDescriptionId());
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
                ItemStack stack = itemEntity.getItem();
                return Target.of(stack);
            }
            case ItemFrame itemFrameEntity -> {
                ItemStack stack = itemFrameEntity.getItem();
                if (stack.isEmpty())
                    return new IdentifierTarget(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()),
                                                entity.getType().getDescriptionId());
                return Target.of(stack);
            }
            default -> {
                return new IdentifierTarget(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()),
                                            entity.getType().getDescriptionId());
            }
        }
    }

    /**
     * Creates a target from an item stack.
     *
     * @param stack The item stack.
     * @return The target.
     */
    static @Nullable Target of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.getComponents().get(DataComponents.CREATIVE_SLOT_LOCK) != null) return null;

        @Nullable CustomData customData = stack.getComponents().get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            var target = customData.copyTag().read(IdentifierTarget.CODEC).orElse(null);
            if (target != null) {
                experimentalWarning(LogUtils.getLogger(), "Custom item based on custom_data or NBT");
                return target;
            }
        }

        return new IdentifierTarget(BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.getItem().getDescriptionId());
    }

    /**
     * Creates a target from a status effect instance.
     *
     * @param effect The status effect instance.
     * @return The target.
     */
    static Target of(MobEffectInstance effect) {
        Holder<MobEffect> effectEntry = effect.getEffect();
        var key = effectEntry.unwrapKey();
        if (key.isEmpty()) return null;
        Identifier identifier = key.get().identifier();

        return new IdentifierTarget(identifier, effect.getDescriptionId());
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
    static Target of(Holder<?> registryEntry, String translationKeyPrefix) {
        var key = registryEntry.unwrapKey();
        if (key.isEmpty()) return null;
        Identifier identifier = key.get().identifier();

        return new IdentifierTarget(identifier, identifier.toLanguageKey(translationKeyPrefix));
    }

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
}
