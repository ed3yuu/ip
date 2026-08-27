/**
 * Represents a task that must be completed by a given date or time string.
 */
public class Deadline extends Task {
    private final String by;

    /**
     * Creates an incomplete deadline.
     *
     * @param description the task description
     * @param by the deadline text supplied by the user
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Formats this deadline as one line in the save file.
     *
     * @return the serialized deadline
     */
    @Override
    public String toDataString() {
        return "D | " + toDataFields() + " | " + escapeDataField(by);
    }

    /**
     * Formats this deadline for display in the task list.
     *
     * @return the task type, completion status, description, and deadline
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
