import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads tasks from a file and saves the current task list back to that file.
 */
public class Storage {
    private final Path filePath;

    /**
     * Contains the usable tasks and any warning discovered while loading them.
     *
     * @param tasks tasks reconstructed successfully
     * @param skippedLines number of malformed non-blank lines ignored
     * @param readFailed whether the save file could not be read at all
     */
    public record LoadResult(List<Task> tasks, int skippedLines, boolean readFailed) {
    }

    /**
     * Creates storage that reads from and writes to the given path.
     *
     * @param filePath location of the task data file
     */
    public Storage(String filePath) {
        this.filePath = Path.of(filePath);
    }

    /**
     * Loads tasks from the save file, or returns an empty list when no save file exists yet.
     * Malformed records are skipped so that valid records can still be recovered.
     *
     * @return the loaded tasks and details of any loading problem
     */
    public LoadResult load() {
        List<Task> tasks = new ArrayList<>();
        try {
            if (!Files.exists(filePath)) {
                return new LoadResult(tasks, 0, false);
            }

            int skippedLines = 0;
            for (String taskLine : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
                if (taskLine.isBlank()) {
                    continue;
                }
                try {
                    tasks.add(createTaskFromData(parseDataFields(taskLine)));
                } catch (IllegalArgumentException e) {
                    skippedLines++;
                }
            }
            return new LoadResult(tasks, skippedLines, false);
        } catch (IOException | SecurityException e) {
            return new LoadResult(new ArrayList<>(), 0, true);
        }
    }

    /**
     * Rewrites the save file so that it matches the current task list.
     * The parent directory is created automatically on the first save.
     *
     * @param tasks the complete current task list
     * @throws IOException if the directory or file cannot be written
     */
    public void save(List<Task> tasks) throws IOException {
        Files.createDirectories(filePath.getParent());
        List<String> taskLines = tasks.stream()
                .map(Task::toDataString)
                .toList();
        Path temporaryFile = Files.createTempFile(filePath.getParent(), "lobby-", ".tmp");
        try {
            Files.write(temporaryFile, taskLines, StandardCharsets.UTF_8);
            try {
                Files.move(temporaryFile, filePath,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    /**
     * Splits a stored line while unescaping literal separators and backslashes.
     * Unescaped backslashes from older save files remain valid.
     *
     * @param taskLine one line from the save file
     * @return the unescaped fields
     */
    private String[] parseDataFields(String taskLine) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        for (int i = 0; i < taskLine.length(); i++) {
            char current = taskLine.charAt(i);
            if (current == '\\' && i + 1 < taskLine.length()) {
                char next = taskLine.charAt(i + 1);
                if (next == '\\' || next == '|') {
                    field.append(next);
                    i++;
                    continue;
                }
            }
            if (current == '|') {
                fields.add(field.toString().strip());
                field.setLength(0);
            } else {
                field.append(current);
            }
        }
        fields.add(field.toString().strip());
        return fields.toArray(String[]::new);
    }

    /**
     * Reconstructs one task from the fields stored on a line of the save file.
     *
     * @param taskFields the task type, completion flag, and task-specific fields
     * @return the reconstructed task
     */
    private Task createTaskFromData(String[] taskFields) {
        if (taskFields.length < 2 || (!taskFields[1].equals("0") && !taskFields[1].equals("1"))) {
            throw new IllegalArgumentException("Invalid task status");
        }

        int expectedFieldCount = switch (taskFields[0]) {
            case "T" -> 3;
            case "D" -> 4;
            case "E" -> 5;
            default -> throw new IllegalArgumentException("Unknown task type");
        };
        if (taskFields.length != expectedFieldCount) {
            throw new IllegalArgumentException("Incorrect number of task fields");
        }
        for (int i = 2; i < taskFields.length; i++) {
            if (taskFields[i].isBlank()) {
                throw new IllegalArgumentException("Task fields cannot be blank");
            }
        }

        Task task = switch (taskFields[0]) {
            case "T" -> new Todo(taskFields[2]);
            case "D" -> new Deadline(taskFields[2], LocalDate.parse(taskFields[3]));
            case "E" -> new Event(taskFields[2], taskFields[3], taskFields[4]);
            default -> throw new IllegalArgumentException("Unknown task type");
        };
        if (taskFields[1].equals("1")) {
            task.markAsDone();
        }
        return task;
    }
}
