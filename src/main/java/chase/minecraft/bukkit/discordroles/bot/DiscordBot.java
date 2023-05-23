package chase.minecraft.bukkit.discordroles.bot;

import chase.minecraft.bukkit.discordroles.DiscordRoles;
import com.google.gson.Gson;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
		this.bot = JDABuilder.createDefault("MTEwODE2NTQ1NzM1OTQwNTA5Ng.Gm6HzV.yRMFFSWNxsGDzFxDP-vEDdQmERfOxSt0d3o7JI").build();
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
				Button button = Button.primary("accept-link-button", "Link Account!");
				
				File local = getBannerImage();
				if (local != null) {
					embedBuilder.setImage("attachment://banner");
					DiscordRoles.log.info(local.getAbsolutePath());
				}
				
				user.openPrivateChannel().queue(channel -> {
					if (local != null) {
						try {
							FileInputStream fs = new FileInputStream(local);
							channel.sendFiles(FileUpload.fromData(fs, "banner")).setEmbeds(embedBuilder.build()).setActionRow(button).queue(success -> {
								try {
									fs.close();
								} catch (IOException e) {
									DiscordRoles.log.severe("Unable to send image: " + e.getMessage());
									e.printStackTrace();
								}
							}, failure -> {
								try {
									fs.close();
								} catch (IOException e) {
									DiscordRoles.log.severe("Unable to send image: " + e.getMessage());
									e.printStackTrace();
								}
							});
						} catch (IOException e) {
							DiscordRoles.log.severe("Unable to send image: " + e.getMessage());
							e.printStackTrace();
						}
					} else {
						channel.sendMessageEmbeds(embedBuilder.build()).setActionRow(button).queue();
					}
				});
				
			}
			return new MessageResponse("User linked", true);
		}
		return new MessageResponse("Unable to find user: %s".formatted(discord), false);
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
	
	private static @Nullable File getBannerImage()
	{
		String[] types = new String[]{"png", "jpg", "jpeg", "gif", "webp"};
		for (String type : types)
		{
			File local = new File(DiscordRoles.getPlugin(DiscordRoles.class).getDataFolder(), "banner." + type);
			if (local.exists())
				return local;
		}
		return null;
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
			throw new RuntimeException("Discord Bot is not running");
		}
		return instance;
	}
}
