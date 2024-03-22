package DBActions;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseActions {

	private static Connection conn = null;
	private static Statement smt;
	private static ResultSet rSet = null;
	PreparedStatement preparedStatement;
	int id;
	String name;
	ArrayList<String> studentName = new ArrayList<>();
	ArrayList<Integer> studentID = new ArrayList<>();

	public static void connectToDatabase(String db_url, String user, String password)
			throws SQLException, ClassNotFoundException {

		try {
			conn = DriverManager.getConnection(db_url, user, password);
			if (conn != null) {
				System.out.println("Connected to the database");
			}
		} catch (SQLException ex) {
			System.out.println("An error occurred. Maybe user/password is invalid");
			ex.printStackTrace();
		}
	}

	public static void createTable() throws SQLException {
		smt = conn.createStatement();
		String query = "CREATE TABLE employees (id INT AUTO_INCREMENT PRIMARY KEY, emp_name varchar(244), last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);";

		try {
			smt.executeUpdate(query);
			System.out.println("Created table Student in given database...");

		} catch (SQLSyntaxErrorException e) {
			if (e.getMessage().contains("already exists")) {
				String dropQuery = "DROP TABLE employees";
				smt.execute(dropQuery);
				smt.executeUpdate(query);
				System.out.println("Existing table dropeed and Created table employees in given database...");
			}
		}

		String insertToDb = "INSERT INTO employees (emp_name) VALUES ('kanika')";
		// Execute the update query and store the number of affected rows
		int rowsAffected = smt.executeUpdate(insertToDb);

		// Check if the insert operation was successful
		if (rowsAffected > 0) {
			System.out.println("Inserted");
		} else {
			System.out.println("Failed");
		}
	}

	public static void dropTable() throws SQLException {
		String query = "DROP TABLE employees";
		smt.execute(query);
	}

	/**
	 * Pass valid SQL query and get the result
	 *
	 * @param sql
	 * @return
	 */
	public ResultSet query(String sql) {

		try {
			smt = conn.createStatement();
			rSet = smt.executeQuery(sql);

		} catch (Exception e) {

			System.out.println(e.toString());
		}
		return (rSet);
	}

	// move to test class
	public static List<String> getDBData(String sql) throws SQLException {
		ResultSet resultSet = smt.executeQuery(sql);
		List<String> data = new ArrayList<String>();
		while (resultSet.next()) {

			data.add(Integer.toString(resultSet.getInt(1)));
			data.add(resultSet.getString(2));
			data.add(resultSet.getString(3));
		}

		return data;
	}

}
