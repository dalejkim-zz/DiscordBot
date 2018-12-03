package events;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import cache.CacheService;
import cache.RedisService;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Can order users by their name => guild by their snowflake id by their, for
 * example, User#1000 (User.getDiscriminator())
 * 
 * Handle rate limiting by using jda.queue()
 * 
 * another thing was that there was a null exception for last game played when
 * the user updates the first time so something we can do is preload everyone
 * from the server but that might be expensive on really big servers another
 * thing is
 * 
 * branch prediction could definitely be optimized on the onusergameupdate
 * 
 * another otion is getting all the user data then storing it in local
 * variaables and comparing it during a conditional check however hgetall is
 * O(n) whereas hget is O(1)
 * 
 * remember to minus starting a new game time from the end time and put it in
 * the map
 * 
 * game type is never null
 */
public class BotListener extends ListenerAdapter {
	/**
	 * If execution of commands like lastgame happened !lastgame could pick up from
	 * updated event that occurred during the time so to be messy but safe add
	 * variables
	 * 
	 * curious about the timing of lets say we populate everyone as none for both
	 * new game and old game, versus adding them to the DB as we get updates from
	 * them so we set all fields..
	 */
	CacheService redis = new RedisService();

	ExecutorService executor = Executors.newCachedThreadPool();
	ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;

	// long timeStamp = MiscUtil.getDiscordTimestamp(timeStamp);

	/**
	 * Make sure the bot has permissions to respond or reply to that channel
	 * (handlet hat in the eventmanager
	 * 
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		/**
		 * Discord has their own Snowflake timestamp and does not use Unix Epoch time,
		 * they use their own house Epoch.
		 * 
		 * Just using local time relative to the west coast
		 */
		// long epoch = System.currentTimeMillis();
		User author = e.getAuthor();
		String authorName = author.getName();
		Message message = e.getMessage();
		MessageChannel channel = e.getChannel();
		String msg = message.getContentDisplay();
		boolean isBot = author.isBot();
		// String key = "guild:" + e.getGuild().getName().replaceAll("\\s", "") +
		// ":member:" + author.getId();
		String key = "userID:" + author.getId();

		if (!isBot) {
			if (msg.equals(".lastgame")) {
				String fullMessage = authorName + ", your last game played was " + redis.getLastPlayed(key);
				channel.sendMessage(fullMessage).queue();
//				try {
//					redis.getUserLastGameStats(key);
//				} catch (SQLException e1) {
//					e1.printStackTrace();
//				}
			} else if (msg.equals(".resetgame")) {
				redis.removeLastGameStats(key);
				String fullMessage = author.getName() + ", your last game played has been reset!";
				channel.sendMessage(fullMessage).queue();
			}
		}
	}

	/**
	 * Discord has their own time epoch and rich presence time but unless im missing
	 * something Some games like League of Legends and Osu! I noticed that it keeps
	 * switching (Unless they meant things like Spotify cause that works fine). Also
	 * getTimestamps() the RichPresenceTimeStamp although it has duration,old,new It
	 * seems not everyone supports it or rather some applications would just trigger
	 * a (epoch time - 0) sometimes Just going to use unix epoch time unless im
	 * missing something, seems like a wasteful call since
	 */

	@Override
	public void onUserUpdateGame(UserUpdateGameEvent e) {
		// Shaving off the milliseconds by dividing by 1000
		int epoch = (int) (System.currentTimeMillis() / 1000);
		// String time = Long.toString(epoch);
		Map<String, String> map = new ConcurrentHashMap<String, String>();
		User userUUID = e.getUser();
		String userName = userUUID.getName() + "#" + userUUID.getDiscriminator();
		String localTime = LocalDateTime.now().toString();
		// String key = "guild:" + e.getGuild().getName().replaceAll("\\s", "") +
		// ":member:" + userUUID.getId();
		String key = "userID:" + userUUID.getId();
		Game currentGame = e.getNewGame();
		Game oldGame = e.getOldGame();
		boolean pushUpdate = false;
		// boolean isBot = e.getUser().isBot();

		/**
		 * Check the current game / gametype like Some games like League of Legends
		 * fires an event from In lobby > in queue > out of lobby > into game Could
		 * really deform data for how long they actually played
		 * 
		 */
		if (currentGame != null) {
			map.put("username", userName);
			map.put("new_game_played", currentGame.getName());
			map.put("new_game_start_time", Integer.toString(epoch));
		} else if (currentGame == null) {
			String lastGameExitTime = redis.getLastGamePlayTime(key);
			String lastGameLasted = String.valueOf((Integer.valueOf(epoch) - Integer.valueOf(lastGameExitTime)));
			map.put("new_game_played", "");
			map.put("last_game_played", oldGame.getName());
			map.put("when_last_game_played", localTime);
			map.put("last_game_play_time", lastGameLasted);
			pushUpdate = true;
		}
		try {
			redis.setUserStats(key, map, pushUpdate);
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}
	}

	/**
	 * 
	 * One for adding checking guild
	 * 
	 * currently online or offline
	 * 
	 */

	/**
	 * We could potentially just add this into the redis cache and hold people based
	 * off of... Hey. This user was playing and online then went offline. Let's push
	 * him now so add some logic there and maybe optimize the way we push to the db
	 */
	@Override
	public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent e) {
		LocalDateTime pacificTime = LocalDateTime.now();
		User userUUID = e.getUser();
		String online = userUUID.getName();
		OnlineStatus oldStatus = e.getOldOnlineStatus();
		OnlineStatus newStatus = e.getNewOnlineStatus();
		System.out.println(pacificTime + ": " + online + "." + oldStatus + " => " + newStatus);
	}
}