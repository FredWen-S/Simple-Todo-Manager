// Jiaxuan Wen, Hui Shi, Jingyi Yu
// todo list
// 2023
// This class is a design for the structure of a todo task
// It doesn't interact with other classes

public class TodoTask {
  private String title;
  private String deadline;
  private String priority;
  private boolean completed;

  public TodoTask(String title, String deadline, String priority, boolean completed) {
    this.title = title;
    this.deadline = deadline;
    this.priority = priority;
    this.completed = completed;
  }

  // Add getter and setter methods for title, deadline, priority, and completed
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDeadline() {
    return deadline;
  }

  public void setDeadline(String deadline) {
    this.deadline = deadline;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  // You can override toString() method here for better representation of the task details
  @Override
  public String toString() {
    return "TodoTask{" 
        + "title='" + title + '\'' 
        + ", deadline='" + deadline + '\'' 
        + ", priority='" + priority + '\'' 
        + ", completed=" + completed 
        + '}';
  }
}
