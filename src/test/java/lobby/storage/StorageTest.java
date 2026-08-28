package lobby.storage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import lobby.task.Deadline;
import lobby.task.Event;
import lobby.task.Task;
import lobby.task.Todo;

/**
 * Tests saving, loading, and recovery behavior in {@link Storage}.
 */
public class StorageTest {
    @TempDir
    private Path temporaryDirectory;

    /**
     * Verifies that loading a missing save file succeeds with no tasks.
     */
    @Test
    public void load_missingFile_returnsEmptySuccessfulResult() {
        Storage storage = new Storage(temporaryDirectory.resolve("data/lobby.txt").toString());

        Storage.LoadResult result = storage.load();

        assertAll(
                () -> assertTrue(result.tasks().isEmpty()),
                () -> assertEquals(0, result.skippedLines()),
                () -> assertFalse(result.readFailed()));
    }

    /**
     * Verifies that saving and reloading preserves every task type and creates directories.
     *
     * @throws IOException if the temporary test file cannot be written or read
     */
    @Test
    public void saveAndLoad_allTaskTypes_preservesTasksAndCreatesParentDirectory()
            throws IOException {
        Path saveFile = temporaryDirectory.resolve("nested/data/lobby.txt");
        Storage storage = new Storage(saveFile.toString());
        Todo todo = new Todo("read | book \\ notes");
        todo.markAsDone();
        List<Task> tasks = List.of(
                todo,
                new Deadline("submit report", LocalDate.of(2026, 9, 30)),
                new Event("project meeting", "Monday | 2pm", "Monday \\ 3pm"));

        storage.save(tasks);
        Storage.LoadResult result = storage.load();

        assertAll(
                () -> assertTrue(Files.exists(saveFile)),
                () -> assertFalse(result.readFailed()),
                () -> assertEquals(0, result.skippedLines()),
                () -> assertEquals(
                        tasks.stream().map(Task::toDataString).toList(),
                        result.tasks().stream().map(Task::toDataString).toList()));
    }

    /**
     * Verifies that loading skips malformed records while preserving valid tasks.
     *
     * @throws IOException if the temporary test file cannot be written or read
     */
    @Test
    public void load_validAndMalformedLines_recoversValidTasksAndCountsSkippedLines()
            throws IOException {
        Path saveFile = temporaryDirectory.resolve("lobby.txt");
        Files.write(saveFile, List.of(
                "T | 0 | valid task",
                "",
                "X | 0 | unknown type",
                "T | 2 | invalid status",
                "D | 0 | missing date | ",
                "E | 1 | missing end time | 2pm"), StandardCharsets.UTF_8);
        Storage storage = new Storage(saveFile.toString());

        Storage.LoadResult result = storage.load();

        assertAll(
                () -> assertFalse(result.readFailed()),
                () -> assertEquals(4, result.skippedLines()),
                () -> assertEquals(1, result.tasks().size()),
                () -> assertEquals("T | 0 | valid task", result.tasks().get(0).toDataString()));
    }

    /**
     * Verifies that attempting to load a directory is reported as a read failure.
     */
    @Test
    public void load_pathIsDirectory_returnsReadFailure() {
        Storage storage = new Storage(temporaryDirectory.toString());

        Storage.LoadResult result = storage.load();

        assertAll(
                () -> assertTrue(result.readFailed()),
                () -> assertTrue(result.tasks().isEmpty()),
                () -> assertEquals(0, result.skippedLines()));
    }
}
