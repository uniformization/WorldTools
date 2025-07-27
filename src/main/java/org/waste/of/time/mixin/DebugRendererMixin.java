package org.waste.of.time.mixin;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.waste.of.time.Events;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    public void renderInject(
            MatrixStack matrices,
            Frustum frustum,
            VertexConsumerProvider.Immediate vertexConsumers,
            double cameraX,
            double cameraY,
            double cameraZ,
            CallbackInfo ci
    ) {
        Events.INSTANCE.onDebugRenderStart(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
    }
}
