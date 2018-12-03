package cache;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * Just default config, for more advanced and optimizations theres better
 * timings
 * 
 * There's also like below, multi/exec, batch and push, and transactions
 */

public class RedisConfig {

	RedisCommands<String, String> commands;
	RedisClient client = RedisClient.create("redis://localhost");
	StatefulRedisConnection<String, String> connection = client.connect();

	public RedisConfig() {

	}

	public RedisCommands<String, String> commands() {
		return commands = connection.sync();
	}

	/**
	 * Pipelining:
	 * 
	 * RedisAsyncCommands<String, String> commands = connect.async();
	 * commands.multi();
	 * 
	 * list of commands here so maybe like if we got 1000 requests a second, push it
	 * all at the same time
	 * 
	 * RedisFuture<String> rf = commands.multi();
	 * 
	 * commands.setAutoFlushCommands(true); commands.flushCommands();
	 * 
	 * multi.thenAccept(rf);
	 * 
	 */
}