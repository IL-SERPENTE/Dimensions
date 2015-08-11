package net.samagames.parallelspvp.commands;

import net.samagames.parallelspvp.ParallelsPVP;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by zyuiop on 28/09/14.
 */
public class CommandStart implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        commandSender.sendMessage(ChatColor.GREEN+">> L'arène est démarrée de force. <<");
        ParallelsPVP.instance.getArena().startGame();
        return true;
    }
}
