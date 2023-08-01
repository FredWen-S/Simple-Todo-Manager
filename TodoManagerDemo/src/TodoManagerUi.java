// Jiaxuan Wen, Hui Shi, Jingyi Yu
// todo list
// 2023
// This class is a design for UI
// It uses TodoTask class inside the class

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TodoManagerUi extends JFrame {
  
  private final JTable taskTable;
  private final JTextField searchField;

  private final TodoDatabaseManager databaseManager;

  // Parameters: None
  // retrun : None
  // Behavior: Creates a TodoManagerUi object and initializes the UI interface. 
  // Exception: none
  public TodoManagerUi() {

    setTitle("Todo Manager");
    databaseManager = new TodoDatabaseManager();
    System.out.println("TodoDatabaseManager instance created.");


    // Create table header and data for task list (add "Completed" column)
    String[] columnNames = {"Title", "Deadline", "Priority", "Completed"};
    Object[][] data = {
      // Add more task data...
    };

    // Create JTable for task list
    DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {

      // Set "Completed" column as Boolean type to display checkboxes in the table
      @Override
      public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 3 ? Boolean.class : super.getColumnClass(columnIndex);
      }

      // Disable editing for task title, deadline, and priority columns
      @Override
      public boolean isCellEditable(int row, int column) {
        return column != 0 && column != 1 && column != 2;
      }
    };
        
    taskTable = new JTable(tableModel);

    // Create scroll pane to display task list
    final JScrollPane scrollPane = new JScrollPane(taskTable);

    // Create "Add New" button
    JButton addNewButton = new JButton("Add New");

    // Create "Mark as Complete" button
    final JButton markAsCompleteButton = new JButton("Mark as Complete");

    // Create search text field and search button
    searchField = new JTextField(20);
    final JButton searchButton = new JButton("Search");

    // Create "Delete" button
    final JButton deleteButton = new JButton("Delete Selected");

    // Load task data from database
    loadTasksFromDatabase();

    // Add action listener for "Add New" button
    addNewButton.addActionListener(e -> {

      // Show "Add New" dialog
      showAddNewDialog();
    });

    // Add action listener for "Mark as Complete" button
    markAsCompleteButton.addActionListener(e -> {

      // Handle logic for marking task as complete
      markSelectedTaskAsComplete();
    });

    // Add action listener for "Search" button
    searchButton.addActionListener(e -> {

      // Get search keyword
      String keyword = searchField.getText();

      // Load task data with keyword search
      loadTasksFromDatabase(keyword);
    });

    // Add action listener for "Delete" button
    deleteButton.addActionListener(e -> {

      // Handle logic for deleting selected tasks
      deleteSelectedTasks();
    });

    // Create search panel with FlowLayout
    JPanel searchPanel = new JPanel();
    searchPanel.add(new JLabel("Search:"));
    searchPanel.add(searchField);
    searchPanel.add(searchButton);

    // Create button panel with FlowLayout
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(addNewButton);
    buttonPanel.add(deleteButton);
    buttonPanel.add(markAsCompleteButton);

    // Create main panel with BorderLayout
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(searchPanel, BorderLayout.NORTH);
    mainPanel.add(scrollPane, BorderLayout.CENTER);
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    // Add main panel to the frame
    add(mainPanel);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        saveTasksToDatabase();
      }
    });

    Runtime.getRuntime().addShutdownHook(new Thread(this::saveTasksToDatabase));

    // Set frame size and close operation
    setSize(600, 400);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  // Parameters: keyword (String)
  // return: none
  // Behavior: Load task data from database with keyword search
  // Exception: if connection to database has problems, then catch the error

  private void loadTasksFromDatabase(String keyword) {
    try {
      List<TodoTask> tasks = databaseManager.loadTasksFromDatabase(keyword);
      DefaultTableModel model = (DefaultTableModel) taskTable.getModel();
      model.setRowCount(0); // Clear the current table data
      for (TodoTask task : tasks) {
        model.addRow(new Object[]{task.getTitle(), task.getDeadline(), task.getPriority(),
             task.isCompleted()});
      }
    } catch (Exception e) {

      // Handle the exception appropriately, like logging or showing an error message
      e.printStackTrace();
    }
  }
    
  // Load all task data from database
  private void loadTasksFromDatabase() {
    loadTasksFromDatabase(null);
  }

  // Method to delete selected tasks
  // parameters: none
  // return: none
  // behavior: get selected rows and delete them. Finally save the new tasks to the database
  private void deleteSelectedTasks() {
    int[] selectedRows = taskTable.getSelectedRows();
    if (selectedRows.length > 0) {
      DefaultTableModel model = (DefaultTableModel) taskTable.getModel();
      for (int i = selectedRows.length - 1; i >= 0; i--) {
        model.removeRow(selectedRows[i]);
      }
      saveTasksToDatabase(); // Save updated task data to database
    }
  }

  // save tasks to database
  // parameter: none
  // return: none
  // behavior: get every row in the panel and add them to tasks,
  //           tasks are then saved to the database and the tasks are reload
  // exception: the operation in the database may lead to problems, so we make sure
  //            all sentences related to the database are put in the "try" part

  private void saveTasksToDatabase() {
    try {
      DefaultTableModel model = (DefaultTableModel) taskTable.getModel();
      int rowCount = model.getRowCount();
      List<TodoTask> tasks = new ArrayList<>();

      for (int row = 0; row < rowCount; row++) {
        String title = (String) model.getValueAt(row, 0);
        String deadline = (String) model.getValueAt(row, 1);
        String priority = (String) model.getValueAt(row, 2);
        boolean completed = (boolean) model.getValueAt(row, 3);

        tasks.add(new TodoTask(title, deadline, priority, completed));
      }

      // Save the tasks to the database
      databaseManager.saveTasksToDatabase(tasks);

      // Reload tasks from the database after saving
      loadTasksFromDatabase();

    } catch (Exception e) {

      // Handle the exception appropriately, like logging or showing an error message
      e.printStackTrace();
    }
  }

  // Method to show "Add New" dialog
  // parameters: none
  // return: none
  // exception: if the user input an invalid date, the program will show message dialog

  private void showAddNewDialog() {

    // Create dialog
    JDialog addNewDialog = new JDialog(this, "Add New Task", true);
    addNewDialog.setLayout(new BorderLayout());

    // Create components
    JLabel titleLabel = new JLabel("Title:");
    JTextField titleField = new JTextField(20);

    JLabel deadlineLabel = new JLabel("Deadline:");
    JTextField deadlineField = new JTextField(10);

    JLabel priorityLabel = new JLabel("Priority:");
    String[] priorities = {"High", "Medium", "Low"};
    JComboBox<String> priorityComboBox = new JComboBox<>(priorities);

    final JButton addButton = new JButton("Add");
    final JButton cancelButton = new JButton("Cancel");

    // Add components to dialog
    JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
    inputPanel.add(titleLabel);
    inputPanel.add(titleField);
    inputPanel.add(deadlineLabel);
    inputPanel.add(deadlineField);
    inputPanel.add(priorityLabel);
    inputPanel.add(priorityComboBox);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add(addButton);
    buttonPanel.add(cancelButton);

    addNewDialog.add(inputPanel, BorderLayout.CENTER);
    addNewDialog.add(buttonPanel, BorderLayout.SOUTH);

    // Set action listener for "Add" button
    addButton.addActionListener(e -> {

      // Get user input for task information
      String title = titleField.getText();
      String deadline = deadlineField.getText();
      String priority = (String) priorityComboBox.getSelectedItem();

      // Add date format validation
      if (!isValidDate(deadline)) {
        JOptionPane.showMessageDialog(addNewDialog, "Invalid date format. Please use yyyy-MM-dd.", 
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      // Add new task data to task list
      DefaultTableModel model = (DefaultTableModel) taskTable.getModel();
      model.addRow(new Object[]{title, deadline, priority, false});

      // Save the tasks to the database
      saveTasksToDatabase();

      // Close the dialog
      addNewDialog.dispose();
    });

    // Set action listener for "Cancel" button
    cancelButton.addActionListener(e -> {

      // Close the dialog
      addNewDialog.dispose();
    });

    // Set dialog size and close operation
    addNewDialog.setSize(300, 150);
    addNewDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    addNewDialog.setVisible(true);
  }

  // Method to handle marking a task as complete
  private void markSelectedTaskAsComplete() {

    // Get selected task row
    int selectedRow = taskTable.getSelectedRow();

    // Ensure a task is selected
    if (selectedRow != -1) {

      // Set the value of the "Completed" column to true (task is completed)
      taskTable.setValueAt(true, selectedRow, 3);
    }
  }

  @Override 
  public void setDefaultCloseOperation(int operation) {

    // Save task data to database when window is closed
    saveTasksToDatabase();
    super.setDefaultCloseOperation(operation);
  }

  // Method for date format validation
  private boolean isValidDate(String date) {
    try {
      LocalDate.parse(date);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      TodoManagerUi todoManagerUi = new TodoManagerUi();
      todoManagerUi.setVisible(true);
    });
  }
}


    