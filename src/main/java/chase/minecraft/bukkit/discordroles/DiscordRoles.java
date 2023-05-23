package chase.minecraft.bukkit.discordroles;

import chase.minecraft.bukkit.discordroles.bot.DiscordBot;
import chase.minecraft.bukkit.discordroles.command.BotRolesCommand;
import chase.minecraft.bukkit.discordroles.command.LinkCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;


public final class DiscordRoles extends JavaPlugin
{
	
	
	public static Logger log = Bukkit.getLogger();
	
	public static FileConfiguration config;
	
	@Override
	public void onEnable()
	{
		log.info("Starting Plugin");
		config = getConfig();
		config.addDefault("guild-id", 0L);
		config.options().copyDefaults(true);
		saveDefaultConfig();
		log.info("ID: "+DiscordRoles.config.getLong("guild-id"));
		DiscordBot.start();
		Objects.requireNonNull(getCommand("link")).setExecutor(new LinkCommand());
		Objects.requireNonNull(getCommand("discordroles")).setExecutor(new BotRolesCommand());
	}
	
	@Override
	public void onDisable()
	{
		DiscordBot.getInstance().stop();
		log.warning("Stopping Plugin");
	}
	
}
