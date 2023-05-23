package chase.minecraft.bukkit.discordroles.bot;

import chase.minecraft.bukkit.discordroles.DiscordRoles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class DiscordBot
{
	public record MessageResponse(String message, boolean success)
	{
	}
	
	@Nullable
	private static DiscordBot instance;
	
	private final JDA bot;
	private HashMap<Long, Member> membersWithIds;
	private HashMap<String, Member> membersWithNames;
	
	protected DiscordBot()
	{
		Properties properties = new Properties();
		try (InputStream fs = DiscordRoles.class.getResourceAsStream("/bot.properties"))
		{
			properties.load(fs);
		} catch (IOException e)
		{
			DiscordRoles.log.severe("Unable to load bot token: " + e.getMessage());
			e.printStackTrace();
		}
		
		this.bot = JDABuilder.createDefault(properties.getProperty("token")).enableIntents(GatewayIntent.GUILD_MEMBERS).build();
		this.bot.addEventListener(new ListenerAdapter()
		{
			@Override
			public void onButtonInteraction(@NotNull ButtonInteractionEvent event)
			{
				if (event.getComponentId().equalsIgnoreCase("accept-link-button"))
				{
					User user = event.getUser();
					Guild guild = getGuild();
					if (guild != null)
					{
						guild.addRoleToMember(user, getOrCreateRole()).complete();
						event.reply("Account linked successfully!").complete();
					}
				}
			}
		});
		membersWithIds = new HashMap<>();
		membersWithNames = new HashMap<>();
		refreshMembersList();
	}
	
	/**
	 * Links discord account with your minecraft account
	 *
	 * @param mc      Minecraft username
	 * @param discord discord username
	 * @return Message response
	 */
	public MessageResponse link(String mc, String discord)
	{
		Member member = getMember(discord);
		if (member != null)
		{
			User user = member.getUser();
			
			@Nullable Guild guild = getGuild();
			if (guild != null)
			{
				EmbedBuilder embedBuilder = new EmbedBuilder()
						.setTitle("Link Your Minecraft Account?")
						.setDescription("Hello %s, would you like to link your minecraft account with %s?".formatted(mc, guild.getName()));
				
				Button accept = Button.primary("accept-link-button", "Link Account!");
				Button learnmore = Button.link("https://github.com/DcmanProductions/DiscordRoles", "Learn More...");
				
				embedBuilder.setImage(DiscordRoles.config.getString("banner-image"));
				user.openPrivateChannel().queue(channel ->
				{
					channel.sendMessageEmbeds(embedBuilder.build()).setActionRow(accept, learnmore).queue();
				});
				return new MessageResponse("User linked", true);
			}
		}
		return new MessageResponse("Unable to find user: %s".formatted(discord), false);
	}
	
	public Role getOrCreateRole()
	{
		String name = DiscordRoles.config.getString("server-name");
		Guild guild = getGuild();
		assert guild != null;
		assert name != null;
		return guild.getRolesByName(name, true).stream().findFirst().orElseGet(() -> guild.createRole().setName(name).complete());
	}
	
	public String generateInviteLink()
	{
		return bot.getInviteUrl(
				Permission.MANAGE_ROLES,
				Permission.MANAGE_CHANNEL,
				Permission.MANAGE_SERVER,
				Permission.MODERATE_MEMBERS,
				Permission.CREATE_INSTANT_INVITE,
				Permission.USE_APPLICATION_COMMANDS,
				Permission.MESSAGE_SEND,
				Permission.VIEW_GUILD_INSIGHTS
		);
	}
	
	/**
	 * Gets a list of all usernames
	 *
	 * @return Usernames
	 */
	public Set<String> getUsers()
	{
		Set<String> names = new HashSet<>();
		Guild guild = getGuild();
		if (guild != null)
		{
			if (membersWithIds.size() == 0) refreshMembersList();
			for (Member member : membersWithIds.values())
			{
				if (!member.getUser().isBot() && !member.getUser().isSystem())
				{
					names.add(member.getUser().getName());
				}
			}
		}
		
		return names;
	}
	
	public void refreshMembersList()
	{
		refreshMembersList(null);
	}
	
	public void refreshMembersList(@Nullable CommandSender sender)
	{
		membersWithIds.clear();
		Guild guild = getGuild();
		if (guild != null)
		{
			DiscordRoles.log.warning("%s[Discord Roles] %sRefreshing discord members list!".formatted(ChatColor.GREEN, ChatColor.GOLD));
			for (Member member : getGuild().loadMembers().get())
			{
				membersWithIds.put(member.getIdLong(), member);
				membersWithNames.put(member.getUser().getName(), member);
			}
			if (sender != null)
			{
				DiscordRoles.sendMessage(sender, "%sLoaded %s%d %sMembers".formatted(ChatColor.GOLD, ChatColor.GREEN, membersWithIds.size(), ChatColor.GOLD));
			}
		}
		
	}
	
	public @Nullable Member getMember(long id)
	{
		Guild guild = getGuild();
		if (guild != null && membersWithIds.containsKey(id))
		{
			return membersWithIds.get(id);
		}
		
		return null;
	}
	
	public @Nullable Member getMember(String name)
	{
		Guild guild = getGuild();
		if (guild != null && membersWithNames.containsKey(name))
		{
			return membersWithNames.get(name);
		}
		
		return null;
	}
	
	public boolean doesMemberExist(String name)
	{
		return getMember(name) != null;
	}
	
	public boolean doesMemberExist(int id)
	{
		return getMember(id) != null;
	}
	
	public @Nullable Guild getGuild()
	{
		long guildId = DiscordRoles.config.getLong("guild-id");
		if (guildId == 0)
		{
			DiscordRoles.log.warning("You must set your discords guild id before using this!");
			return null;
		}
		Guild guild = bot.getGuildById(guildId);
		if (guild == null)
		{
			DiscordRoles.log.severe("Unable to find guild from id: " + guildId);
		}
		return guild;
	}
	
	/**
	 * Starts the bot with the given token
	 *
	 * @throws IllegalArgumentException if the token is blank
	 */
	public static void start()
	{
		if (instance != null)
		{
			throw new RuntimeException("Discord Bot cannot be started more than once");
		}
		instance = new DiscordBot();
	}
	
	public void stop()
	{
		instance = null;
		bot.shutdownNow();
	}
	
	/**
	 * Gets current instance of bot
	 *
	 * @return Bot Instance
	 * @throws RuntimeException if the bot has not been started.
	 */
	public static DiscordBot getInstance()
	{
		if (instance == null)
		{
			start();
		}
		return instance;
	}
}
