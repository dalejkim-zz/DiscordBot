package main;

//import com.jagrosh.jdautilities.command.*;

import events.BotListener;
import events.EventManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

import static spark.Spark.*;

import api.UserStatsService;

/**
 * Simple launcher with the base api in here.
 * 
 * Only accepts a get request by calling
 * 
 * localhost:4567/user/stats/:id replacing ":id" with the discord user you are
 * looking for
 *
 *
 * Current prefix is set to '.' by the way
 * 
 */

public class Launcher {
	public static void main(String[] args) {
		run();
	}

	public static void run() {
		final String token = "";
		UserStatsService stats = new UserStatsService();

		/**
		 * CommandClientBuilder builder = new CommandClientBuilder();
		 * 
		 * builder.setPrefix("."); builder.setAlternativePrefix("!");
		 * builder.setOwnerId(ownerId); builder.addCommand(new CoolCommand());
		 * builder.setGuildSettingsManager(new MyBotsGuildSettingsManager());
		 * 
		 * CommandClient client = builder.build();
		 * 
		 * Shutdown mechanism create a getJDA().shutdown() make some sort of hook to
		 * look for that based off each bot / shard
		 * 
		 * builder.useSharding() based off sharding and the idea of 1000 servers per
		 * shard
		 * 
		 */

		try {
			JDA jda = new JDABuilder(token).setEventManager(new EventManager()).addEventListener(new BotListener())
					.build();

			jda.awaitReady();

			get("/userstats/:id", (req, res) -> {
				try {
					return stats.getAndBeautifyData(req.params(":id"));
				} catch (NullPointerException npe) {
					res.status(404);
					return "User does not exist / Has no previous history.";
				} catch (Exception e) {
					res.status(404);
					return "User does not exist / Has no previous history.";
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}