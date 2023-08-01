// Jiaxuan Wen, Hui Shi, Jingyi Yu
// todo list
// 2023
// This class is for testing the database connection
// It doesn't interact with other classes

import java.sql.*;

public class DatabaseConnectionTest {
  private static final String DB_URL = "jdbc:mysql://localhost:3306/tododemo";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "123456";

  public static void main(String[] args) {
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
      System.out.println("Connected to the database successfully!");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}