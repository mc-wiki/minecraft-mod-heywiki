package wiki.minecraft.heywiki.gametest.wiki.target;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.Vec3d;
import wiki.minecraft.heywiki.target.Target;

public class TargetTest {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void resolvesBlock(TestContext context) {
        var block = Blocks.GRASS_BLOCK;
        var target = Target.of(block);
        assert target != null;
        context.assertEquals(target.namespace(), "minecraft", "does not have the correct NS");
        context.assertEquals(target.title(), "Grass Block", "does not have the correct Title");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void resolvesItem(TestContext context) {
        var item = Items.BONE;
        var target = Target.of(item.getDefaultStack());
        assert target != null;
        context.assertEquals(target.namespace(), "minecraft", "does not have the correct NS");
        context.assertEquals(target.title(), "Bone", "does not have the correct Title");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void resolvesEntity(TestContext context) {
        var entity = context.spawnEntity(EntityType.COW, new Vec3d(2, 0, 2));
        var target = Target.of(entity);
        assert target != null;
        context.assertEquals(target.namespace(), "minecraft", "does not have the correct NS");
        context.assertEquals(target.title(), "Cow", "does not have the correct Title");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void resolvesStatusEffect(TestContext context) {
        var effect = new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1);
        var target = Target.of(effect);
        assert target != null;
        context.assertEquals(target.namespace(), "minecraft", "does not have the correct NS");
        context.assertEquals(target.title(), "Regeneration", "does not have the correct Title");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void resolvesRegistryEntry(TestContext context) {
        var entry = Registries.BLOCK.getEntry(Blocks.GRASS_BLOCK);
        var target = Target.of(entry, "block");
        assert target != null;
        context.assertEquals(target.namespace(), "minecraft", "does not have the correct NS");
        context.assertEquals(target.title(), "Grass Block", "does not have the correct Title");
        context.complete();
    }
}