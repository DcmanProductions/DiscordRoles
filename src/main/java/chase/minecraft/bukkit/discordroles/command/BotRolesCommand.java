package chase.minecraft.bukkit.discordroles.command;

import chase.minecraft.bukkit.discordroles.DiscordRoles;
import chase.minecraft.bukkit.discordroles.bot.DiscordBot;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BotRolesCommand implements TabExecutor
{
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
	{
		if (args.length == 1)
		{
			if (args[0].equalsIgnoreCase("invite"))
			{
				sender.sendMessage(ChatColor.GOLD + DiscordBot.getInstance().generateInviteLink());
				return true;
			}
		}
		if (args.length == 2)
		{
			if (args[0].equalsIgnoreCase("guild"))
			{
				try
				{
					final long guild = Long.parseLong(args[1]);
					DiscordRoles.config.set("guild-id", guild);
					sender.sendMessage("%sGuild ID set to %s%s".formatted(ChatColor.GOLD, ChatColor.GREEN, args[1]));
					DiscordRoles.getPlugin(DiscordRoles.class).saveConfig();
					return true;
				} catch (NumberFormatException e)
				{
					sender.sendMessage("%sInvalid Discord Server GUILD: %s".formatted(ChatColor.RED, args[1]));
					return false;
				}
			}
		}
		return false;
	}
	
	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
	{
		if (args.length == 1)
		{
			return List.of("invite", "guild");
		}
		return new ArrayList<>();
	}
}
