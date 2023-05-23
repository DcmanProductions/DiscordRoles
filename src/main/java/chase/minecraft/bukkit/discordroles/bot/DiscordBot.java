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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DiscordBot
{
	public record MessageResponse(String message, boolean success)
	{
	}
	
	@Nullable
	private static DiscordBot instance;
	
	private final JDA bot;
	
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
		
		this.bot = JDABuilder.createDefault(properties.getProperty("token")).build();
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
						guild.addRoleToMember(user, getOrCreateRole());
					}
				}
				super.onButtonInteraction(event);
			}
		});
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
		Optional<User> userOptional = bot.getUsersByName(discord, true).stream().findFirst();
		if (userOptional.isPresent())
		{
			User user = userOptional.get();
			
			@Nullable Guild guild = getGuild();
			if (guild != null)
			{
				EmbedBuilder embedBuilder = new EmbedBuilder()
						.setTitle("Link Your Minecraft Account?")
						.setDescription("Hello %s, would you like to link your minecraft account with %s?".formatted(mc, guild.getName()));
				
				Button accept = Button.primary("accept-link-button", "Link Account!");
				Button learnmore = Button.link("https://github.com/dcmanProductions/discordroles", "Learn More...");
				
				embedBuilder.setImage(DiscordRoles.config.getString("banner-image"));
				user.openPrivateChannel().queue(channel ->
				{
					channel.sendMessageEmbeds(embedBuilder.build()).setActionRow(accept, learnmore).queue();
				});
				
			}
			return new MessageResponse("User linked", true);
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
				Permission.MESSAGE_SEND
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
			List<Member> members = guild.getMembers();
			for (Member member : members)
			{
				if (!member.getUser().isBot() && !member.getUser().isSystem())
				{
					names.add(member.getUser().getName());
				}
			}
		}
		
		return names;
	}
	
	public @Nullable Guild getGuild()
	{
		long guildId = DiscordRoles.config.getLong("guild-id");
		if (guildId == 0)
		{
			DiscordRoles.log.warning("You must set your discords guild id before using this!");
			return null;
		}
		
		return bot.getGuildById(guildId);
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
