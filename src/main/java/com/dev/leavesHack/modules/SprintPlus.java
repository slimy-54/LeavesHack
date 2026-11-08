/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dev.leavesHack.modules;

import com.dev.leavesHack.LeavesHack;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.world.effect.MobEffects;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

public class SprintPlus extends Module {
    public static SprintPlus INSTANCE;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public enum Mode {
        Strict,
        Rage
    }

    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("sprint-mode")
        .description("What mode of sprinting.")
        .defaultValue(Mode.Strict)
        .build()
    );

    public final Setting<Boolean> unsprintInWater = sgGeneral.add(new BoolSetting.Builder()
        .name("unsprint-in-water")
        .description("Whether to stop sprinting when in water.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Rage)
        .build()
    );

    private final Setting<Boolean> permaSprint = sgGeneral.add(new BoolSetting.Builder()
        .name("sprint-while-stationary")
        .description("Sprint even when not moving.")
        .defaultValue(false)
        .visible(() -> mode.get() == Mode.Rage)
        .build()
    );

    public SprintPlus() {
        super(LeavesHack.LEAVES_MISC, "SprintPlus", "自动疾跑Plus");
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onTickMovement(TickEvent.Post event) {
        if (unsprintInWater.get() && mc.player.isInWater()) return;
        mc.player.setSprinting(shouldSprint());
    }


    public boolean shouldSprint() {
        if (FireworkElytraFly.INSTANCE.isActive()) return false;
        float movement = mode.get() == Mode.Rage
            ? (Math.abs(mc.player.input.getMoveVector().y) + Math.abs(mc.player.input.getMoveVector().x))
            : mc.player.input.getMoveVector().y;

        if (movement <= (mc.player.isUnderWater() ? 1.0E-5F : 0.8)) {
            if (mode.get() == Mode.Rage && !permaSprint.get()) return false;
        }

        boolean strictSprint = !(mc.player.isInWater())
            && !mc.player.hasEffect(MobEffects.BLINDNESS)
            && (mc.player.isPassenger() || mc.player.canSprint())
            && (!mc.player.horizontalCollision || mc.player.minorHorizontalCollision);

        return isActive() && (mode.get() == Mode.Rage || strictSprint);
    }
}
