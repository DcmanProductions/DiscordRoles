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

public class LinkCommand implements TabExecutor
{
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
	{
		if (DiscordRoles.config.getLong("guild-id") == 0L)
		{
			DiscordRoles.log.warning("You must set your discords guild id before using this!");
			sender.sendMessage(ChatColor.RED + "You must set your discords guild id before using this!");
			sender.sendMessage("%sUse %s/discordroles guild <guild-id> %sto set your guild-id".formatted(ChatColor.RED, ChatColor.GOLD, ChatColor.RED));
			return true;
		}
		if (args.length >= 1)
		{
			StringBuilder buffer = new StringBuilder();
			for (int i = 0; i < args.length; i++)
			{
				buffer.append(args[i]);
				if (i < args.length - 1)
					buffer.append(" ");
			}
			String username = buffer.toString();
			if (DiscordBot.getInstance().getUsers().contains(username))
			{
				DiscordBot.MessageResponse response = DiscordBot.getInstance().link(sender.getName(), username);
				if (!response.success())
				{
					sender.sendMessage(ChatColor.RED + response.message());
				}
				return true;
				
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
			return DiscordBot.getInstance().getUsers().stream().toList();
		}
		return new ArrayList<>();
	}
}
