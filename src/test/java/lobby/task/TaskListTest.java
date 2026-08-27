package lobby.task;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests task collection operations and one-based task numbering in {@link TaskList}.
 */
public class TaskListTest {

    @Test
    public void constructor_sourceListChanged_taskListUnaffected() {
        List<Task> source = new ArrayList<>();
        source.add(new Todo("first"));
        TaskList taskList = new TaskList(source);

        source.add(new Todo("second"));

        assertEquals(1, taskList.size());
        assertEquals("first", taskList.get(1).getDescription());
    }

    @Test
    public void add_tasksAdded_returnsTasksInInsertionOrder() {
        TaskList taskList = new TaskList();
        Task first = new Todo("first");
        Task second = new Todo("second");

        taskList.add(first);
        taskList.add(second);

        assertAll(
                () -> assertEquals(2, taskList.size()),
                () -> assertSame(first, taskList.get(1)),
                () -> assertSame(second, taskList.get(2)));
    }

    @Test
    public void add_oneBasedPosition_insertsTaskAtPosition() {
        Task first = new Todo("first");
        Task second = new Todo("second");
        Task inserted = new Todo("inserted");
        TaskList taskList = new TaskList(List.of(first, second));

        taskList.add(2, inserted);

        assertIterableEquals(List.of(first, inserted, second), taskList.asList());
    }

    @Test
    public void delete_validTaskNumber_removesAndReturnsTask() {
        Task first = new Todo("first");
        Task second = new Todo("second");
        TaskList taskList = new TaskList(List.of(first, second));

        Task deleted = taskList.delete(1);

        assertAll(
                () -> assertSame(first, deleted),
                () -> assertEquals(1, taskList.size()),
                () -> assertSame(second, taskList.get(1)));
    }

    @Test
    public void markAndUnmark_validTaskNumber_updatesAndReturnsTask() {
        Task task = new Todo("read book");
        TaskList taskList = new TaskList(List.of(task));

        Task markedTask = taskList.mark(1);

        assertSame(task, markedTask);
        assertTrue(task.isDone());

        Task unmarkedTask = taskList.unmark(1);

        assertSame(task, unmarkedTask);
        assertFalse(task.isDone());
    }

    @Test
    public void containsTaskNumber_boundaries_returnsExpectedResult() {
        TaskList taskList = new TaskList(List.of(new Todo("first"), new Todo("second")));

        assertAll(
                () -> assertFalse(taskList.containsTaskNumber(-1)),
                () -> assertFalse(taskList.containsTaskNumber(0)),
                () -> assertTrue(taskList.containsTaskNumber(1)),
                () -> assertTrue(taskList.containsTaskNumber(2)),
                () -> assertFalse(taskList.containsTaskNumber(3)));
    }

    @Test
    public void asList_listModified_snapshotAndTaskListUnaffected() {
        Task first = new Todo("first");
        TaskList taskList = new TaskList(List.of(first));
        List<Task> snapshot = taskList.asList();

        taskList.add(new Todo("second"));

        assertAll(
                () -> assertEquals(List.of(first), snapshot),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> snapshot.add(new Todo("third"))),
                () -> assertEquals(2, taskList.size()));
    }

    @Test
    public void taskOperations_invalidTaskNumber_exceptionThrown() {
        TaskList taskList = new TaskList(List.of(new Todo("only task")));

        assertAll(
                () -> assertThrows(IndexOutOfBoundsException.class, () -> taskList.get(0)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> taskList.get(2)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> taskList.delete(0)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> taskList.mark(2)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> taskList.unmark(0)));
    }
}
