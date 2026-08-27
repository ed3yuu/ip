import java.util.ArrayList;
import java.util.List;

/**
 * Owns the in-memory task collection and provides operations on task numbers.
 */
public class TaskList {
    private final List<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list containing the supplied tasks.
     * A defensive copy prevents outside code from changing the collection directly.
     *
     * @param tasks initial tasks
     */
    public TaskList(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task task to add
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Inserts a task at a one-based position.
     * This supports restoring a deleted task if persistence fails.
     *
     * @param taskNumber one-based position at which to insert the task
     * @param task task to insert
     */
    public void add(int taskNumber, Task task) {
        tasks.add(toIndex(taskNumber), task);
    }

    /**
     * Returns the task with the given one-based task number.
     *
     * @param taskNumber one-based task number
     * @return the selected task
     */
    public Task get(int taskNumber) {
        return tasks.get(toIndex(taskNumber));
    }

    /**
     * Removes and returns the task with the given one-based task number.
     *
     * @param taskNumber one-based task number
     * @return the removed task
     */
    public Task delete(int taskNumber) {
        return tasks.remove(toIndex(taskNumber));
    }

    /**
     * Marks a task as completed and returns it for display.
     *
     * @param taskNumber one-based task number
     * @return the updated task
     */
    public Task mark(int taskNumber) {
        Task task = get(taskNumber);
        task.markAsDone();
        return task;
    }

    /**
     * Marks a task as incomplete and returns it for display.
     *
     * @param taskNumber one-based task number
     * @return the updated task
     */
    public Task unmark(int taskNumber) {
        Task task = get(taskNumber);
        task.markAsNotDone();
        return task;
    }

    /**
     * Checks whether a one-based task number identifies a task in this list.
     *
     * @param taskNumber one-based task number to check
     * @return {@code true} when the task number is valid
     */
    public boolean containsTaskNumber(int taskNumber) {
        return taskNumber >= 1 && taskNumber <= tasks.size();
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return current task count
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns an immutable snapshot for display or persistence.
     *
     * @return snapshot of the tasks in their current order
     */
    public List<Task> asList() {
        return List.copyOf(tasks);
    }

    /**
     * Converts a one-based task number to its zero-based list index.
     *
     * @param taskNumber one-based task number
     * @return zero-based index
     */
    private int toIndex(int taskNumber) {
        return taskNumber - 1;
    }
}
