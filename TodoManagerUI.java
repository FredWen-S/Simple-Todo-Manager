import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;

public class TodoManagerUI extends JFrame {
    private final JTable taskTable;
    private final JTextField searchField;

    // 数据库连接相关的信息
    private static final String DB_URL = "jdbc:mysql://localhost:3306/todo";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456";

    public TodoManagerUI() {
        // 设置主界面的标题
        setTitle("Todo Manager");

        // 创建任务列表的表头和数据（添加"Completed"列）
        String[] columnNames = {"Title", "Deadline", "Priority", "Completed"};
        Object[][] data = {
                {"Task 1", "2023-07-31", "High", false},
                {"Task 2", "2023-08-15", "Medium", false},
                // 添加更多任务数据...
        };

        // 创建任务列表的JTable
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            // 设置"Completed"列为Boolean类型，以便在表格中显示复选框
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 3 ? Boolean.class : super.getColumnClass(columnIndex);
            }

            // 禁止编辑任务标题、截止日期和优先级列
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && column != 1 && column != 2;
            }
        };
        taskTable = new JTable(tableModel);

        // 创建滚动面板，用于显示任务列表
        JScrollPane scrollPane = new JScrollPane(taskTable);

        // 创建"Add New"按钮
        JButton addNewButton = new JButton("Add New");

        // 创建"Mark as Complete"按钮
        JButton markAsCompleteButton = new JButton("Mark as Complete");

        // 创建搜索文本框和搜索按钮
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        // 创建"Delete"按钮
        JButton deleteButton = new JButton("Delete Selected");

        // 添加"Add New"按钮点击事件监听器
        addNewButton.addActionListener(e -> {
            // 弹出"Add New"对话框
            showAddNewDialog();
        });

        // 添加"Mark as Complete"按钮点击事件监听器
        markAsCompleteButton.addActionListener(e -> {
            // 处理标记任务为已完成的逻辑
            markSelectedTaskAsComplete();
        });

        // 添加"Search"按钮点击事件监听器
        searchButton.addActionListener(e -> {
            // 获取搜索关键词
            String keyword = searchField.getText();
            // 加载带有关键词搜索的任务数据
            loadTasksFromDatabase(keyword);
        });

        // 添加"Delete"按钮点击事件监听器
        deleteButton.addActionListener(e -> {
            // 处理删除选中任务的逻辑
            deleteSelectedTasks();
        });

        // 创建搜索面板，并设置布局为FlowLayout
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // 创建操作按钮面板，并设置布局为FlowLayout
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addNewButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(markAsCompleteButton);

        // 创建主面板，并设置布局为BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 将主面板添加到主窗口
        add(mainPanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveTasksToDatabase();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(this::saveTasksToDatabase));

        // 设置主窗口大小和关闭操作
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 从数据库加载任务数据
        loadTasksFromDatabase();
    }

    // ... （showAddNewDialog() 和 markSelectedTaskAsComplete() 方法不变）

    // 从数据库加载任务数据，增加关键词搜索功能
    private void loadTasksFromDatabase(String keyword) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM tasks";
            if (keyword != null && !keyword.trim().isEmpty()) {
                query += " WHERE title LIKE ?";
            }
            PreparedStatement statement = conn.prepareStatement(query);
            if (keyword != null && !keyword.trim().isEmpty()) {
                statement.setString(1, "%" + keyword + "%");
            }
            ResultSet resultSet = statement.executeQuery();

            DefaultTableModel model = (DefaultTableModel) taskTable.getModel();
            model.setRowCount(0); // 清空当前表格数据

            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String deadline = resultSet.getString("deadline");
                String priority = resultSet.getString("priority");
                boolean completed = resultSet.getBoolean("completed");

                model.addRow(new Object[]{title, deadline, priority, completed});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load tasks from the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 从数据库加载任务数据，加载所有任务
    private void loadTasksFromDatabase() {
        loadTasksFromDatabase(null);
    }

    // 删除选中任务的方法
    private void deleteSelectedTasks() {
        int[] selectedRows = taskTable.getSelectedRows();
        if (selectedRows.length > 0) {
            DefaultTableModel model = (DefaultTableModel) taskTable.getModel();
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                model.removeRow(selectedRows[i]);
            }
            saveTasksToDatabase(); // 保存更新后的任务数据到数据库
        }
    }

    private void saveTasksToDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 清空数据库中的数据
            String clearQuery = "DELETE FROM tasks";
            Statement clearStatement = conn.createStatement();
            clearStatement.executeUpdate(clearQuery);

            // 将任务数据插入数据库
            String insertQuery = "INSERT INTO tasks (title, deadline, priority, completed) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE deadline = VALUES(deadline), priority = VALUES(priority), completed = VALUES(completed)";
            PreparedStatement statement = conn.prepareStatement(insertQuery);

            DefaultTableModel model = (DefaultTableModel) taskTable.getModel();

            for (int row = 0; row < model.getRowCount(); row++) {
                String title = (String) model.getValueAt(row, 0);
                String deadline = (String) model.getValueAt(row, 1);
                String priority = (String) model.getValueAt(row, 2);
                boolean completed = (boolean) model.getValueAt(row, 3);

                statement.setString(1, title);
                statement.setString(2, deadline);
                statement.setString(3, priority);
                statement.setBoolean(4, completed);

                statement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save tasks to the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // 弹出"Add New"对话框的方法
    private void showAddNewDialog() {
        // 创建对话框
        JDialog addNewDialog = new JDialog(this, "Add New Task", true);
        addNewDialog.setLayout(new BorderLayout());

        // 创建组件
        JLabel titleLabel = new JLabel("Title:");
        JTextField titleField = new JTextField(20);

        JLabel deadlineLabel = new JLabel("Deadline:");
        JTextField deadlineField = new JTextField(10);

        JLabel priorityLabel = new JLabel("Priority:");
        String[] priorities = {"High", "Medium", "Low"};
        JComboBox<String> priorityComboBox = new JComboBox<>(priorities);

        JButton addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");

        // 添加组件到对话框
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

        // 设置"Add"按钮点击事件监听器
        addButton.addActionListener(e -> {
            // 获取用户输入的任务信息
            String title = titleField.getText();
            String deadline = deadlineField.getText();
            String priority = (String) priorityComboBox.getSelectedItem();

            // 添加日期格式验证
            if (!isValidDate(deadline)) {
                JOptionPane.showMessageDialog(addNewDialog, "Invalid date format. Please use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 在任务列表中添加新任务数据
            DefaultTableModel model = (DefaultTableModel) taskTable.getModel();
            model.addRow(new Object[]{title, deadline, priority, false});

            // Save the tasks to the database
            saveTasksToDatabase();

            // 关闭对话框
            addNewDialog.dispose();
        });

        // 设置"Cancel"按钮点击事件监听器
        cancelButton.addActionListener(e -> {
            // 关闭对话框
            addNewDialog.dispose();
        });

        // 设置对话框大小和关闭操作
        addNewDialog.setSize(300, 150);
        addNewDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addNewDialog.setVisible(true);
    }

// ...

    // 处理标记任务为已完成的方法
    private void markSelectedTaskAsComplete() {
        // 获取选中的任务行
        int selectedRow = taskTable.getSelectedRow();

        // 确保有任务被选中
        if (selectedRow != -1) {
            // 将"Completed"列的值设置为true（任务已完成）
            taskTable.setValueAt(true, selectedRow, 3);
        }
    }


    @Override
    public void setDefaultCloseOperation(int operation) {
        // 在窗口关闭时，保存任务数据到数据库
        saveTasksToDatabase();
        super.setDefaultCloseOperation(operation);
    }

    // 日期格式验证方法
    private boolean isValidDate(String date) {
        String regex = "\\d{4}-\\d{2}-\\d{2}";
        return date.matches(regex);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 创建并显示主界面
            TodoManagerUI todoManagerUI = new TodoManagerUI();
            todoManagerUI.setVisible(true);
        });
    }
}
