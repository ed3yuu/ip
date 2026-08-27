import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Starts the Lobby chatbot application.
 */
public class Lobby {
    private static final Path SAVE_FILE = Path.of("data", "lobby.txt");

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
            return Command.valueOf(commandWord.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Command.UNKNOWN;
        }
    }

    /**
     * Displays a greeting, stores tasks, lists them on request, and ends when the user enters {@code bye}.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) throws IOException {
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
        List<Task> tasks = loadTasks();
        while (true) {
            String command = scanner.nextLine();
            System.out.println(divider);

            Command commandType = parseCommand(command);
            switch (commandType) {
                case BYE:
                    System.out.println(" Bye. Hope to see you again soon!");
                    System.out.println(divider);
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
                            tasks.get(taskIndex).markAsDone();
                            saveTasks(tasks);
                            System.out.println(" Nice! I've marked this task as done:");
                            System.out.println("   " + tasks.get(taskIndex));
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
                            tasks.get(taskIndex).markAsNotDone();
                            saveTasks(tasks);
                            System.out.println(" OK, I've marked this task as not done yet:");
                            System.out.println("   " + tasks.get(taskIndex));
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
                        saveTasks(tasks);
                        printTaskAdded(todo, tasks.size());
                    } catch (LobbyException e) {
                        System.out.println(" " + e.getMessage());
                    }
                    break;
                case DEADLINE:
                    try {
                        Deadline deadline = createDeadline(command);
                        tasks.add(deadline);
                        saveTasks(tasks);
                        printTaskAdded(deadline, tasks.size());
                    } catch (LobbyException e) {
                        System.out.println(" " + e.getMessage());
                    }
                    break;
                case EVENT:
                    try {
                        Event event = createEvent(command);
                        tasks.add(event);
                        saveTasks(tasks);
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
                            saveTasks(tasks);
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
        Files.write(SAVE_FILE, taskLines, StandardCharsets.UTF_8);
    }

    /**
     * Loads tasks from the save file, or starts with an empty list when no save file exists yet.
     *
     * @return the tasks stored during the previous run
     * @throws IOException if the save file cannot be read
     */
    private static List<Task> loadTasks() throws IOException {
        List<Task> tasks = new ArrayList<>();
        if (!Files.exists(SAVE_FILE)) {
            return tasks;
        }

        for (String taskLine : Files.readAllLines(SAVE_FILE, StandardCharsets.UTF_8)) {
            String[] taskFields = taskLine.split("\\s*\\|\\s*", -1);
            Task task = createTaskFromData(taskFields);
            if (taskFields[1].equals("1")) {
                task.markAsDone();
            }
            tasks.add(task);
        }
        return tasks;
    }

    /**
     * Reconstructs one task from the fields stored on a line of the save file.
     *
     * @param taskFields the task type, completion flag, and task-specific fields
     * @return the reconstructed task
     */
    private static Task createTaskFromData(String[] taskFields) {
        return switch (taskFields[0]) {
            case "T" -> new Todo(taskFields[2]);
            case "D" -> new Deadline(taskFields[2], taskFields[3]);
            case "E" -> new Event(taskFields[2], taskFields[3], taskFields[4]);
            default -> throw new IllegalArgumentException("Unknown task type: " + taskFields[0]);
        };
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
        String[] deadlineParts = command.substring("deadline".length()).split("\\s+/by\\s*", -1);
        if (deadlineParts.length < 2) {
            throw new LobbyException("A deadline needs a /by time. Try: deadline <description> /by <when>.");
        }

        String description = deadlineParts[0].trim();
        String by = deadlineParts[1].trim();
        if (description.isEmpty()) {
            throw new LobbyException("A deadline needs a description before /by.");
        }
        if (by.isEmpty()) {
            throw new LobbyException("A deadline needs a time after /by.");
        }
        return new Deadline(description, by);
    }

    /**
     * Creates an event from a user command after validating all required parts.
     *
     * @param command the complete command entered by the user
     * @return the new event
     * @throws LobbyException if the command lacks a description, {@code /from}, start time, {@code /to}, or end time
     */
    private static Event createEvent(String command) throws LobbyException {
        String eventDetails = command.substring("event".length());
        String[] fromParts = eventDetails.split("\\s+/from\\s*", 2);
        if (fromParts.length < 2) {
            throw new LobbyException("An event needs a /from start time. Try: event <description> /from <start> /to <end>.");
        }

        String[] toParts = fromParts[1].split("\\s+/to\\s*", -1);
        if (toParts.length < 2) {
            throw new LobbyException("An event needs a /to end time. Try: event <description> /from <start> /to <end>.");
        }

        String description = fromParts[0].trim();
        String from = toParts[0].trim();
        String to = toParts[1].trim();
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

}
