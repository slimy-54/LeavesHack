package com.dev.leavesHack.asm.mixin;

import com.dev.leavesHack.modules.GlobalSetting;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
public class MixinGameRenderer {

    @Redirect(
        method = "calculateFov",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F", ordinal = 0)
    )
    private float removeSprintFov(float delta, float start, float end) {
        if (GlobalSetting.INSTANCE != null && GlobalSetting.INSTANCE.isActive() && !GlobalSetting.INSTANCE.changeFov.get()) {
            return 1.0F;
        }
        return Mth.lerp(delta, start, end);
    }
}
