package de.crazymemecoke.features.modules.player;

import de.crazymemecoke.manager.modulemanager.Category;
import de.crazymemecoke.manager.modulemanager.Module;
import org.lwjgl.input.Keyboard;

public class AutoWalk extends Module {
    public AutoWalk() {
        super("AutoWalk", Keyboard.KEY_NONE, Category.PLAYER, -1);
    }

    @Override
    public void onDisable() {
        mc.gameSettings.keyBindForward.pressed = false;
    }

    @Override
    public void onUpdate() {
        if (state()) {
            mc.gameSettings.keyBindForward.pressed = true;
        }
    }
}