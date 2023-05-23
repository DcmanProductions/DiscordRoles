package chase.minecraft.bukkit.discordroles.command;

import chase.minecraft.bukkit.discordroles.DiscordRoles;
import chase.minecraft.bukkit.discordroles.bot.DiscordBot;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
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
				DiscordRoles.sendMessage(sender, ChatColor.GOLD + DiscordBot.getInstance().generateInviteLink());
				return true;
			}
			if (args[0].equalsIgnoreCase("reload"))
			{
				DiscordRoles.sendMessage(sender, "%sRefreshing discord members list!".formatted(ChatColor.GOLD));
				DiscordBot.getInstance().refreshMembersList(sender);
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
					DiscordRoles.sendMessage(sender, "%sGuild ID set to %s%s".formatted(ChatColor.GOLD, ChatColor.GREEN, args[1]));
					DiscordRoles.getPlugin(DiscordRoles.class).saveConfig();
					DiscordBot.getInstance().refreshMembersList(sender);
					return true;
				} catch (NumberFormatException e)
				{
					DiscordRoles.sendMessage(sender, "%sInvalid Discord Server GUILD: %s".formatted(ChatColor.RED, args[1]));
					return false;
				}
			}
			
			if (args[0].equalsIgnoreCase("banner"))
			{
				try
				{
					final URI imageUri = new URI(args[1]);
					DiscordRoles.config.set("banner-image", imageUri.toString());
					DiscordRoles.getPlugin(DiscordRoles.class).saveConfig();
				} catch (URISyntaxException e)
				{
					DiscordRoles.sendMessage(sender, "%sInvalid Image URL: %s".formatted(ChatColor.RED, args[1]));
				}
				return true;
				
			}
			if (args[0].equalsIgnoreCase("server"))
			{
				StringBuilder buffer = new StringBuilder();
				for (int i = 1; i < args.length; i++)
				{
					buffer.append(args[i]);
					if (i < args.length - 1)
						buffer.append(" ");
				}
				DiscordRoles.config.set("server-name", buffer.toString());
				DiscordRoles.getPlugin(DiscordRoles.class).saveConfig();
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
			return List.of("invite", "guild", "server", "banner", "reload");
		}
		return new ArrayList<>();
	}
}
