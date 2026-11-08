package com.dev.leavesHack.modules;

import com.dev.leavesHack.LeavesHack;
import com.dev.leavesHack.utils.combat.CombatUtil;
import com.dev.leavesHack.utils.entity.InventoryUtil;
import com.dev.leavesHack.utils.math.Timer;
import com.dev.leavesHack.utils.rotation.Rotation;
import com.dev.leavesHack.utils.world.BlockUtil;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class AutoWeb extends Module {
    public static AutoWeb INSTANCE;

    public AutoWeb() {
        super(LeavesHack.LEAVES_COMBAT, "AutoWeb", "自动蛛网");
        INSTANCE = this;
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General Settings
    private final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("TargetRange")
        .description("目标距离")
        .defaultValue(4.0)
        .sliderRange(1, 8)
        .build()
    );

    private final Setting<Double> placeRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("PlaceRange")
        .description("放置距离")
        .defaultValue(4.5)
        .sliderRange(1, 6)
        .build()
    );

    private final Setting<Integer> placeDelay = sgGeneral.add(new IntSetting.Builder()
        .name("PlaceDelay")
        .description("放置延迟")
        .defaultValue(50)
        .sliderRange(0, 500)
        .build()
    );

    private final Setting<Boolean> inventory = sgGeneral.add(new BoolSetting.Builder()
        .name("InventorySwap")
        .description("背包鬼手")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("Rotate")
        .description("转头")
        .defaultValue(true)
        .build()
    );

    private final Setting<WebPlaceMode> placeMode = sgGeneral.add(new EnumSetting.Builder<WebPlaceMode>()
        .name("PlaceMode")
        .description("放置位置")
        .defaultValue(WebPlaceMode.Feet)
        .build()
    );

    private final Setting<Boolean> predict = sgGeneral.add(new BoolSetting.Builder()
        .name("Predict")
        .description("预判位置")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> detectMining = sgGeneral.add(new BoolSetting.Builder()
        .name("DetectMining")
        .description("检测挖掘")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> predictTicks = sgGeneral.add(new IntSetting.Builder()
        .name("PredictTicks")
        .description("预判tick数")
        .defaultValue(2)
        .sliderRange(1, 10)
        .visible(predict::get)
        .build()
    );

    private final Setting<Boolean> skipWebbed = sgGeneral.add(new BoolSetting.Builder()
        .name("SkipWebbed")
        .description("跳过已在蛛网中的目标")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> usingPause = sgGeneral.add(new BoolSetting.Builder()
        .name("UsingPause")
        .description("使用物品时暂停")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> onlyMain = sgGeneral.add(new BoolSetting.Builder()
        .name("OnlyMain")
        .description("仅检查主手")
        .defaultValue(true)
        .visible(usingPause::get)
        .build()
    );
    // Render Settings
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("渲染放置位置")
        .defaultValue(true)
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
        .description("填充颜色")
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("LineColor")
        .description("边框颜色")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );

    private final Setting<Double> renderSpeed = sgRender.add(new DoubleSetting.Builder()
        .name("RenderSpeed")
        .description("渲染插值速度")
        .defaultValue(0.15)
        .sliderRange(0.01, 0.5)
        .build()
    );

    // State
    private final Timer placeTimer = new Timer();
    private Player target;
    public BlockPos webPos;

    private static class RenderPos {
        double x, y, z;
    }

    private final RenderPos renderPos = new RenderPos();

    @Override
    public void onActivate() {
        placeTimer.setMs(9999999);
        renderPos.x = 0;
        renderPos.y = 0;
        renderPos.z = 0;
        webPos = null;
    }

    @Override
    public void onDeactivate() {
        webPos = null;
    }

    @Override
    public String getInfoString() {
        return target == null ? null : "[" + target.getName().getString() + "]";
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null) return;
        target = CombatUtil.getClosestEnemy(targetRange.get());
        if (target == null) {
            webPos = null;
            return;
        }
        if (usingPause.get() && checkPause(onlyMain.get())) return;
        int webSlot = inventory.get()
            ? InventoryUtil.findItemInventorySlot(Items.COBWEB)
            : InventoryUtil.findItem(Items.COBWEB);
        if (webSlot == -1) {
            webPos = null;
            return;
        }
        webPos = getWebPos(target);
        if (skipWebbed.get() && isWebbed(target, webPos) ) {
            return;
        }
        if (!PlayerUtils.isWithin(webPos.getCenter(), placeRange.get())) {
            webPos = null;
            return;
        }
        if (placeTimer.passedMs(placeDelay.get())) {
            placeWeb(webPos, webSlot);
            placeTimer.reset();
        }
    }

    private BlockPos getWebPos(Player target) {
        Vec3 targetPos;

        if (predict.get() && predictTicks.get() > 0) {
            int ticks = predictTicks.get();
            double dx = target.getX() - target.xo;
            double dy = target.getY() - target.yo;
            double dz = target.getZ() - target.zo;
            targetPos = new Vec3(
                target.getX() + dx * ticks,
                target.getY() + dy * ticks,
                target.getZ() + dz * ticks
            );
        } else {
            targetPos = target.position();
        }

        return switch (placeMode.get()) {
            case Feet -> new BlockPos((int) Math.floor(targetPos.x), (int) Math.floor(targetPos.y), (int) Math.floor(targetPos.z));
            case Head -> new BlockPos((int) Math.floor(targetPos.x), (int) Math.floor(targetPos.y + target.getEyeHeight(target.getPose())), (int) Math.floor(targetPos.z));
            case Body -> new BlockPos((int) Math.floor(targetPos.x), (int) Math.floor(targetPos.y + 0.5), (int) Math.floor(targetPos.z));
        };
    }

    private boolean isWebbed(Player target, BlockPos pos) {
        return mc.level.getBlockState(pos).getBlock() == Blocks.COBWEB;
    }

    private void placeWeb(BlockPos pos, int slot) {
        Direction side = BlockUtil.getPlaceSide(pos, null);
        if (side == null || !mc.level.isEmptyBlock(pos)) return;
        int old = mc.player.getInventory().getSelectedSlot();
        doSwap(slot);
        if (rotate.get()) {
            Rotation.snapAt(pos.getCenter());
        }
        BlockUtil.placeBlock(pos, side, rotate.get());
        if (rotate.get()) {
            Rotation.snapBack();
        }
        if (inventory.get()) {
            doSwap(slot);
        } else {
            doSwap(old);
        }
    }

    private void doSwap(int slot) {
        if (!inventory.get()) {
            InventoryUtil.switchToSlot(slot);
        } else {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().getSelectedSlot());
        }
    }
    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!render.get() || webPos == null) {
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

        renderPos.x += (webPos.getX() - renderPos.x) * renderSpeed.get();
        renderPos.y += (webPos.getY() - renderPos.y) * renderSpeed.get();
        renderPos.z += (webPos.getZ() - renderPos.z) * renderSpeed.get();

        AABB box = new AABB(
            renderPos.x,
            renderPos.y,
            renderPos.z,
            renderPos.x + 1,
            renderPos.y + 1,
            renderPos.z + 1
        );

        event.renderer.box(
            box,
            sideColor.get(),
            lineColor.get(),
            shapeMode.get(),
            0
        );
    }

    public enum WebPlaceMode {
        Feet,
        Head,
        Body
    }
    public boolean checkPause(boolean onlyMain) {
        return mc.options.keyUse.isDown() && (!onlyMain || mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND);
    }
}
