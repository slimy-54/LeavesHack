package com.dev.leavesHack.asm.mixin;

import com.dev.leavesHack.modules.GlobalSetting;
import com.dev.leavesHack.utils.entity.InventoryUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class MixinInGameHud {
    @ModifyExpressionValue(
        method = "extractItemHotbar",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelectedSlot()I")
    )
    private int selectedSlot(int original) {
        if (GlobalSetting.INSTANCE != null && GlobalSetting.INSTANCE.isActive() && GlobalSetting.INSTANCE.clientSwitch.get()) {
            return InventoryUtil.serverSlot;
        }
        return original;
    }
}
