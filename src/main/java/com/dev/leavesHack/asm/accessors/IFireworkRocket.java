package com.dev.leavesHack.asm.accessors;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FireworkRocketEntity.class)
public interface IFireworkRocket {
    @Accessor("life")
    int getLife();

    @Accessor("lifetime")
    int getLifetime();

    @Accessor("life")
    void setLife(int life);

    @Accessor("lifetime")
    void setLifetime(int lifetime);
}
