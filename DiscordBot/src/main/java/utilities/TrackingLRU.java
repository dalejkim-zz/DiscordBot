package utilities;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Using an internal tracking LRU style where, alongisde the redis data, we keep
 * our own structure that updates alongside it
 * 
 * This is one way where we can track data and update/push data to the mysql
 * based off a few factors like time, frequency, if offline/online, other
 * metrics and whatnot
 * 
 * Again just a thought, not used
 * 
 * Base LRU's hashmap & DLL
 *
 * We could also use the zset zscore in redis to keep track there too, (syncing
 * with the eviction policies like LRU/LFU in redis) alot of interesting things,
 * there's replication on redis but if there was a sudden peak, this can adjust
 * before any breakage or slowdowns occur ttl/expiration
 *
 */

public class TrackingLRU {
	int capacity;
	// head and tail, with head representing the most and the right / tail being the
	// least frequently used waiting for boot
	CustomNode head = null, tail = null;
	Map<Integer, CustomNode> hm = new HashMap<Integer, CustomNode>();

	/**
	 * Java does detail that there are default capacity or capacity algorithms
	 * that's true but can configure your own based off of the 75% rule or creating
	 * overhead
	 * 
	 * @param capacity
	 */
	public TrackingLRU(int capacity) {
		this.capacity = capacity;
	}

	public int get(int key) {
		if (hm.containsKey(key)) {
			CustomNode node = hm.get(key);
			remove(node);
			set(node);
			return node.value;
		}
		return -1;
	}

	@SuppressWarnings("unlikely-arg-type")
	public void add(int key, int value) {
		CustomNode curr = null;
		if (hm.containsKey(key)) {
			curr = hm.get(key);
			curr.value = value;
			remove(curr);
			set(curr);
		} else {
			// CustomNode dne = new CustomNode(key, value);
			if (hm.size() >= capacity) {
				hm.remove(tail);
				set(curr);
			} else {
				set(curr);
			}
		}
	}

	public void remove(CustomNode node) {
		if (node.pre != null) {
			node.pre.next = node.next;
		} else {
			head = node.next;
		}
		if (node.next != null) {
			node.next.pre = node.pre;
		} else {
			tail = node.pre;
		}
	}

	public void set(CustomNode node) {
		// CustomNode pre = null;
		// CustomNode next = head;

		if (head != null) {
			head.pre = node;
		}

		head = node;

		if (tail == null) {
			tail = head;
		}
	}
}