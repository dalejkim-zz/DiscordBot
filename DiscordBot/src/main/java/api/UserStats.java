package api;

public class UserStats {

	private final long id;
	private final String content;

	public UserStats(long id, String content) {
		this.id = id;
		this.content = content;
	}

	public long getId() {
		return id;
	}

	public String getContent() {
		return content;
	}

//	public boolean exists(String key) {
//	if(!(key.equals(UUID))) {
//		return false;
//	}
//	return true;
}