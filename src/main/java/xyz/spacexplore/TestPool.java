package xyz.spacexplore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestPool {

	public static void main(String[] args) throws SQLException {
		for (int i = 0; i < 2000; i++) {
		Pool instance = PoolManager.INSTANCE.getInstance();
		Connection connection = instance.getConnection();
		PreparedStatement prepareStatement = connection.prepareStatement("select * from atom where atom_id=99");
		ResultSet executeQuery = prepareStatement.executeQuery();
		while (executeQuery.next()) {
			System.out.println(executeQuery.getString("atom_id"));
			System.out.println(i);
		}
		executeQuery.close();
		prepareStatement.close();
		connection.close();
		}
	}
}
