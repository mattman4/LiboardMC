package org.limc.liboardmc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {

    private LiboardMC plugin;
    public StartCommand(LiboardMC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(command.getName().equalsIgnoreCase("start"))) return true;

        Player player = (Player) sender;
        player.sendMessage("Starting game..");
        plugin.bot.challenge(player, "mattman23");



        return true;
    }
}
