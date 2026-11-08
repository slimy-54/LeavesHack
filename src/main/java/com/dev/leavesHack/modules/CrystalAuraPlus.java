/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dev.leavesHack.modules;

import com.dev.leavesHack.LeavesHack;
import com.dev.leavesHack.utils.combat.CombatUtil;
import com.dev.leavesHack.utils.entity.InventoryUtil;
import com.dev.leavesHack.utils.world.BlockUtil;
import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.ints.*;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CrystalAuraPlus extends Module {
    public static CrystalAuraPlus INSTANCE;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSwitch = settings.createGroup("Switch");
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgFacePlace = settings.createGroup("Face Place");
    private final SettingGroup sgBreak = settings.createGroup("Break");
    private final SettingGroup sgPause = settings.createGroup("Pause");
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General

    private final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("TargetRange")
        .description("目标范围")
        .defaultValue(10)
        .min(0)
        .sliderMax(16)
        .build()
    );

    private final Setting<Boolean> predictMovement = sgGeneral.add(new BoolSetting.Builder()
        .name("PredictMovement")
        .description("预判移动")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> minDamage = sgGeneral.add(new DoubleSetting.Builder()
        .name("MinDamage")
        .description("最低敌伤")
        .defaultValue(6)
        .min(0)
        .build()
    );

    private final Setting<Double> maxDamage = sgGeneral.add(new DoubleSetting.Builder()
        .name("MaxDamage")
        .description("最大己伤")
        .defaultValue(6)
        .range(0, 36)
        .sliderMax(36)
        .build()
    );

    private final Setting<Boolean> antiSuicide = sgGeneral.add(new BoolSetting.Builder()
        .name("AntiSuicide")
        .description("防自杀")
        .defaultValue(true)
        .build()
    );

    public final Setting<PreferMode> preferMode = sgGeneral.add(new EnumSetting.Builder<PreferMode>()
        .name("PreferMode")
        .description("优先模式")
        .defaultValue(PreferMode.PreferAnchor)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("Rotate")
        .description("转头")
        .defaultValue(true)
        .build()
    );

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("Entities")
        .description("目标选择")
        .onlyAttackable()
        .defaultValue(EntityType.PLAYER, EntityType.WARDEN, EntityType.WITHER)
        .build()
    );

    // Switch

    private final Setting<AutoSwitchMode> autoSwitch = sgSwitch.add(new EnumSetting.Builder<AutoSwitchMode>()
        .name("AutoSwitch")
        .description("自动切换")
        .defaultValue(AutoSwitchMode.Silent)
        .build()
    );

    private final Setting<Boolean> inventorySwap = sgSwitch.add(new BoolSetting.Builder()
        .name("InventorySwap")
        .description("背包鬼手")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> switchDelay = sgSwitch.add(new IntSetting.Builder()
        .name("SwitchDelay")
        .description("切换CD")
        .defaultValue(0)
        .min(0)
        .build()
    );

    private final Setting<Boolean> antiWeakness = sgSwitch.add(new BoolSetting.Builder()
        .name("AntiWeakness")
        .description("反虚弱")
        .defaultValue(true)
        .build()
    );

    // Place

    private final Setting<Boolean> doPlace = sgPlace.add(new BoolSetting.Builder()
        .name("Place")
        .description("放置")
        .defaultValue(true)
        .build()
    );

    public final Setting<Integer> placeDelay = sgPlace.add(new IntSetting.Builder()
        .name("PlaceDelay")
        .description("放置延迟")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Double> placeRange = sgPlace.add(new DoubleSetting.Builder()
        .name("PlaceRange")
        .description("放置范围")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<Double> placeWallsRange = sgPlace.add(new DoubleSetting.Builder()
        .name("WallsRange")
        .description("穿墙放置范围")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<Boolean> placement112 = sgPlace.add(new BoolSetting.Builder()
        .name("Placement112")
        .description("1.12放置")
        .defaultValue(false)
        .build()
    );

    private final Setting<SupportMode> support = sgPlace.add(new EnumSetting.Builder<SupportMode>()
        .name("Support")
        .description("辅助方块")
        .defaultValue(SupportMode.Fast)
        .build()
    );

    private final Setting<Integer> supportDelay = sgPlace.add(new IntSetting.Builder()
        .name("SupportDelay")
        .description("辅助延迟")
        .defaultValue(1)
        .min(0)
        .visible(() -> support.get() != SupportMode.Disabled)
        .build()
    );

    // Face place

    private final Setting<Boolean> facePlace = sgFacePlace.add(new BoolSetting.Builder()
        .name("FacePlace")
        .description("炸脸")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> facePlaceHealth = sgFacePlace.add(new DoubleSetting.Builder()
        .name("FacePlaceHealth")
        .description("炸脸血量")
        .defaultValue(8)
        .min(1)
        .sliderMin(1)
        .sliderMax(36)
        .visible(facePlace::get)
        .build()
    );

    private final Setting<Double> facePlaceDurability = sgFacePlace.add(new DoubleSetting.Builder()
        .name("FacePlaceDurability")
        .description("耐久百分比阈值")
        .defaultValue(2)
        .min(1)
        .sliderMin(1)
        .sliderMax(100)
        .visible(facePlace::get)
        .build()
    );

    private final Setting<Boolean> facePlaceArmor = sgFacePlace.add(new BoolSetting.Builder()
        .name("FacePlaceMissingArmor")
        .description("洗甲")
        .defaultValue(false)
        .visible(facePlace::get)
        .build()
    );

    private final Setting<Keybind> forceFacePlace = sgFacePlace.add(new KeybindSetting.Builder()
        .name("ForceFacePlace")
        .description("按键炸脸")
        .defaultValue(Keybind.none())
        .build()
    );

    // Break

    private final Setting<Boolean> doBreak = sgBreak.add(new BoolSetting.Builder()
        .name("Break")
        .description("破坏水晶")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> breakDelay = sgBreak.add(new IntSetting.Builder()
        .name("BreakDelay")
        .description("破坏延迟")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> smartDelay = sgBreak.add(new BoolSetting.Builder()
        .name("SmartDelay")
        .description("智能延迟")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> breakRange = sgBreak.add(new DoubleSetting.Builder()
        .name("BreakRange")
        .description("破坏范围")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<Double> breakWallsRange = sgBreak.add(new DoubleSetting.Builder()
        .name("WallsRange")
        .description("穿墙破坏范围")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<Boolean> onlyBreakOwn = sgBreak.add(new BoolSetting.Builder()
        .name("OnlyOwn")
        .description("仅破坏自己放置的水晶")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> breakAttempts = sgBreak.add(new IntSetting.Builder()
        .name("BreakAttempts")
        .description("最大破坏尝试")
        .defaultValue(2)
        .sliderMin(1)
        .sliderMax(5)
        .build()
    );

    private final Setting<Integer> ticksExisted = sgBreak.add(new IntSetting.Builder()
        .name("TicksExisted")
        .description("水晶Age")
        .defaultValue(0)
        .min(0)
        .build()
    );

    private final Setting<Integer> attackFrequency = sgBreak.add(new IntSetting.Builder()
        .name("AttackFrequency")
        .description("每秒最多攻击次数")
        .defaultValue(25)
        .min(1)
        .sliderRange(1, 30)
        .build()
    );

    private final Setting<Boolean> fastBreak = sgBreak.add(new BoolSetting.Builder()
        .name("Ak47")
        .description("AK47")
        .defaultValue(true)
        .build()
    );

    // Pause

    public final Setting<PauseMode> pauseOnUse = sgPause.add(new EnumSetting.Builder<PauseMode>()
        .name("PauseOnUse")
        .description("使用物品时应暂停哪些进程")
        .defaultValue(PauseMode.Both)
        .build()
    );

    public final Setting<PauseMode> pauseOnMine = sgPause.add(new EnumSetting.Builder<PauseMode>()
        .name("PauseOnMine")
        .description("挖掘方块时应暂停哪些进程")
        .defaultValue(PauseMode.None)
        .build()
    );

    private final Setting<Boolean> pauseOnLag = sgPause.add(new BoolSetting.Builder()
        .name("PauseOnLag")
        .description("服务器无响应时是否暂停")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> pauseHealth = sgPause.add(new DoubleSetting.Builder()
        .name("PauseHealth")
        .description("生命值低于指定值时暂停")
        .defaultValue(5)
        .range(0,36)
        .sliderRange(0,36)
        .build()
    );

    // Render

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("渲染")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> renderDmg = sgRender.add(new BoolSetting.Builder()
        .name("RenderDmg")
        .description("渲染伤害")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> dmgColor = sgRender.add(new ColorSetting.Builder()
        .name("DamageColor")
        .description("伤害文本颜色")
        .defaultValue(new SettingColor(140, 142, 255, 255))
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("ShapeMode")
        .description("渲染模式")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("SideColor")
        .description("框内颜色")
        .defaultValue(new SettingColor(140, 142, 255, 50))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("LineColor")
        .description("边框颜色")
        .defaultValue(new SettingColor(140, 142, 255, 255))
        .build()
    );

    private final Setting<Double> renderSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("RenderSpeed")
        .description("渲染速度")
        .defaultValue(0.05)
        .sliderRange(0, 1)
        .build()
    );

    private final Setting<Double> renderH = sgGeneral.add(new DoubleSetting.Builder()
        .name("RenderHeight")
        .description("渲染高度")
        .defaultValue(0.1)
        .sliderRange(0, 1)
        .build()
    );

    // Fields

    private Item mainItem, offItem;

    private int breakTimer, placeTimer, switchTimer, ticksPassed;
    private final List<LivingEntity> targets = new ArrayList<>();

    private Vec3 vec3d = new Vec3(0, 0, 0);
    private Vec3 playerEyePos = new Vec3(0, 0, 0);
    private final Vector3d vec3 = new Vector3d();
    private final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
    private AABB box = new AABB(0, 0, 0, 0, 0, 0);

    private Vec3 vec3dRayTraceEnd = new Vec3(0, 0, 0);
    private ClipContext raycastContext;

    private final IntSet placedCrystals = new IntOpenHashSet();
    private boolean placing;
    private int placingTimer;
    public int kaTimer;
    private final BlockPos.MutableBlockPos placingCrystalBlockPos = new BlockPos.MutableBlockPos();

    private final IntSet removed = new IntOpenHashSet();
    private final Int2IntMap attemptedBreaks = new Int2IntOpenHashMap();
    private final Int2IntMap waitingToExplode = new Int2IntOpenHashMap();
    private int attacks;

    private LivingEntity bestTarget;
    private double bestTargetDamage;
    private int bestTargetTimer;

    private boolean didActionThisTick;

    private static class RenderPos {
        double x, y, z;
    }

    public BlockPos renderCrystalPos;
    private int renderedDamage;
    private final RenderPos renderPos = new RenderPos();

    public CrystalAuraPlus() {
        super(LeavesHack.LEAVES_COMBAT, "CrystalAuraPlus", "自动水晶Plus");
        INSTANCE = this;
    }

    @Override
    public void onActivate() {
        breakTimer = 0;
        placeTimer = 0;
        ticksPassed = 0;

        raycastContext = new ClipContext(new Vec3(0, 0, 0), new Vec3(0, 0, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player);

        placing = false;
        placingTimer = 0;
        kaTimer = 0;

        attacks = 0;

        bestTargetDamage = 0;
        bestTargetTimer = 0;

        renderCrystalPos = null;
        renderPos.x = 0;
        renderPos.y = 0;
        renderPos.z = 0;
    }

    @Override
    public void onDeactivate() {
        targets.clear();

        placedCrystals.clear();

        attemptedBreaks.clear();
        waitingToExplode.clear();

        removed.clear();

        bestTarget = null;
        renderCrystalPos = null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPreTick(TickEvent.Pre event) {
        didActionThisTick = false;

        // Decrement placing timer
        if (placing) {
            if (placingTimer > 0) placingTimer--;
            else placing = false;
        }

        if (kaTimer > 0) kaTimer--;

        if (ticksPassed < 20) ticksPassed++;
        else {
            ticksPassed = 0;
            attacks = 0;
        }

        // Decrement best target timer
        if (bestTargetTimer > 0) bestTargetTimer--;
        bestTargetDamage = 0;

        // Decrement break, place and switch timers
        if (breakTimer > 0) breakTimer--;
        if (placeTimer > 0) placeTimer--;
        if (switchTimer > 0) switchTimer--;

        mainItem = mc.player.getMainHandItem().getItem();
        offItem = mc.player.getOffhandItem().getItem();

        // Update waiting to explode crystals and mark them as existing if reached threshold
        for (IntIterator it = waitingToExplode.keySet().iterator(); it.hasNext();) {
            int id = it.nextInt();
            int ticks = waitingToExplode.get(id);

            if (ticks > 3) {
                it.remove();
                removed.remove(id);
            }
            else {
                waitingToExplode.put(id, ticks + 1);
            }
        }

        // Set player eye pos
        playerEyePos = new Vec3(mc.player.position().x, mc.player.position().y + mc.player.getEyeHeight(mc.player.getPose()), mc.player.position().z);

        // Find targets, break and place
        findTargets();
        if (targets.isEmpty()) renderCrystalPos = null;

        if (!targets.isEmpty()) {
            if (!didActionThisTick) doBreak();
            if (!didActionThisTick) doPlace();
        }
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (!(event.entity instanceof EndCrystal)) return;

        if (placing && event.entity.blockPosition().equals(placingCrystalBlockPos)) {
            placing = false;
            placingTimer = 0;
            placedCrystals.add(event.entity.getId());
        }

        if (fastBreak.get() && !didActionThisTick && attacks < attackFrequency.get()) {
            float damage = getBreakDamage(event.entity, true);
            if (damage > minDamage.get()) doBreak(event.entity);
        }
    }

    @EventHandler
    private void onEntityRemoved(EntityRemovedEvent event) {
        if (event.entity instanceof EndCrystal) {
            placedCrystals.remove(event.entity.getId());
            removed.remove(event.entity.getId());
            waitingToExplode.remove(event.entity.getId());
        }
    }

    // Break

    private void doBreak() {
        if (!doBreak.get() || breakTimer > 0 || switchTimer > 0 || attacks >= attackFrequency.get()) return;
        if (shouldPause(PauseMode.Break)) return;

        float bestDamage = 0;
        Entity crystal = null;

        // Find best crystal to break
        for (Entity entity : mc.level.entitiesForRendering()) {
            float damage = getBreakDamage(entity, true);

            if (damage > bestDamage) {
                bestDamage = damage;
                crystal = entity;
            }
        }

        // Break the crystal
        if (crystal != null) doBreak(crystal);
    }

    private float getBreakDamage(Entity entity, boolean checkCrystalAge) {
        if (!(entity instanceof EndCrystal)) return 0;

        // Check only break own
        if (onlyBreakOwn.get() && !placedCrystals.contains(entity.getId())) return 0;

        // Check if it should already be removed
        if (removed.contains(entity.getId())) return 0;

        // Check attempted breaks
        if (attemptedBreaks.get(entity.getId()) > breakAttempts.get()) return 0;

        // Check crystal age
        if (checkCrystalAge && entity.tickCount < ticksExisted.get()) return 0;

        // Check range
        if (isOutOfRange(entity.position(), entity.blockPosition(), false)) return 0;

        // Check damage to self and anti suicide
        blockPos.set(entity.blockPosition()).move(0, -1, 0);
        float selfDamage = DamageUtils.crystalDamage(mc.player, entity.position(), predictMovement.get(), blockPos);
        if (selfDamage > maxDamage.get() || (antiSuicide.get() && selfDamage >= EntityUtils.getTotalHealth(mc.player))) return 0;

        // Check damage to targets and face place
        float damage = getDamageToTargets(entity.position(), blockPos, true, false);
        boolean shouldFacePlace = shouldFacePlace();
        double minimumDamage = shouldFacePlace ? Math.min(minDamage.get(), 1.5d) : minDamage.get();

        if (damage < minimumDamage) return 0f;

        return damage;
    }

    private void doBreak(Entity crystal) {
        // Anti weakness
        if (antiWeakness.get()) {
            MobEffectInstance weakness = mc.player.getEffect(MobEffects.WEAKNESS);
            MobEffectInstance strength = mc.player.getEffect(MobEffects.STRENGTH);

            // Check for strength
            if (weakness != null && (strength == null || strength.getAmplifier() <= weakness.getAmplifier())) {
                // Check if the item in your hand is already valid
                if (!isValidWeaknessItem(mc.player.getMainHandItem(), crystal)) {
                    // Find valid item to break with
                    int slot = findWeaknessItemSlot(crystal);
                    if (slot == -1) return;
                    if (inventorySwap.get()) InventoryUtil.inventorySwap(InventoryUtil.findItemInventorySlot(mc.player.getInventory().getItem(slot).getItem()), mc.player.getInventory().getSelectedSlot());
                    else InventoryUtil.switchToSlot(slot);

                    switchTimer = 1;
                    return;
                }
            }
        }

        // CombatUtil owns crystal rotation and attack handling.
        CombatUtil.attackCrystal(crystal, rotate.get(), false);
        attacks++;
        didActionThisTick = true;
        breakTimer = breakDelay.get();

        // Update state
        removed.add(crystal.getId());
        attemptedBreaks.put(crystal.getId(), attemptedBreaks.get(crystal.getId()) + 1);
        waitingToExplode.put(crystal.getId(), 0);

    }

    private boolean isValidWeaknessItem(ItemStack itemStack, Entity crystal) {
        return DamageUtils.getAttackDamage(mc.player, crystal, itemStack) > 0;
    }

    private int findWeaknessItemSlot(Entity crystal) {
        for (int slot = 0; slot < 9; slot++) {
            if (isValidWeaknessItem(mc.player.getInventory().getItem(slot), crystal)) return slot;
        }
        return -1;
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof ServerboundSetCarriedItemPacket) {
            switchTimer = switchDelay.get();
        }
    }

    // Place

    private void doPlace() {
        if (!doPlace.get() || placeTimer > 0) return;
        if (shouldPause(PauseMode.Place)) return;

        // Return if there are no crystals in hotbar or offhand
        if (autoSwitch.get() == AutoSwitchMode.None
            && mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL
            && mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) {
            renderCrystalPos = null;
            return;
        }
        int crystalSlot = inventorySwap.get()
            ? InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL)
            : InventoryUtil.findItem(Items.END_CRYSTAL);
        if (autoSwitch.get() != AutoSwitchMode.None && crystalSlot == -1) {
            renderCrystalPos = null;
            return;
        }

        // Check for multiplace
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (getBreakDamage(entity, false) > 0) return;
        }

        // Setup variables
        AtomicDouble bestDamage = new AtomicDouble(0);
        AtomicReference<BlockPos.MutableBlockPos> bestBlockPos = new AtomicReference<>(new BlockPos.MutableBlockPos());
        AtomicBoolean isSupport = new AtomicBoolean(support.get() != SupportMode.Disabled);

        // Find best position to place the crystal on
        BlockIterator.register((int) Math.ceil(placeRange.get()), (int) Math.ceil(placeRange.get()), (bp, blockState) -> {
            // Check if its bedrock or obsidian and return if isSupport is false
            boolean hasBlock = blockState.is(Blocks.BEDROCK) || blockState.is(Blocks.OBSIDIAN);
            if (!hasBlock) {
                if (!isSupport.get() || !blockState.canBeReplaced()) return;
                // A replaceable position is only a valid support candidate if it has a usable placement side.
                if (BlockUtil.getPlaceSide(bp, null) == null) return;
            }

            // Check if there is air on top
            blockPos.set(bp.getX(), bp.getY() + 1, bp.getZ());
            if (!mc.level.getBlockState(blockPos).isAir()) return;

            if (placement112.get()) {
                blockPos.move(0, 1, 0);
                if (!mc.level.getBlockState(blockPos).isAir()) return;
            }

            // Check range
            vec3d = new Vec3(bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5);
            blockPos.set(bp).move(0, 1, 0);
            if (isOutOfRange(vec3d, blockPos, true)) return;

            // Check damage to self and anti suicide
            float selfDamage = DamageUtils.crystalDamage(mc.player, vec3d, predictMovement.get(), bp);
            if (selfDamage > maxDamage.get() || (antiSuicide.get() && selfDamage >= EntityUtils.getTotalHealth(mc.player))) return;

            // Check damage to targets and face place
            float damage = getDamageToTargets(vec3d, bp, false, !hasBlock && support.get() == SupportMode.Fast);

            boolean shouldFacePlace = shouldFacePlace();
            double minimumDamage = Math.min(minDamage.get(), shouldFacePlace ? 1.5 : minDamage.get());

            if (damage < minimumDamage) return;

            // Check if it can be placed
            double x = bp.getX();
            double y = bp.getY() + 1;
            double z = bp.getZ();
            box = new AABB(x, y, z, x + 1, y + (placement112.get() ? 1 : 2), z + 1);

            if (intersectsWithEntities(box)) return;

            // Compare damage
            if (damage > bestDamage.get() || (isSupport.get() && hasBlock)) {
                bestDamage.set(damage);
                bestBlockPos.get().set(bp);
            }

            if (hasBlock) isSupport.set(false);
        });

        // Place the crystal
        BlockIterator.after(() -> {
            if (bestDamage.get() == 0) {
                renderCrystalPos = null;
                return;
            }

            renderCrystalPos = bestBlockPos.get().above().immutable();
            renderedDamage = (int) bestDamage.get();

            BlockHitResult result = getPlaceInfo(bestBlockPos.get());

            vec3d = new Vec3(
                result.getBlockPos().getX() + 0.5 + result.getDirection().getUnitVec3i().getX() * 1.0 / 2.0,
                result.getBlockPos().getY() + 0.5 + result.getDirection().getUnitVec3i().getY() * 1.0 / 2.0,
                result.getBlockPos().getZ() + 0.5 + result.getDirection().getUnitVec3i().getZ() * 1.0 / 2.0
            );

            if (isSupport.get()) {
                if (!placeSupportBlock(bestBlockPos.get())) return;
                if (supportDelay.get() == 0) placeCrystal(result);
            }
            else {
                placeCrystal(result);
            }

            didActionThisTick = true;
            placeTimer += placeDelay.get();
        });
    }

    private BlockHitResult getPlaceInfo(BlockPos blockPos) {
        vec3d = new Vec3(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());

        for (Direction side : Direction.values()) {
            vec3dRayTraceEnd = new Vec3(
                blockPos.getX() + 0.5 + side.getUnitVec3i().getX() * 0.5,
                blockPos.getY() + 0.5 + side.getUnitVec3i().getY() * 0.5,
                blockPos.getZ() + 0.5 + side.getUnitVec3i().getZ() * 0.5
            );

            raycastContext = new ClipContext(vec3d, vec3dRayTraceEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player);
            BlockHitResult result = mc.level.clip(raycastContext);

            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(blockPos)) {
                return result;
            }
        }

        Direction side = blockPos.getY() > vec3d.y ? Direction.DOWN : Direction.UP;
        return new BlockHitResult(vec3d, side, blockPos, false);
    }

    private boolean placeSupportBlock(BlockPos supportBlock) {
        int itemSlot = inventorySwap.get()
            ? InventoryUtil.findItemInventorySlot(Items.OBSIDIAN)
            : InventoryUtil.findItem(Items.OBSIDIAN);
        if (autoSwitch.get() == AutoSwitchMode.None) {
            if (mc.player.getMainHandItem().getItem() != Items.OBSIDIAN) return false;
        }
        else if (itemSlot == -1) return false;

        Direction side = BlockUtil.getPlaceSide(supportBlock, null);
        if (side == null) return false;

        int prevSlot = mc.player.getInventory().getSelectedSlot();
        if (autoSwitch.get() != AutoSwitchMode.None) doSwap(itemSlot);

        BlockUtil.placeBlock(supportBlock, side, rotate.get());
        placeTimer += supportDelay.get();

        switchBack(itemSlot, prevSlot);
        return true;
    }

    private void placeCrystal(BlockHitResult result) {
        int itemSlot = inventorySwap.get()
            ? InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL)
            : InventoryUtil.findItem(Items.END_CRYSTAL);
        if (autoSwitch.get() == AutoSwitchMode.None) {
            if (mc.player.getMainHandItem().getItem() != Items.END_CRYSTAL
                && mc.player.getOffhandItem().getItem() != Items.END_CRYSTAL) return;
        }
        else if (itemSlot == -1) return;

        int prevSlot = mc.player.getInventory().getSelectedSlot();
        if (autoSwitch.get() != AutoSwitchMode.None) doSwap(itemSlot);

        Direction side = result.getDirection();
        InteractionHand hand = mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL && autoSwitch.get() == AutoSwitchMode.None
            ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        BlockUtil.clickBlock(result.getBlockPos(), side, rotate.get(), hand);

        placing = true;
        placingTimer = 4;
        kaTimer = 8;
        placingCrystalBlockPos.set(result.getBlockPos()).move(0, 1, 0);

        switchBack(itemSlot, prevSlot);
    }

    private void switchBack(int itemSlot, int prevSlot) {
        if (autoSwitch.get() == AutoSwitchMode.Silent || inventorySwap.get()) {
            if (inventorySwap.get()) InventoryUtil.inventorySwap(itemSlot, prevSlot);
            else InventoryUtil.switchToSlot(prevSlot);
        }
    }

    private void doSwap(int slot) {
        if (inventorySwap.get()) InventoryUtil.inventorySwap(slot, mc.player.getInventory().getSelectedSlot());
        else InventoryUtil.switchToSlot(slot);
    }

    // Face place

    private boolean shouldFacePlace() {
        if (!facePlace.get()) return false;

        if (forceFacePlace.get().isPressed()) return true;

        // Checks if the provided crystal position should face place to any target
        for (LivingEntity target : targets) {
            if (EntityUtils.getTotalHealth(target) <= facePlaceHealth.get()) return true;

            for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
                ItemStack itemStack = target.getItemBySlot(slot);

                if (itemStack == null || itemStack.isEmpty()) {
                    if (facePlaceArmor.get()) return true;
                }
                else {
                    if ((double) (itemStack.getMaxDamage() - itemStack.getDamageValue()) / itemStack.getMaxDamage() * 100 <= facePlaceDurability.get()) return true;
                }
            }
        }

        return false;
    }

    // Others

    private boolean shouldPause(PauseMode process) {
        if (preferMode.get() == PreferMode.PreferAnchor) {
            return AutoAnchor.INSTANCE.currentPos != null;
        }
        if (mc.player.isUsingItem() || mc.options.keyUse.isDown()) {
            if (pauseOnUse.get().equals(process)) return true;
        }
        if (pauseOnLag.get() && TickRate.INSTANCE.getTimeSinceLastTick() >= 1.0f) return true;
        if (pauseOnMine.get().equals(process) && mc.gameMode.isDestroying()) return true;
        return (EntityUtils.getTotalHealth(mc.player) <= pauseHealth.get());
    }

    private boolean isOutOfRange(Vec3 vec3d, BlockPos blockPos, boolean place) {
        raycastContext = new ClipContext(playerEyePos, vec3d, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player);

        BlockHitResult result = mc.level.clip(raycastContext);

        if (result == null || !result.getBlockPos().equals(blockPos)) // Is behind wall
            return !PlayerUtils.isWithin(vec3d, (place ? placeWallsRange : breakWallsRange).get());
        return !PlayerUtils.isWithin(vec3d, (place ? placeRange : breakRange).get());
    }

    private LivingEntity getNearestTarget() {
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity target : targets) {
            double distance = mc.player.distanceToSqr(target);

            if (distance < nearestDistance) {
                nearestTarget = target;
                nearestDistance = distance;
            }
        }

        return nearestTarget;
    }

    private float getDamageToTargets(Vec3 vec3d, BlockPos obsidianPos, boolean breaking, boolean fast) {
        float damage = 0;

        if (fast) {
            LivingEntity target = getNearestTarget();
            if (!(smartDelay.get() && breaking && target.hurtTime > 0)) damage = DamageUtils.crystalDamage(target, vec3d, predictMovement.get(), obsidianPos);
        }
        else {
            for (LivingEntity target : targets) {
                if (smartDelay.get() && breaking && target.hurtTime > 0) continue;

                float dmg = DamageUtils.crystalDamage(target, vec3d, predictMovement.get(), obsidianPos);

                // Update best target
                if (dmg > bestTargetDamage) {
                    bestTarget = target;
                    bestTargetDamage = dmg;
                    bestTargetTimer = 10;
                }

                damage += dmg;
            }
        }

        return damage;
    }

    @Override
    public String getInfoString() {
        if (mc.player == null || mc.level == null) return null;
        return bestTarget != null && bestTargetTimer > 0 ? ("[" + EntityUtils.getName(bestTarget) + "]") : null;
    }

    private void findTargets() {
        targets.clear();

        // Living Entities
        for (Entity entity : mc.level.entitiesForRendering()) {
            // Ignore non-living
            if (!(entity instanceof LivingEntity livingEntity)) continue;

            // Player
            if (livingEntity instanceof Player player) {
                if (player.isCreative() || livingEntity == mc.player) continue;
                if (!player.isAlive() || !Friends.get().shouldAttack(player)) continue;

            }

            // Animals, water animals, monsters, bats, misc
            if (!(entities.get().contains(livingEntity.getType()))) continue;

            // Close enough to damage
            if (livingEntity.distanceToSqr(mc.player) > targetRange.get() * targetRange.get()) continue;

            targets.add(livingEntity);
        }
    }

    private boolean intersectsWithEntities(AABB box) {
        return EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator() && !removed.contains(entity.getId()));
    }

    // Rendering

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (renderCrystalPos == null || !renderDmg.get()) return;

        Vector3d pos = new Vector3d(
            renderPos.x + 0.5,
            renderPos.y + (1 - renderH.get() / 2),
            renderPos.z + 0.5
        );
        if (NametagUtils.to2D(pos, 1.3, true)) {
            NametagUtils.begin(pos);
            TextRenderer textRenderer = TextRenderer.get();
            textRenderer.begin(NametagUtils.scale, false, true);
            String text = renderedDamage + "";
            double width = textRenderer.getWidth(text, true);
            double textHeight = textRenderer.getHeight(true);
            textRenderer.render(text, -width / 2, -textHeight / 2, dmgColor.get(), true);
            textRenderer.end();
            NametagUtils.end();
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!render.get()) return;
        if (renderCrystalPos == null) {
            renderPos.x = 0;
            renderPos.y = 0;
            renderPos.z = 0;
            return;
        }
        if (renderPos.x == 0 && renderPos.y == 0 && renderPos.z == 0) {
            renderPos.x = mc.player.getX();
            renderPos.y = mc.player.getY();
            renderPos.z = mc.player.getZ();
        }

        renderPos.x += (renderCrystalPos.getX() - renderPos.x) * renderSpeed.get();
        renderPos.y += (renderCrystalPos.getY() - 1 - renderPos.y) * renderSpeed.get();
        renderPos.z += (renderCrystalPos.getZ() - renderPos.z) * renderSpeed.get();

        AABB box = new AABB(
            renderPos.x,
            renderPos.y + (1 - renderH.get()),
            renderPos.z,
            renderPos.x + 1,
            renderPos.y + 1,
            renderPos.z + 1
        );
        event.renderer.box(box, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }

    public enum AutoSwitchMode {
        Normal,
        Silent,
        None
    }

    public enum SupportMode {
        Disabled,
        Accurate,
        Fast
    }

    public enum PauseMode {
        Both,
        Place,
        Break,
        None;

        public boolean equals(PauseMode process) {
            return this == process || this == PauseMode.Both;
        }
    }
    public enum PreferMode {
        PreferCrystal,
        PreferAnchor
    }
}
