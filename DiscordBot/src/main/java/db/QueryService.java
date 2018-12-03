package db;

import java.sql.SQLException;

/**
 * Just a general interface for refactoring/abstraction/flexibility
 * 
 * Could use redoing this and the other services > abstraction & encapsulation
 * on different levels
 */

public interface QueryService {
	public void updateDB(String data) throws SQLException;

	public void insertData(String userUUID, String userName, String lastGame, String lastGameDate,
			String lastGameLength) throws SQLException;

	public String pullUserLastGameStats(String userUUID) throws SQLException;

	public String beautifyData(String data);

	// public String getUserStats();
}