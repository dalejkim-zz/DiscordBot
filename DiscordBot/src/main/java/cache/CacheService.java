/**
 * 
 */
package cache;

import java.sql.SQLException;
import java.util.Map;

/**
 * CacheService interface for a level of flexibility and refactoring and
 * abstraction
 *
 */
public interface CacheService {
	public void setUserStats(String key, Map<String, String> map, boolean pushUpdate) throws SQLException;

	public String getUser(String uuid, String userName, String guild);

	public String getLastPlayed(String key);

	public String getUserLastGameStats(String key) throws SQLException;

	public Map<String, String> getUserStats(String key);

	public String getLastGamePlayTime(String key);

	public boolean exists(String key);

	public boolean lastGameExists(String key);

	public void removeLastGameStats(String key);

	public String removeUserFromServer(String key);

	// Put a hold on this
	// public Object getObject(String key);
}