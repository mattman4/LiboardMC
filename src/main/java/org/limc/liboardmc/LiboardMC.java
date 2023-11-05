package org.limc.liboardmc;

import org.bukkit.plugin.java.JavaPlugin;

public final class LiboardMC extends JavaPlugin {

    public ChessBot bot;

    @Override
    public void onEnable() {
        bot = new ChessBot(this, "INSERT_TOKEN_HERE");
        getCommand("start").setExecutor(new StartCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
