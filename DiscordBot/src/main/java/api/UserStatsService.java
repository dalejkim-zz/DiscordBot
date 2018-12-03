package api;

import java.sql.SQLException;

import cache.CacheService;
import cache.RedisService;

public class UserStatsService {
	private CacheService cs = new RedisService();
	
	public UserStatsService() {
		
	}
	
	/**
	 * Username
	 * The last game the Discord user played
	 * The last time they played the game
	 * How long they played the game for
	 * @throws SQLException 
	 * 
	 */
	public String getAndBeautifyData(String data) throws SQLException {
		String retrieveData = cs.getUserLastGameStats("userID:" + data);
		String clean = retrieveData.replaceAll("\\[KeyValue\\[|KeyValue\\[|\\]|\\s", "");
		String[] splitter = clean.split(",");
		clean = "The requested Discord User's stats by the SnowflakeID of: " + data
					+ " /// " + "Discord User Name: " + splitter[1]
					+ " /// " + "The Last Game Played Was: " + splitter[3]
					+ " /// " + "The Last Game Was Played On: " + splitter[5]
					+ " /// " + "The Game Duration Was For: " + splitter[7] + " (In Seconds).";
		return clean;
	}
}