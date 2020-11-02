package de.crazymemecoke.features.commands;

import de.crazymemecoke.manager.notificationmanager.NotificationType;
import org.lwjgl.input.Keyboard;

import de.crazymemecoke.Client;
import de.crazymemecoke.manager.commandmanager.Command;
import de.crazymemecoke.manager.modulemanager.Module;
import de.crazymemecoke.manager.modulemanager.ModuleManager;
import de.crazymemecoke.utils.NotifyUtil;
import de.crazymemecoke.utils.Wrapper;

public class Bind extends Command {

    String syntax = ".bind <module> <key> | .bind <module> none | .bind list | .bind clear";

    @Override
    public void execute(String[] args) {
        ModuleManager moduleManager = Client.main().modMgr();
        if (args.length == 2) {
            if (Client.main().modMgr().getByName(args[0]) != null) {
                Module mod = moduleManager.getByName(args[0]);
                if (mod == null) {
                    NotifyUtil.notification("Modul nicht gefunden!", "Modul §c" + args[0] + "§r wurde nicht gefunden!", NotificationType.ERROR, 5);
                    return;
                }

                if(args[1].equalsIgnoreCase("none")){
                    mod.setBind(0);
                    NotifyUtil.notification("Modul entbunden!", "§c" + args[0] + "§r wurde entbunden!", NotificationType.INFO, 5);
                    Wrapper.mc.thePlayer.playSound("random.anvil_use", 1f, 1f);
                    return;
                }

                int bind = Keyboard.getKeyIndex(args[1]);

                if (bind == 0) {
                    NotifyUtil.notification("Key nicht gefunden!", "Key §c" + args[1] + "§r wurde nicht gefunden!", NotificationType.ERROR, 5);
                    return;
                }

                mod.setBind(bind);
                moduleManager.saveBinds();
                NotifyUtil.notification("Modul gebunden!", "§c" + args[0] + "§r wurde auf §c" + args[1] + "§r gebunden!", NotificationType.INFO, 5);
                Wrapper.mc.thePlayer.playSound("random.anvil_use", 1f, 1f);
            } else {
                NotifyUtil.chat(syntax);
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                NotifyUtil.chat("Alle Keybinds (Format: MOD : KEY):");
                for (Module mod : Client.main().modMgr().getModules()) {
                    if (!(mod.bind() == 0)) {
                        NotifyUtil.chat(mod.name() + " §8:§a " + Keyboard.getKeyName(mod.bind()));
                    }
                }
            } else if (args[0].equalsIgnoreCase("clear")) {
                for (Module mod : Client.main().modMgr().getModules()) {
                    mod.setBind(0);
                }
                NotifyUtil.notification("Keybinds gelöscht!", "Alle Keybinds wurden gelöscht!", NotificationType.INFO, 5);
            } else {
                NotifyUtil.chat(syntax);
            }
        } else {
            NotifyUtil.chat(syntax);
        }

    }

    @Override
    public String getName() {
        return "Bind";
    }

}