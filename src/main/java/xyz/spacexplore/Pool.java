package xyz.spacexplore;

import java.sql.Connection;

public interface Pool {

	public Connection getConnection();
	
	public void createConnection( int count);
}
