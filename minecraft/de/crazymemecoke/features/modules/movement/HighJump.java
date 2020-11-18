package de.crazymemecoke.features.modules.movement;

import de.crazymemecoke.Client;
import de.crazymemecoke.manager.settingsmanager.Setting;
import de.crazymemecoke.manager.eventmanager.Event;
import de.crazymemecoke.manager.eventmanager.impl.EventUpdate;
import de.crazymemecoke.manager.modulemanager.Category;
import de.crazymemecoke.manager.modulemanager.Module;
import org.lwjgl.input.Keyboard;

public class HighJump extends Module {
    public HighJump() {
        super("HighJump", Keyboard.KEY_NONE, Category.MOVEMENT);

        Client.main().setMgr().addSetting(new Setting("Boost", this, 0.5, 0.1, 5.0, false));
    }

    @Override
    public void onEvent(Event event) {
        if(event instanceof EventUpdate) {
            double boost = Client.main().setMgr().settingByName("Boost", this).getNum();
            if (mc.gameSettings.keyBindJump.pressed && mc.gameSettings.keyBindForward.pressed) {
                mc.thePlayer.motionY = boost;
            }
        }
    }
}
