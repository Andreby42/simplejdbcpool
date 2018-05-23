package xyz.spacexplore;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;

public class PoolConnection {
	private AtomicBoolean busy=new AtomicBoolean(false);
	private Connection connection;
	
	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public AtomicBoolean getBusy() {
		return busy;
	}

	public void setBusy(AtomicBoolean busy) {
		this.busy = busy;
	}

	public PoolConnection() {
		super();
	}

	public PoolConnection(Connection connection) {
		super();
		this.connection = connection;
	}

	public PoolConnection(AtomicBoolean busy, Connection connection) {
		super();
		this.busy = busy;
		this.connection = connection;
	}
	
}
