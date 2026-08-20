/**
 * Represents a task that must be completed by a given date or time string.
 */
public class Deadline {
    private final String description;
    private final String by;
    private boolean isDone;

    /**
     * Creates an incomplete deadline.
     *
     * @param description the task description
     * @param by the deadline text supplied by the user
     */
    public Deadline(String description, String by) {
        this.description = description;
        this.by = by;
    }

    /** Marks this deadline as complete. */
    public void markAsDone() {
        isDone = true;
    }

    /** Marks this deadline as incomplete. */
    public void markAsNotDone() {
        isDone = false;
    }

    /**
     * Formats this deadline for display in the task list.
     *
     * @return the task type, completion status, description, and deadline
     */
    @Override
    public String toString() {
        return "[D][" + (isDone ? "X" : " ") + "] " + description + " (by: " + by + ")";
    }
}
