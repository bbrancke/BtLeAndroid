package com.nerdlinger.btle.User;

// There is only ONE user; this is probably their first name
// (Other users should run this app on their own phones)
// This is just so we can have a top label:
//      "Fred's Health"
// We only keep one name in the database.
public class OneUser {
	private int m_id;  // SQL Row Id
	public int GetId() { return m_id; }

	private String m_userName;
	public String GetUsername() { return m_userName; }

	// Reading from SQL:
	public OneUser(int id, String userName) {
		m_id = id;
		m_userName = userName;
	}

	// Creating a new User from UI:
	public OneUser(String userName) {
		m_id = 0;
		m_userName = userName;
	}
}
