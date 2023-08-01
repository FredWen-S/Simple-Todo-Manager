// Jiaxuan Wen, Hui Shi, Jingyi Yu
// todo list
// 2023
// This class is a manager of database
// It uses TodoTask class inside the class

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TodoDatabaseManager {

  private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
  private static final String DB_URL = "jdbc:mysql://localhost:3306/tododemo?"
      + "useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "123456";

  private static final String TABLE_NAME = "tasks";
  private static final String COL_TITLE = "title";
  private static final String COL_DEADLINE = "deadline";
  private static final String COL_PRIORITY = "priority";
  private static final String COL_COMPLETED = "completed";

  private Connection connection;

  // parameters: none
  // behavior: load and register JDBC driver and then establish the database connection
  // exception: when the class is not found or the connection to the database has problems, catch it

  public TodoDatabaseManager() {
    try {
      // Load and register JDBC driver
      Class.forName(JDBC_DRIVER);

      // Establish the database connection
      connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

      System.out.println("Database connection established successfully.");
    } catch (ClassNotFoundException | SQLException e) {

      // Handle the exception appropriately, like logging or throwing custom exception
      e.printStackTrace();
    }
  }

  // parameters: keyword(String)
  // return: a list of todo tasks
  // behavior: load tasks from the database
  // exception: catch all the database related problems

  public List<TodoTask> loadTasksFromDatabase(String keyword) {
    List<TodoTask> tasks = new ArrayList<>();
    try {
      String query = "SELECT " + COL_TITLE + ", " + COL_DEADLINE + ", " 
          + COL_PRIORITY + ", " + COL_COMPLETED + " FROM " + TABLE_NAME;
      if (keyword != null && !keyword.trim().isEmpty()) {
        query += " WHERE " + COL_TITLE + " LIKE ?";
      }
      try (PreparedStatement statement = connection.prepareStatement(query)) {
        if (keyword != null && !keyword.trim().isEmpty()) {
          statement.setString(1, "%" + keyword + "%");
        }
        try (ResultSet resultSet = statement.executeQuery()) {
          while (resultSet.next()) {
            String title = resultSet.getString(COL_TITLE);
            String deadline = resultSet.getString(COL_DEADLINE);
            String priority = resultSet.getString(COL_PRIORITY);
            boolean completed = resultSet.getBoolean(COL_COMPLETED);

            tasks.add(new TodoTask(title, deadline, priority, completed));
          }
        }
      } 
    } catch (SQLException e) {

      // Handle the exception appropriately, like logging or throwing custom exception
      e.printStackTrace();
    }
    return tasks;
  }

  // behavior: save tasks to the database
  // parameters: tasks(List<TodoTask>)
  // return: none
  // exception: catch sql exception
  public void saveTasksToDatabase(List<TodoTask> tasks) {
    try {

      // Clear all data from the table
      String clearQuery = "DELETE FROM " + "tasks";

      try (PreparedStatement clearStatement = connection.prepareStatement(clearQuery)) {
        clearStatement.executeUpdate();
      }

      // Insert new tasks into the database using batch updates
      String insertQuery = "INSERT INTO " + TABLE_NAME + " (" + COL_TITLE + ", " + COL_DEADLINE 
            + ", " + COL_PRIORITY + ", " + COL_COMPLETED + ") VALUES (?, ?, ?, ?)";

      try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
        for (TodoTask task : tasks) {
          statement.setString(1, task.getTitle());
          statement.setString(2, task.getDeadline());
          statement.setString(3, task.getPriority());
          statement.setBoolean(4, task.isCompleted());
          statement.addBatch(); // Add the current task to the batch
        }
        statement.executeBatch(); // Execute the batch update
      }
    } catch (SQLException e) {

      // Handle the exception appropriately, like logging or throwing custom exception
      e.printStackTrace();
    }
  }

  public void closeConnection() {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {

      // Handle the exception appropriately, like logging or throwing custom exception
      e.printStackTrace();
    }
  }
}
