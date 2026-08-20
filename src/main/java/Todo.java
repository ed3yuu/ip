/**
 * Represents a task without a date or time.
 */
public class Todo {
    private final String description;
    private boolean isDone;

    /**
     * Creates a to-do that is initially incomplete.
     *
     * @param description the task description
     */
    public Todo(String description) {
        this.description = description;
    }

    /** Marks this to-do as complete. */
    public void markAsDone() {
        isDone = true;
    }

    /** Marks this to-do as incomplete. */
    public void markAsNotDone() {
        isDone = false;
    }

    /**
     * Formats this to-do for display in the task list.
     *
     * @return the task type, completion status, and description
     */
    @Override
    public String toString() {
        return "[T][" + (isDone ? "X" : " ") + "] " + description;
    }
}
