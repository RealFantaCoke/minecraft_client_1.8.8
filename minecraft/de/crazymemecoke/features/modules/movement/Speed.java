package de.crazymemecoke.features.modules.movement;

import de.crazymemecoke.Client;
import de.crazymemecoke.manager.clickguimanager.settings.Setting;
import de.crazymemecoke.manager.clickguimanager.settings.SettingsManager;
import de.crazymemecoke.manager.modulemanager.Category;
import de.crazymemecoke.manager.modulemanager.Module;
import de.crazymemecoke.utils.entity.EntityUtils;
import de.crazymemecoke.utils.entity.PlayerUtil;
import de.crazymemecoke.utils.events.MoveEvent;
import de.crazymemecoke.utils.events.UpdateEvent;
import de.crazymemecoke.utils.events.eventapi.EventTarget;
import de.crazymemecoke.utils.time.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class Speed extends Module {
    public static boolean canStep;
    private final TimerUtil framesDelay = new TimerUtil();
    private final TimerUtil delayTimer = new TimerUtil();
    public double speed;
    public int stage;
    public int tickse;
    public double moveSpeed;
    ArrayList<String> mode = new ArrayList<>();
    String speedMode;
    boolean move;
    boolean hop;
    SettingsManager sM = Client.instance().setMgr();
    private double prevY;
    private int motionTicks;
    private int ticks = 0;
    private float prevYaw;

    public Speed() {
        super("Speed", Keyboard.KEY_NONE, Category.MOVEMENT, -1);
        mode.add("Ground");
        mode.add("AAC 1.9.10");
        mode.add("Frames");
        mode.add("Motion");
        mode.add("Jump");
        mode.add("Timer");

        sM.newSetting(new Setting("Mode", this, "Jump", mode));
        sM.newSetting(new Setting("Frames Speed", this, 4.25, 0, 50, true));
        sM.newSetting(new Setting("Timer Speed", this, 4.25, 0, 50, true));
    }

    public static float getDistanceBetweenAngles(float angle1, float angle2) {
        float angle = Math.abs(angle1 - angle2) % 360.0F;
        if (angle > 180.0F) {
            angle = 360.0F - angle;
        }
        return angle;
    }

    public void onEnable() {
        framesDelay.setLastMS();
        delayTimer.setLastMS();
        motionTicks = 0;
        move = false;
        hop = false;
        prevY = 0.0D;
        ticks = 0;
    }

    public void onDisable() {
        mc.timer.timerSpeed = 1.0F;
        mc.thePlayer.motionX = 0.0D;
        mc.thePlayer.motionZ = 0.0D;
        mc.thePlayer.setVelocity(0.0D, 0.0D, 0.0D);
    }

    public void onUpdate() {
        speedMode = sM.getSettingByName("Mode", this).getMode();
        double frames_speed = sM.getSettingByName("Frames Speed", this).getNum();
        if (getState()) {

            switch (speedMode) {
                case "ground": {
                    doGround();
                    break;
                }
                case "aac 1.9.10": {
                    doAAC1910();
                }
                case "frames": {
                    doFrames(frames_speed);
                    break;
                }
                case "motion": {
                    doMotion();
                    break;
                }
                case "jump": {
                    doJump();
                    break;
                }
                case "timer": {
                    doTimer();
                    break;
                }
            }
        }
    }

    private void doTimer() {
        double timer_speed = sM.getSettingByName("Timer Speed", this).getNum();
        mc.timer.timerSpeed = (float) timer_speed;
    }

    private void doJump() {
        if (!(EntityUtils.isMoving())) {
            return;
        }
        if (mc.thePlayer.moveForward > 0.0F) {
            float var1 = mc.thePlayer.rotationYaw * 0.017453292F;
            mc.thePlayer.motionX -= MathHelper.sin(var1) * 0.2D;
            mc.thePlayer.motionZ += MathHelper.cos(var1) * 0.2D;
        }
    }

    private void doMotion() {
        double speed = 3.15D;
        double slow = 1.49D;
        double offset = 4.9D;
        if ((mc.gameSettings.keyBindForward.isPressed())
                || (mc.gameSettings.keyBindBack.isPressed())
                || (mc.gameSettings.keyBindLeft.isPressed())
                || (mc.gameSettings.keyBindRight.isPressed())) {
            boolean shouldOffset = true;
            if ((mc.thePlayer.onGround) && (prevYaw < 1.0F)) {
                prevYaw += 0.2F;
            }
            if (!mc.thePlayer.onGround) {
                prevYaw = 0.0F;
            }
            if ((prevYaw == 1.0F) && (shouldSpeedUp())) {
                if (!mc.thePlayer.isSprinting()) {
                    offset += 0.8D;
                }
                if (mc.thePlayer.moveStrafing != 0.0F) {
                    speed -= 0.1D;
                    offset += 0.5D;
                }
                if (mc.thePlayer.isInWater()) {
                    speed -= 0.1D;
                }
                double ncT = 1.15D;
                ticks += 1;
                if (ticks == 1) {
                    mc.thePlayer.motionX *= speed;
                    mc.thePlayer.motionZ *= speed;
                } else if (ticks == 2) {
                    mc.timer.timerSpeed = 1.0F;
                    mc.thePlayer.motionX /= slow;
                    mc.thePlayer.motionZ /= slow;
                } else if (ticks == 4) {
                    mc.timer.timerSpeed = 1.0F;
                    mc.thePlayer.setPosition(
                            mc.thePlayer.posX + mc.thePlayer.motionX / offset,
                            mc.thePlayer.posY,
                            mc.thePlayer.posZ + mc.thePlayer.motionZ / offset);
                    ticks = 0;
                }
            }
        }
    }

    private void doFrames(double speed) {
        if ((mc.thePlayer.movementInput.moveForward > 0.0F)
                || (mc.thePlayer.movementInput.moveStrafe > 0.0F)) {
            if (mc.thePlayer.onGround) {
                prevY = mc.thePlayer.posY;
                hop = true;
                mc.thePlayer.jump();
                if (motionTicks == 1) {
                    framesDelay.reset();
                    if (move) {
                        mc.thePlayer.motionX /= speed * 2.0D;
                        mc.thePlayer.motionZ /= speed * 2.0D;
                        move = false;
                    }
                    motionTicks = 0;
                } else {
                    motionTicks = 1;
                }
            } else if ((!move) && (motionTicks == 1) && (framesDelay.isDelayComplete(450L))) {
                mc.thePlayer.motionX *= speed;
                mc.thePlayer.motionZ *= speed;
                move = true;
            }
        }
    }

    private void doAAC1910() {
        if ((hop) && (mc.thePlayer.posY >= prevY + 0.399994D)) {
            mc.thePlayer.motionY = -0.9D;
            mc.thePlayer.posY = prevY;
            hop = false;
        }
        if ((mc.thePlayer.moveForward != 0.0F) && (!mc.thePlayer.isCollidedHorizontally)
                && (!mc.thePlayer.isEating())) {
            if ((mc.thePlayer.moveForward == 0.0F) && (mc.thePlayer.moveStrafing == 0.0F)) {
                mc.thePlayer.motionX = 0.0D;
                mc.thePlayer.motionZ = 0.0D;
                if (mc.thePlayer.isCollidedVertically) {
                    mc.thePlayer.jump();
                    move = true;
                }
                if ((move) && (mc.thePlayer.isCollidedVertically)) {
                    move = false;
                }
            }
            if (mc.thePlayer.isCollidedVertically) {
                mc.thePlayer.motionX *= 1.2D;
                mc.thePlayer.motionZ *= 1.2D;
                doMiniHop();
            }
            if ((hop) && (!move) && (mc.thePlayer.posY >= prevY + 0.399994D)) {
                mc.thePlayer.motionY = -100.0D;
                mc.thePlayer.posY = prevY;
                hop = false;
            }
        }
    }

    private void doMiniHop() {
        hop = true;
        prevY = mc.thePlayer.posY;
        mc.thePlayer.jump();
    }

    private void doGround() {
        if ((hop) && (mc.thePlayer.posY >= prevY + 0.399994D)) {
            mc.thePlayer.motionY = -0.9D;
            mc.thePlayer.posY = prevY;
            hop = false;
        }
        if ((mc.thePlayer.moveForward != 0.0F) && (!mc.thePlayer.isCollidedHorizontally)
                && (!mc.thePlayer.isEating())) {
            if ((mc.thePlayer.moveForward == 0.0F) && (mc.thePlayer.moveStrafing == 0.0F)) {
                mc.thePlayer.motionX = 0.0D;
                mc.thePlayer.motionZ = 0.0D;
                if (mc.thePlayer.isCollidedVertically) {
                    mc.thePlayer.jump();
                    move = true;
                }
                if ((move) && (mc.thePlayer.isCollidedVertically)) {
                    move = false;
                }
            }
            if (mc.thePlayer.isCollidedVertically) {
                mc.thePlayer.motionX *= 1.0379D;
                mc.thePlayer.motionZ *= 1.0379D;
                doMiniHop();
            }
            if ((hop) && (!move) && (mc.thePlayer.posY >= prevY + 0.399994D)) {
                mc.thePlayer.motionY = -100.0D;
                mc.thePlayer.posY = prevY;
                hop = false;
            }
        }
    }

    public boolean shouldSpeedUp() {
        boolean b = (mc.thePlayer.movementInput.moveForward != 0.0F)
                || (mc.thePlayer.movementInput.moveStrafe != 0.0F);
        return (!mc.thePlayer.isInWater()) && (!PlayerUtil.isOnLiquid())
                && (!mc.thePlayer.isCollidedHorizontally) && (!mc.thePlayer.isSneaking())
                && (mc.thePlayer.onGround) && (b);
    }

    public boolean defaultChecks() {
        return !mc.thePlayer.isSneaking() && !mc.thePlayer.isCollidedHorizontally && (mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F) && !mc.gameSettings.keyBindJump.isPressed() && !PlayerUtil.isOnLiquid() && !PlayerUtil.isInLiquid();
    }

    public Block getBlock(AxisAlignedBB bb) {
        int y = (int) bb.minY;
        for (int x = MathHelper.floor_double(bb.minX); x < MathHelper.floor_double(bb.maxX) + 1; x++) {
            for (int z = MathHelper.floor_double(bb.minZ); z < MathHelper.floor_double(bb.maxZ) + 1; z++) {
                Block block = mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
                if (block != null) {
                    return block;
                }
            }
        }
        return null;
    }

    @EventTarget
    public void onNewMove(MoveEvent e) {
        if (!speedMode.equalsIgnoreCase("Jump")) {
            return;
        }
        e.setX(e.getX() * 0.4D);
        e.setZ(e.getZ() * 0.4D);
    }

    public boolean checks() {
        return !mc.thePlayer.isSneaking() && !mc.thePlayer.isCollidedHorizontally && (mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F) && !mc.gameSettings.keyBindJump.isPressed() && !PlayerUtil.isOnLiquid() && !PlayerUtil.isInLiquid();
    }

    @EventTarget
    public void onJumpUpdate(UpdateEvent e) {
        if (!speedMode.equalsIgnoreCase("Jump")) {
            return;
        }
        if (e.getState() == WorldRenderer.State.PRE) {
            if (((mc.thePlayer.moveForward == 0.0F) && (mc.thePlayer.moveStrafing == 0.0F))
                    || (mc.thePlayer.isCollidedHorizontally)) {
                return;
            }
            if (mc.thePlayer.isCollidedVertically) {
                mc.thePlayer.motionY = 0.145D;
                mc.thePlayer.isAirBorne = true;
            } else if (mc.thePlayer.isSprinting()) {
                float var1 = mc.thePlayer.rotationYaw * 0.017453292F;
                mc.thePlayer.motionX -= MathHelper.sin(var1) * 0.2D;
                mc.thePlayer.motionZ += MathHelper.cos(var1) * 0.2D;
            }
        }
    }

    @EventTarget
    public void onNewUpdate(UpdateEvent e) {
        if (!speedMode.equalsIgnoreCase("New")) {
            return;
        }
        if (e.getState() == WorldRenderer.State.PRE) {
            double speed = 3.15D;
            double slow = 1.49D;
            double offset = 4.9D;
            if ((mc.gameSettings.keyBindForward.isPressed())
                    || (mc.gameSettings.keyBindBack.isPressed())
                    || (mc.gameSettings.keyBindLeft.isPressed())
                    || (mc.gameSettings.keyBindRight.isPressed())) {
                boolean shouldOffset = true;
                if ((mc.thePlayer.onGround) && (prevYaw < 1.0F)) {
                    prevYaw += 0.2F;
                }
                if (!mc.thePlayer.onGround) {
                    prevYaw = 0.0F;
                }
                if ((prevYaw == 1.0F) && (shouldSpeedUp())) {
                    if (!mc.thePlayer.isSprinting()) {
                        offset += 0.8D;
                    }
                    if (mc.thePlayer.moveStrafing != 0.0F) {
                        speed -= 0.1D;
                        offset += 0.5D;
                    }
                    if (mc.thePlayer.isInWater()) {
                        speed -= 0.1D;
                    }
                    double ncT = 1.15D;
                    ticks += 1;
                    if (ticks == 1) {
                        mc.thePlayer.motionX *= speed;
                        mc.thePlayer.motionZ *= speed;
                    } else if (ticks == 2) {
                        mc.timer.timerSpeed = 1.0F;
                        mc.thePlayer.motionX /= slow;
                        mc.thePlayer.motionZ /= slow;
                    } else if (ticks == 4) {
                        mc.timer.timerSpeed = 1.0F;
                        mc.thePlayer.setPosition(mc.thePlayer.posX + mc.thePlayer.motionX / offset,
                                mc.thePlayer.posY,
                                mc.thePlayer.posZ + mc.thePlayer.motionZ / offset);
                        ticks = 0;
                    }
                }
            }
        }
    }

    public double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
