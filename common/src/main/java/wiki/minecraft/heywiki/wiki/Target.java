package wiki.minecraft.heywiki.wiki;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import static wiki.minecraft.heywiki.HeyWikiClient.experimentalWarning;

/**
 * Represents a target.
 *
 * <p>A target is an object, e.g. a block/item/entity, that can be linked to a wiki page.
 *
 * <p>A target is identified by an {@link Identifier} and a translation key.
 *
 * <p>The purpose of a target is usually to create a {@link WikiPage} from it.
 *
 * @param identifier     The identifier of the target.
 * @param translationKey The translation key of the target.
 */
public record Target(Identifier identifier, String translationKey) {
    private final static MapCodec<Target> CODEC = RecordCodecBuilder
            .mapCodec(builder ->
                              builder.group(
                                             Identifier.CODEC.fieldOf("heywiki:identifier")
                                                             .forGetter(target -> target.identifier),
                                             Codec.STRING.fieldOf("heywiki:translation_key")
                                                         .forGetter(target -> target.translationKey))
                                     .apply(builder, Target::new));

    /**
     * Creates a target from a block.
     *
     * @param block The block.
     * @return The target.
     */
    public static Target of(Block block) {
        if (block instanceof AirBlock) return null;
        return new Target(block.arch$registryName(), block.getTranslationKey());
    }

    /**
     * Creates a target from an entity.
     *
     * @param entity The entity.
     * @return The target.
     */
    public static Target of(Entity entity) {
        switch (entity) {
            case ItemEntity itemEntity -> {
                ItemStack stack = itemEntity.getStack();
                return Target.of(stack);
            }
            case ItemFrameEntity itemFrameEntity -> {
                ItemStack stack = itemFrameEntity.getHeldItemStack();
                if (stack.isEmpty())
                    return new Target(entity.getType().arch$registryName(), entity.getType().getTranslationKey());
                return Target.of(stack);
            }
            default -> {
                return new Target(entity.getType().arch$registryName(), entity.getType().getTranslationKey());
            }
        }
    }

    /**
     * Creates a target from an item stack.
     *
     * @param stack The item stack.
     * @return The target.
     */
    public static Target of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.getComponents().get(DataComponentTypes.CREATIVE_SLOT_LOCK) != null) return null;

        @Nullable NbtComponent customData = stack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            var target = customData.get(CODEC).result().orElse(null);
            if (target != null) {
                experimentalWarning("Custom item based on custom_data or NBT");
                return target;
            }
        }

        return new Target(stack.getItem().arch$registryName(), stack.getTranslationKey());
    }

    /**
     * Creates a target from a status effect instance.
     *
     * @param effect The status effect instance.
     * @return The target.
     */
    public static Target of(StatusEffectInstance effect) {
        RegistryEntry<StatusEffect> effectEntry = effect.getEffectType();
        var key = effectEntry.getKey();
        if (key.isEmpty()) return null;
        Identifier identifier = key.get().getValue();

        return new Target(identifier, effect.getTranslationKey());
    }

    /**
     * Creates a target from a registry entry.
     *
     * <p>This is useful to create a target for other objects that cannot be represented by an internal game object.
     * For example, a target for biomes/structures.
     *
     * @param registryEntry        The registry entry.
     * @param translationKeyPrefix The translation key prefix.
     * @return The target.
     */
    public static Target of(RegistryEntry<?> registryEntry, String translationKeyPrefix) {
        var key = registryEntry.getKey();
        if (key.isEmpty()) return null;
        Identifier identifier = key.get().getValue();

        return new Target(identifier, identifier.toTranslationKey(translationKeyPrefix));
    }
}
