import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

/**
 * Starts the Lobby chatbot application.
 */
public class Lobby {
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
        Ui ui = new Ui();
        Storage storage = new Storage("data/lobby.txt");
        ui.showWelcome();
        Storage.LoadResult loadResult = storage.load();
        List<Task> tasks = loadResult.tasks();
        showLoadWarning(loadResult, ui);
        String command;
        while ((command = ui.readCommand()) != null) {
            ui.startResponse();

            Command commandType = parseCommand(command);
            switch (commandType) {
                case BYE:
                    ui.showFarewell();
                    return;
                case LIST:
                    ui.showTaskList(tasks);
                    break;
                case MARK: {
                    String taskNumberText = command.substring("mark".length()).trim();
                    try {
                        int taskNumber = Integer.parseInt(taskNumberText);
                        if (taskNumber < 1 || taskNumber > tasks.size()) {
                            ui.showError("Please enter the number of a task in the list.");
                        } else {
                            int taskIndex = taskNumber - 1;
                            Task task = tasks.get(taskIndex);
                            boolean wasDone = task.isDone();
                            task.markAsDone();
                            if (!trySaveTasks(tasks, storage, ui)) {
                                if (!wasDone) {
                                    task.markAsNotDone();
                                }
                                break;
                            }
                            ui.showTaskMarked(task);
                        }
                    } catch (NumberFormatException e) {
                        ui.showError("Please use mark followed by a task number.");
                    }
                    break;
                }
                case UNMARK: {
                    String taskNumberText = command.substring("unmark".length()).trim();
                    try {
                        int taskNumber = Integer.parseInt(taskNumberText);
                        if (taskNumber < 1 || taskNumber > tasks.size()) {
                            ui.showError("Please enter the number of a task in the list.");
                        } else {
                            int taskIndex = taskNumber - 1;
                            Task task = tasks.get(taskIndex);
                            boolean wasDone = task.isDone();
                            task.markAsNotDone();
                            if (!trySaveTasks(tasks, storage, ui)) {
                                if (wasDone) {
                                    task.markAsDone();
                                }
                                break;
                            }
                            ui.showTaskUnmarked(task);
                        }
                    } catch (NumberFormatException e) {
                        ui.showError("Please use unmark followed by a task number.");
                    }
                    break;
                }
                case TODO:
                    try {
                        Todo todo = createTodo(command);
                        tasks.add(todo);
                        if (!trySaveTasks(tasks, storage, ui)) {
                            tasks.remove(tasks.size() - 1);
                            break;
                        }
                        ui.showTaskAdded(todo, tasks.size());
                    } catch (LobbyException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                case DEADLINE:
                    try {
                        Deadline deadline = createDeadline(command);
                        tasks.add(deadline);
                        if (!trySaveTasks(tasks, storage, ui)) {
                            tasks.remove(tasks.size() - 1);
                            break;
                        }
                        ui.showTaskAdded(deadline, tasks.size());
                    } catch (LobbyException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                case EVENT:
                    try {
                        Event event = createEvent(command);
                        tasks.add(event);
                        if (!trySaveTasks(tasks, storage, ui)) {
                            tasks.remove(tasks.size() - 1);
                            break;
                        }
                        ui.showTaskAdded(event, tasks.size());
                    } catch (LobbyException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                case DELETE: {
                    String taskNumberText = command.substring("delete".length()).trim();
                    try {
                        int taskNumber = Integer.parseInt(taskNumberText);
                        if (taskNumber < 1 || taskNumber > tasks.size()) {
                            ui.showError("Please enter the number of a task in the list.");
                        } else {
                            int taskIndex = taskNumber - 1;
                            Task removedTask = tasks.remove(taskIndex);
                            if (!trySaveTasks(tasks, storage, ui)) {
                                tasks.add(taskIndex, removedTask);
                                break;
                            }
                            ui.showTaskDeleted(removedTask, tasks.size());
                        }
                    } catch (NumberFormatException e) {
                        ui.showError("Please use delete followed by a task number.");
                    }
                    break;
                }
                case UNKNOWN:
                default:
                    ui.showError("Please use todo, deadline, event, list, mark, unmark, delete, or bye.");
                    break;
            }

            ui.endResponse();
        }
        ui.startResponse();
        ui.showFarewell();
    }

    /**
     * Prints a warning when some or all saved tasks could not be loaded.
     *
     * @param loadResult the result of attempting to load the save file
     * @param ui the console UI used to show the warning
     */
    private static void showLoadWarning(Storage.LoadResult loadResult, Ui ui) {
        if (loadResult.readFailed()) {
            ui.showLoadingError();
        } else if (loadResult.skippedLines() > 0) {
            ui.showSkippedLinesWarning(loadResult.skippedLines());
        }
    }

    /**
     * Attempts to save a mutation and reports a recoverable error to the user.
     *
     * @param tasks the proposed new task list
     * @param storage storage used to persist the tasks
     * @param ui the console UI used to report a failure
     * @return {@code true} when the save succeeded
     */
    private static boolean trySaveTasks(List<Task> tasks, Storage storage, Ui ui) {
        try {
            storage.save(tasks);
            return true;
        } catch (IOException | SecurityException e) {
            ui.showError("I couldn't save your changes. Please check that data/lobby.txt is writable.");
            return false;
        }
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
