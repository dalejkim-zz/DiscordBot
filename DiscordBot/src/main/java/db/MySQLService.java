package db;

import java.sql.*;

public class MySQLService implements QueryService {
	// private String dbServer, dbName, dbUser, dbPass;
	// private int port;
	// private Connection status;
	Connection c = null;
	Statement s = null;
	PreparedStatement ps = null;

	/**
	 * when creating statements there is concurrent resultsets for different
	 * ports...
	 * 
	 * versus just insert/replace so delete row then insert row process is on
	 * average x32 faster
	 * 
	 * using values as generics security/performance
	 */
	// PreparedStatement ps;

	// for diff configs later just pass in the attempted connections
	// public MySQLService(String dbServer, String dbName, String dbUser, String
	// dbPass, int port) {
	// this.dbServer = dbServer;
	// this.dbName = dbName;
	// this.dbUser = dbUser;
	// this.dbPass = dbPass;
	// this.port = port;
	// }

	public MySQLService() {

	}

	/**
	 * DiscriminatorID for now from discord temporarily, its technically the
	 * username but not for the guild specifically
	 * 
	 * @throws SQLException
	 * 
	 */
	@Override
	public void insertData(String userUUID, String userName, String lastGame, String lastGameDate,
			String lastGameLength) throws SQLException {
		String query = " INSERT into userstats (userUUID, user_discriminator_id, last_game_played, when_last_game_played, length_last_game_played)"
				+ " values (?, ?, ?, ?, ?) " + "ON DUPLICATE KEY UPDATE "
				+ "user_discriminator_id = VALUES (user_discriminator_id), "
				+ "last_game_played = VALUES (last_game_played), "
				+ "when_last_game_played = VALUES (when_last_game_played), "
				+ "length_last_game_played = VALUES (length_last_game_played)";

		try {
			attemptConnection();
			ps = c.prepareStatement(query);
			ps.setString(1, userUUID);
			ps.setString(2, userName);
			ps.setString(3, lastGame);
			ps.setString(4, lastGameDate);
			ps.setString(5, lastGameLength);
			ps.executeUpdate();
			ps.closeOnCompletion();
			ps = null;
			c.close();
			c = null;
			System.out.println("Done!");
		} catch (Exception e) {
			e.printStackTrace();

//			FORCE CREATE A TABLE just in case if its not created already but should be
//			s = c.createStatement();
//			String sql = "CREATE TABLE userstats (userUUID INTEGER not null, "
//					+ " user_discriminator_id VARCHAR(255), "
//					+ " last_game_played VARCHAR(255), "
//					+ " when_last_game_played VARCHAR(255), "
//					+ " length_last_game_played INTEGER, "
//					+ " PRIMARY KEY ( userUUID ))";
//			s.executeUpdate(sql);
		} finally {
			/**
			 * 
			 * Checking to make sure all resources are closed So no 'leaking' of resources
			 * occurs => problems
			 * 
			 */
			if (s != null) {
				try {
					s.close();
				} catch (SQLException se) {
					//
				}
				s = null;
			}

			if (c != null) {
				try {
					c.close();
				} catch (SQLException se) {
					//
				}
				c = null;
			}

			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException se) {
					//
				}
				ps = null;
			}
		}
	}

	/**
	 * Redis does not maintain sorting on hashes or assure a specific output so we
	 * could either maintain the output order by adding a keyslist and atomically
	 * updating both or we could brute force it or use other redis structures
	 * 
	 * Actually since I am getting it in that order it should work nvm
	 */

	@Override
	public void updateDB(String data) throws SQLException {
		String clean = beautifyData(data);
		String[] splitter = clean.split(",");

		insertData(splitter[1].toString(), splitter[3].toString(), splitter[5].toString(), splitter[7].toString(),
				splitter[9].toString());

		// loop through a key list is another way too
	}

	@Override
	public String beautifyData(String data) {
		return data.replaceAll("\\[KeyValue\\[|KeyValue\\[|\\]|\\s", "");
	}

	public Connection attemptConnection() {
		try {
			// MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
			// ds.setServerName(dbServer);
			// ds.setDatabaseName(dbName);
			// ds.setUser(dbUser);
			// ds.setPassword(dbPass);
			// ds.setPort(port);
			// As far as I recall, unless we use an updated or higher level communication /
			// SSL is redundant since at MySQL level its already held
			// within a SSL tunnel

			// Statement s = null;
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("Connecting to database...");
			c = DriverManager.getConnection("jdbc:mysql://localhost:3306/DiscordUsers?useSSL=false", "root", "root");
			c.setAutoCommit(true);
			System.out.println("Success!");
			return c;
			// return ds.getConnection();
		} catch (Exception e) {
			System.out.println("Failed");
		}
		return null;
	}

	public String pullUserLastGameStats(String userUUID) throws SQLException {
		String userDetails = null;
		try {
			s = c.createStatement();

			String query = "SELECT userUUID, user_discriminator_id, last_game_played, when_last_game_played, length_last_game_played FROM userstats WHERE userUUID = "
					+ userUUID;

			ResultSet rs = s.executeQuery(query);

			while (rs.next()) {
				userUUID = rs.getString(1);
				String userName = rs.getString(2);
				String lastGamePlayed = rs.getString(3);
				String lastGameDate = rs.getString(4);
				int lastGameLength = rs.getInt(5);

				System.out.println("UserUUID: " + userUUID);
				System.out.println("Username: " + userName);
				System.out.println("Last game played: " + lastGamePlayed);
				System.out.println("Last game played on " + lastGameDate);
				System.out.println("Last game lasted " + lastGameLength);
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
		return userDetails;
	}

	/**
	 * Split the data in a more intelligent way
	 * 
	 * Right now it doesn't account for people having commas or data with commas
	 * 
	 * Other way is pattern detection based off the Key and then the grabbing the
	 * value before the next key matches
	 */

	/**
	 * @Override public String getUserStats(String userUUID) { StringBuilder sb =
	 *           new StringBuilder(); try { s = c.createStatement();
	 * 
	 *           String query = "SELECT * FROM UserStats";
	 * 
	 *           ResultSet rs = s.executeQuery(query);
	 * 
	 *           while(rs.next()) { sb.append(rs.get()); } } catch(SQLException
	 *           sqlException) { sqlException.printStackTrace(); } catch(Exception
	 *           e) { e.printStackTrace(); } finally { c.close(); } return
	 *           sb.toString(); }
	 */
}