package events;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Implementing the interface for JDA's EventManager - we use the default for
 * simplicity sake. (Annotated mode exists as other implementation)
 * 
 * Providing methods to listen for events
 * 
 * Was going to create an abstract class, however since we are implementing from
 * IEventManager and AbstractExecutorService
 * 
 * Make sure correct permissions
 */
public class EventManager implements IEventManager {

	private final ThreadPoolExecutor executor;
	private List<Object> listeners = new LinkedList<>();

	/**
	 * Default settings for the ThreadFactory -> Can change thread priorities or
	 * marking daemon threads
	 * 
	 * New threads are created using a ThreadFactory. If not otherwise specified, a
	 * Executors.defaultThreadFactory() is used, that creates threads to all be in
	 * the same ThreadGroup and with the same NORM_PRIORITY priority and non-daemon
	 * status. By supplying a different ThreadFactory, you can alter the thread's
	 * name, thread group, priority, daemon status, etc. If a ThreadFactory fails to
	 * create a thread when asked by returning null from newThread, the executor
	 * will continue, but might not be able to execute any tasks
	 */
	public EventManager() {
		ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
		this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(builder.build());
		// tested and maybe some sort of algorithm to furhter optimize
		// this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(100);
		// builder.build());
	}

	/**
	 * Register the passed in object listener
	 * 
	 * Could simply check for exception and since its either true or false, add the
	 * listener. However with a ton of requests going and adding listeners,
	 * micro-optimization at the cost of "cleaner code." Would love to test and see
	 * if this makes a difference -> production & high requests
	 * 
	 * @param listener
	 */
	@Override
	public void register(Object listener) {
		if (listener instanceof ListenerAdapter) {
			listeners.add(listener);
			// System.out.println("Listener Added!");
		} else {
			throw new IllegalArgumentException("Proper listeners must be an instance of EventListener.");
		}
	}

	/**
	 * Unregister the listener without worrying if it was successful or not
	 * 
	 * Could log if it was actually removed or not through a conditional statement
	 * for console
	 * 
	 * @param listener
	 */
	@Override
	public void unregister(Object listener) {
		listeners.remove(listener);
		// System.out.println("Listener Removed");
	}

	/**
	 * General handler
	 * 
	 * For the many listeners and exceptions that can or can't be handled
	 * 
	 * @param e
	 */
	@Override
	public void handle(Event e) {
		executor.submit(() -> {
			e.getJDA();
			List<Object> copyListeners = new LinkedList<>(listeners);
			for (Object listener : copyListeners) {
				try {
					((ListenerAdapter) listener).onEvent(e);
				} catch (PermissionException pe) {
					pe.printStackTrace();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		});
	}

	/**
	 * List of registered listeners
	 */
	@Override
	public List<Object> getRegisteredListeners() {
		return this.listeners;
	}
}