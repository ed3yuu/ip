import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Starts the Lobby chatbot application.
 */
public class Lobby {
    private static final Path SAVE_FILE = Path.of("data", "lobby.txt");

    /**
     * Contains the usable tasks and any warning discovered while loading them.
     *
     * @param tasks tasks reconstructed successfully
     * @param skippedLines number of malformed non-blank lines ignored
     * @param readFailed whether the save file could not be read at all
     */
    private record LoadResult(List<Task> tasks, int skippedLines, boolean readFailed) {
    }

    /**
     * Represents the set of commands Lobby understands.
     */
    private enum Command {
        BYE,
        LIST,
        MARK,
        UNMARK,
        TODO,
        DEADLINE,
        EVENT,
        DELETE,
        UNKNOWN
    }

    /**
     * Determines which {@link Command} a user command line represents.
     * The comparison only looks at the first word, so any arguments
     * (e.g. a task number after {@code mark}) do not affect matching.
     *
     * @param command the complete command entered by the user
     * @return the matching {@link Command}, or {@code Command.UNKNOWN} if none match
     */
    private static Command parseCommand(String command) {
        String commandWord = command.split("\\s+", 2)[0];
        try {
            return Command.valueOf(commandWord.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Command.UNKNOWN;
        }
    }

    /**
     * Displays a greeting, stores tasks, lists them on request, and ends when the user enters {@code bye}.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        String banner = " _           _     _\n"
                + "| |    ___  | |__ | |__  _   _\n"
                + "| |   / _ \\ | '_ \\| '_ \\| | | |\n"
                + "| |__| (_) | |_) | |_) | |_| |\n"
                + "|_____\\___/|_.__/|_.__/ \\__, |\n"
                + "                         |___/";
        String divider = "____________________________________________________________";

        System.out.println(divider);
        System.out.println(banner);
        System.out.println("Hello! I'm Lobby.");
        System.out.println("What can I do for you?");
        System.out.println(divider);

        Scanner scanner = new Scanner(System.in);
        LoadResult loadResult = loadTasks();
        List<Task> tasks = loadResult.tasks();
        printLoadWarning(loadResult, divider);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            System.out.println(divider);

            Command commandType = parseCommand(command);
            switch (commandType) {
                case BYE:
                    printFarewell(divider);
                    return;
                case LIST:
                    System.out.println(" Here are the tasks in your list:");
                    for (int i = 0; i < tasks.size(); i++) {
                        System.out.println(" " + (i + 1) + "." + tasks.get(i));
                    }
                    break;
                case MARK: {
                    String taskNumberText = command.substring("mark".length()).trim();
                    try {
                        int taskNumber = Integer.parseInt(taskNumberText);
                        if (taskNumber < 1 || taskNumber > tasks.size()) {
                            System.out.println(" Please enter the number of a task in the list.");
                        } else {
                            int taskIndex = taskNumber - 1;
                            Task task = tasks.get(taskIndex);
                            boolean wasDone = task.isDone();
                            task.markAsDone();
                            if (!trySaveTasks(tasks)) {
                                if (!wasDone) {
                                    task.markAsNotDone();
                                }
                                break;
                            }
                            System.out.println(" Nice! I've marked this task as done:");
                            System.out.println("   " + task);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(" Please use mark followed by a task number.");
                    }
                    break;
                }
                case UNMARK: {
                    String taskNumberText = command.substring("unmark".length()).trim();
                    try {
                        int taskNumber = Integer.parseInt(taskNumberText);
                        if (taskNumber < 1 || taskNumber > tasks.size()) {
                            System.out.println(" Please enter the number of a task in the list.");
                        } else {
                            int taskIndex = taskNumber - 1;
                            Task task = tasks.get(taskIndex);
                            boolean wasDone = task.isDone();
                            task.markAsNotDone();
                            if (!trySaveTasks(tasks)) {
                                if (wasDone) {
                                    task.markAsDone();
                                }
                                break;
                            }
                            System.out.println(" OK, I've marked this task as not done yet:");
                            System.out.println("   " + task);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(" Please use unmark followed by a task number.");
                    }
                    break;
                }
                case TODO:
                    try {
                        Todo todo = createTodo(command);
                        tasks.add(todo);
                        if (!trySaveTasks(tasks)) {
                            tasks.remove(tasks.size() - 1);
                            break;
                        }
                        printTaskAdded(todo, tasks.size());
                    } catch (LobbyException e) {
                        System.out.println(" " + e.getMessage());
                    }
                    break;
                case DEADLINE:
                    try {
                        Deadline deadline = createDeadline(command);
                        tasks.add(deadline);
                        if (!trySaveTasks(tasks)) {
                            tasks.remove(tasks.size() - 1);
                            break;
                        }
                        printTaskAdded(deadline, tasks.size());
                    } catch (LobbyException e) {
                        System.out.println(" " + e.getMessage());
                    }
                    break;
                case EVENT:
                    try {
                        Event event = createEvent(command);
                        tasks.add(event);
                        if (!trySaveTasks(tasks)) {
                            tasks.remove(tasks.size() - 1);
                            break;
                        }
                        printTaskAdded(event, tasks.size());
                    } catch (LobbyException e) {
                        System.out.println(" " + e.getMessage());
                    }
                    break;
                case DELETE: {
                    String taskNumberText = command.substring("delete".length()).trim();
                    try {
                        int taskNumber = Integer.parseInt(taskNumberText);
                        if (taskNumber < 1 || taskNumber > tasks.size()) {
                            System.out.println(" Please enter the number of a task in the list.");
                        } else {
                            int taskIndex = taskNumber - 1;
                            Task removedTask = tasks.remove(taskIndex);
                            if (!trySaveTasks(tasks)) {
                                tasks.add(taskIndex, removedTask);
                                break;
                            }
                            System.out.println(" Noted. I've removed this task:");
                            System.out.println("   " + removedTask);
                            String taskWord = tasks.size() == 1 ? "task" : "tasks";
                            System.out.println(" Now you have " + tasks.size() + " " + taskWord + " in the list.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(" Please use delete followed by a task number.");
                    }
                    break;
                }
                case UNKNOWN:
                default:
                    System.out.println(" Please use todo, deadline, event, list, mark, unmark, delete, or bye.");
                    break;
            }

            System.out.println(divider);
        }
        System.out.println(divider);
        printFarewell(divider);
    }

    /**
     * Prints a warning when some or all saved tasks could not be loaded.
     *
     * @param loadResult the result of attempting to load the save file
     * @param divider the line used to separate chatbot responses
     */
    private static void printLoadWarning(LoadResult loadResult, String divider) {
        if (loadResult.readFailed()) {
            System.out.println(" I couldn't read data/lobby.txt, so I started with an empty task list.");
            System.out.println(divider);
        } else if (loadResult.skippedLines() > 0) {
            String lineWord = loadResult.skippedLines() == 1 ? "line" : "lines";
            System.out.println(" I skipped " + loadResult.skippedLines() + " invalid " + lineWord
                    + " while loading data/lobby.txt.");
            System.out.println(divider);
        }
    }

    /**
     * Prints the standard goodbye response.
     *
     * @param divider the line printed after the response
     */
    private static void printFarewell(String divider) {
        System.out.println(" Bye. Hope to see you again soon!");
        System.out.println(divider);
    }

    /**
     * Rewrites the save file so that it matches the current task list.
     * The parent directory is created automatically on the first save.
     *
     * @param tasks the complete current task list
     * @throws IOException if the directory or file cannot be written
     */
    private static void saveTasks(List<Task> tasks) throws IOException {
        Files.createDirectories(SAVE_FILE.getParent());
        List<String> taskLines = tasks.stream()
                .map(Task::toDataString)
                .toList();
        Path temporaryFile = Files.createTempFile(SAVE_FILE.getParent(), "lobby-", ".tmp");
        try {
            Files.write(temporaryFile, taskLines, StandardCharsets.UTF_8);
            try {
                Files.move(temporaryFile, SAVE_FILE,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, SAVE_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    /**
     * Attempts to save a mutation and reports a recoverable error to the user.
     *
     * @param tasks the proposed new task list
     * @return {@code true} when the save succeeded
     */
    private static boolean trySaveTasks(List<Task> tasks) {
        try {
            saveTasks(tasks);
            return true;
        } catch (IOException | SecurityException e) {
            System.out.println(" I couldn't save your changes. Please check that data/lobby.txt is writable.");
            return false;
        }
    }

    /**
     * Loads tasks from the save file, or starts with an empty list when no save file exists yet.
     *
     * @return the tasks stored during the previous run
     */
    private static LoadResult loadTasks() {
        List<Task> tasks = new ArrayList<>();
        try {
            if (!Files.exists(SAVE_FILE)) {
                return new LoadResult(tasks, 0, false);
            }

            int skippedLines = 0;
            for (String taskLine : Files.readAllLines(SAVE_FILE, StandardCharsets.UTF_8)) {
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
     * Splits a stored line while unescaping literal separators and backslashes.
     * Unescaped backslashes from older save files remain valid.
     *
     * @param taskLine one line from the save file
     * @return the unescaped fields
     */
    private static String[] parseDataFields(String taskLine) {
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
    private static Task createTaskFromData(String[] taskFields) {
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

    /**
     * Prints the confirmation shown after a new task is added.
     *
     * @param task the task that was added
     * @param taskCount the number of tasks currently stored
     */
    private static void printTaskAdded(Task task, int taskCount) {
        System.out.println(" Got it. I've added this task:");
        System.out.println("   " + task);
        String taskWord = taskCount == 1 ? "task" : "tasks";
        System.out.println(" Now you have " + taskCount + " " + taskWord + " in the list.");
    }

    /**
     * Creates a to-do from a user command after validating that it has a description.
     *
     * @param command the complete command entered by the user
     * @return the new to-do
     * @throws LobbyException if the command does not include a description
     */
    private static Todo createTodo(String command) throws LobbyException {
        String description = command.substring("todo".length()).trim();
        if (description.isEmpty()) {
            throw new LobbyException("A to-do needs a description. Try: todo <description>.");
        }
        return new Todo(description);
    }

    /**
     * Creates a deadline from a user command after validating all required parts.
     *
     * @param command the complete command entered by the user
     * @return the new deadline
     * @throws LobbyException if the command lacks a description, {@code /by}, or deadline time
     */
    private static Deadline createDeadline(String command) throws LobbyException {
        String deadlineDetails = command.substring("deadline".length()).trim();
        int byIndex = findMarker(deadlineDetails, "/by");
        if (byIndex < 0) {
            throw new LobbyException("A deadline needs a /by time. Try: deadline <description> /by <when>.");
        }

        String description = deadlineDetails.substring(0, byIndex).trim();
        String byText = deadlineDetails.substring(byIndex + "/by".length()).trim();
        if (description.isEmpty()) {
            throw new LobbyException("A deadline needs a description before /by.");
        }
        if (byText.isEmpty()) {
            throw new LobbyException("A deadline needs a time after /by.");
        }
        try {
            return new Deadline(description, LocalDate.parse(byText));
        } catch (DateTimeParseException e) {
            throw new LobbyException("Please enter the deadline date as yyyy-MM-dd, for example 2019-10-15.");
        }
    }

    /**
     * Creates an event from a user command after validating all required parts.
     *
     * @param command the complete command entered by the user
     * @return the new event
     * @throws LobbyException if the command lacks a description, {@code /from}, start time, {@code /to}, or end time
     */
    private static Event createEvent(String command) throws LobbyException {
        String eventDetails = command.substring("event".length()).trim();
        int fromIndex = findMarker(eventDetails, "/from");
        if (fromIndex < 0) {
            throw new LobbyException("An event needs a /from start time. Try: event <description> /from <start> /to <end>.");
        }

        String description = eventDetails.substring(0, fromIndex).trim();
        String timeDetails = eventDetails.substring(fromIndex + "/from".length()).trim();
        int toIndex = findMarker(timeDetails, "/to");
        if (toIndex < 0) {
            throw new LobbyException("An event needs a /to end time. Try: event <description> /from <start> /to <end>.");
        }

        String from = timeDetails.substring(0, toIndex).trim();
        String to = timeDetails.substring(toIndex + "/to".length()).trim();
        if (description.isEmpty()) {
            throw new LobbyException("An event needs a description before /from.");
        }
        if (from.isEmpty()) {
            throw new LobbyException("An event needs a start time after /from.");
        }
        if (to.isEmpty()) {
            throw new LobbyException("An event needs an end time after /to.");
        }
        return new Event(description, from, to);
    }

    /**
     * Finds a case-insensitive command marker that is surrounded by whitespace or string boundaries.
     *
     * @param text the command details to search
     * @param marker the marker to find, such as {@code /by}
     * @return the marker's starting index, or {@code -1} if it is absent
     */
    private static int findMarker(String text, String marker) {
        for (int i = 0; i <= text.length() - marker.length(); i++) {
            boolean markerMatches = text.regionMatches(true, i, marker, 0, marker.length());
            boolean validStart = i == 0 || Character.isWhitespace(text.charAt(i - 1));
            int afterMarker = i + marker.length();
            boolean validEnd = afterMarker == text.length() || Character.isWhitespace(text.charAt(afterMarker));
            if (markerMatches && validStart && validEnd) {
                return i;
            }
        }
        return -1;
    }

}
