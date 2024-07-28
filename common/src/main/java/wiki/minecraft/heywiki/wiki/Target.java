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
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

public record Target(Identifier identifier, String translationKey) {
    private final static MapCodec<Target> CODEC = RecordCodecBuilder
            .mapCodec(builder ->
                              builder.group(
                                             Identifier.CODEC.fieldOf("heywiki:identifier")
                                                             .forGetter(target -> target.identifier),
                                             Codec.STRING.fieldOf("heywiki:translation_key")
                                                         .forGetter(target -> target.translationKey))
                                     .apply(builder, Target::new));

    public static Target of(Block block) {
        if (block instanceof AirBlock) return null;
        return new Target(block.arch$registryName(), block.getTranslationKey());
    }

    public static Target of(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getStack();
            return Target.of(stack);
        }

        return new Target(entity.getType().arch$registryName(), entity.getType().getTranslationKey());
    }

    public static Target of(ItemStack stack) {
        if (stack.isEmpty()) return null;

        @Nullable NbtComponent customData = stack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            var target = customData.get(CODEC).result().orElse(null);
            if (target != null) return target;
        }

        return new Target(stack.getItem().arch$registryName(), stack.getTranslationKey());
    }

    public static Target of(RegistryEntry<Biome> biomeRegistryEntry) {
        var key = biomeRegistryEntry.getKey();
        if (key.isEmpty()) return null;
        Identifier identifier = key.get().getValue();

        return new Target(identifier, identifier.toTranslationKey("biome"));
    }

    public static Target of(StatusEffectInstance effect) {
        RegistryEntry<StatusEffect> effectEntry = effect.getEffectType();
        var key = effectEntry.getKey();
        if (key.isEmpty()) return null;
        Identifier identifier = key.get().getValue();

        return new Target(identifier, effect.getTranslationKey());
    }
}
