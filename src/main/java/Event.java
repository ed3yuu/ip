/**
 * Represents a task that has user-supplied start and end date/time strings.
 */
public class Event {
    private final String description;
    private final String from;
    private final String to;
    private boolean isDone;

    /**
     * Creates an incomplete event.
     *
     * @param description the event description
     * @param from the event start text supplied by the user
     * @param to the event end text supplied by the user
     */
    public Event(String description, String from, String to) {
        this.description = description;
        this.from = from;
        this.to = to;
    }

    /** Marks this event as complete. */
    public void markAsDone() {
        isDone = true;
    }

    /** Marks this event as incomplete. */
    public void markAsNotDone() {
        isDone = false;
    }

    /**
     * Formats this event for display in the task list.
     *
     * @return the task type, completion status, description, start, and end
     */
    @Override
    public String toString() {
        return "[E][" + (isDone ? "X" : " ") + "] " + description
                + " (from: " + from + " to: " + to + ")";
    }
}
