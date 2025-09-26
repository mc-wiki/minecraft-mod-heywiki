package wiki.minecraft.heywiki.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererMixin {
    @Invoker("pick")
    HitResult invokePick(Entity camera, double blockInteractionRange, double entityInteractionRange,
                         float tickDelta);
}
