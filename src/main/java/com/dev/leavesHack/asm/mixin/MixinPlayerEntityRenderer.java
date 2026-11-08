package com.dev.leavesHack.asm.mixin;

import com.dev.leavesHack.events.ElytraUpdateEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(AvatarRenderer.class)
public class MixinPlayerEntityRenderer {
    @Inject(
        method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
        at = @At("HEAD")
    )
    private void onSetupRotations(AvatarRenderState instance, PoseStack matrices, float animationProgress, float bodyYaw, CallbackInfo ci) {
        if (mc.player != null && instance.id == mc.player.getId()) {
            MeteorClient.EVENT_BUS.post(new ElytraUpdateEvent(mc.player));
        }
    }
}
