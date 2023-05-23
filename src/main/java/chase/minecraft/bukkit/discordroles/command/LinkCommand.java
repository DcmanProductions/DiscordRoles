package chase.minecraft.bukkit.discordroles.command;

import chase.minecraft.bukkit.discordroles.DiscordRoles;
import chase.minecraft.bukkit.discordroles.bot.DiscordBot;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LinkCommand implements TabExecutor
{
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
	{
		if (DiscordRoles.config.getLong("guild-id") == 0L)
		{
			DiscordRoles.log.warning("You must set your discords guild id before using this!");
			DiscordRoles.sendMessage(sender, ChatColor.RED + "You must set your discords guild id before using this!");
			DiscordRoles.sendMessage(sender, "%sUse %s/discordroles guild <guild-id> %sto set your guild-id".formatted(ChatColor.RED, ChatColor.GOLD, ChatColor.RED));
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
			try
			{
				long id = Long.parseLong(username);
				Member member = DiscordBot.getInstance().getMember(id);
				if (member != null)
				{
					username = member.getUser().getName();
				}
			} catch (NumberFormatException ignore)
			{
			}
			
			if (DiscordBot.getInstance().doesMemberExist(username))
			{
				DiscordRoles.sendMessage(sender, "%sSending authentication request to %s%s".formatted(ChatColor.GOLD, ChatColor.GREEN, username));
				DiscordBot.MessageResponse response = DiscordBot.getInstance().link(sender.getName(), username);
				if (!response.success())
				{
					DiscordRoles.sendMessage(sender, ChatColor.RED + response.message());
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
			List<String> complete = new ArrayList<>();
			Set<String> users = DiscordBot.getInstance().getUsers();
			for (String user : users)
			{
				if (user.startsWith(args[0]))
					complete.add(user);
				
			}
			return complete;
		}
		return new ArrayList<>();
	}
}
