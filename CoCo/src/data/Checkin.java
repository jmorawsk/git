package data;

import java.util.Date;

public class Checkin implements Comparable<Checkin> {

	private Date time;
	private User user;
	private Location location;
	
	public Checkin(User user, Location location, Date time) {
		this.setUser(user);
		this.setLocation(location);
		this.setTime(time);
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
	public int compareTo(Checkin other) {
		return this.getTime().compareTo(other.getTime());
	}
}
