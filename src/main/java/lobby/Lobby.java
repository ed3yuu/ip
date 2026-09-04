package lobby;

import java.io.IOException;

import lobby.exception.LobbyException;
import lobby.parser.Parser;
import lobby.storage.Storage;
import lobby.task.Deadline;
import lobby.task.Event;
import lobby.task.Task;
import lobby.task.TaskList;
import lobby.task.Todo;
import lobby.ui.ResponseFormatter;
import lobby.ui.Ui;

/**
 * Starts the Lobby chatbot application.
 */
public class Lobby {
    private final Ui ui;
    private final Storage storage;
    private final Parser parser;
    private final TaskList tasks;
    private final Storage.LoadResult loadResult;
    private final ResponseFormatter responseFormatter;

    /**
     * Creates a Lobby application backed by the given task file.
     *
     * @param filePath location used to load and save tasks.
     */
    public Lobby(String filePath) {
        this.ui = new Ui();
        this.storage = new Storage(filePath);
        this.parser = new Parser();
        this.loadResult = storage.load();
        this.tasks = new TaskList(loadResult.tasks());
        this.responseFormatter = new ResponseFormatter();
    }

    /**
     * Runs the command loop until the user exits or the input stream ends.
     */
    public void run() {
        ui.showWelcome();
        showLoadWarning();
        String command;
        while ((command = ui.readCommand()) != null) {
            ui.startResponse();
            Parser.Command commandType = parser.parseCommand(command);
            ui.showResponse(getResponse(command));
            ui.endResponse();
            if (commandType == Parser.Command.BYE) {
                return;
            }
        }
        ui.startResponse();
        ui.showFarewell();
    }

    /**
     * Processes one user command and returns Lobby's response.
     *
     * @param command complete command entered by the user.
     * @return Lobby's response to the command
     */
    public String getResponse(String command) {
        String normalizedCommand = command.trim();
        return switch (parser.parseCommand(normalizedCommand)) {
            case BYE -> " Bye. Hope to see you again soon!";
            case LIST -> responseFormatter.formatTaskList(" Here are the tasks in your list:", tasks);
            case FIND -> handleFind(normalizedCommand);
            case MARK -> handleTaskCompletion(normalizedCommand, true);
            case UNMARK -> handleTaskCompletion(normalizedCommand, false);
            case TODO -> handleTodo(normalizedCommand);
            case DEADLINE -> handleDeadline(normalizedCommand);
            case EVENT -> handleEvent(normalizedCommand);
            case DELETE -> handleDelete(normalizedCommand);
            case UNKNOWN -> " Please use todo, deadline, event, list, find, mark, unmark, delete, or bye.";
        };
    }

    /**
     * Returns the greeting and any warning produced while loading saved tasks.
     *
     * @return startup message for the graphical UI
     */
    public String getStartupMessage() {
        String greeting = "Hello! I'm Lobby." + System.lineSeparator() + "What can I do for you?";
        if (loadResult.readFailed()) {
            return greeting + System.lineSeparator()
                    + "I couldn't read data/lobby.txt, so I started with an empty task list.";
        }
        if (loadResult.skippedLines() > 0) {
            String lineWord = loadResult.skippedLines() == 1 ? "line" : "lines";
            return greeting + System.lineSeparator() + "I skipped " + loadResult.skippedLines()
                    + " invalid " + lineWord + " while loading data/lobby.txt.";
        }
        return greeting;
    }

    /**
     * Starts Lobby using its default task file.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        new Lobby("data/lobby.txt").run();
    }

    /**
     * Prints a warning when some or all saved tasks could not be loaded.
     */
    private void showLoadWarning() {
        if (loadResult.readFailed()) {
            ui.showLoadingError();
        } else if (loadResult.skippedLines() > 0) {
            ui.showSkippedLinesWarning(loadResult.skippedLines());
        }
    }

    /**
     * Attempts to save a mutation and reports a recoverable error to the user.
     *
     * @return {@code true} when the save succeeded
     */
    private boolean trySaveTasks() {
        try {
            storage.save(tasks.asList());
            return true;
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    private String handleFind(String command) {
        try {
            String keyword = parser.parseFindKeyword(command);
            return responseFormatter.formatTaskList(" Here are the matching tasks in your list:", tasks.find(keyword));
        } catch (LobbyException e) {
            return " " + e.getMessage();
        }
    }

    private String handleTaskCompletion(String command, boolean isMarkingDone) {
        String commandWord = isMarkingDone ? "mark" : "unmark";
        try {
            int taskNumber = parser.parseTaskNumber(command, commandWord);
            if (!tasks.containsTaskNumber(taskNumber)) {
                return " Please enter the number of a task in the list.";
            }

            Task task = tasks.get(taskNumber);
            boolean wasDone = task.isDone();
            if (isMarkingDone) {
                tasks.mark(taskNumber);
            } else {
                tasks.unmark(taskNumber);
            }
            if (!trySaveTasks()) {
                restoreCompletion(taskNumber, wasDone);
                return getSaveErrorMessage();
            }
            return isMarkingDone
                    ? responseFormatter.formatTaskMarked(task)
                    : responseFormatter.formatTaskUnmarked(task);
        } catch (LobbyException e) {
            return " " + e.getMessage();
        }
    }

    private void restoreCompletion(int taskNumber, boolean wasDone) {
        if (wasDone) {
            tasks.mark(taskNumber);
        } else {
            tasks.unmark(taskNumber);
        }
    }

    private String handleTodo(String command) {
        try {
            Todo todo = parser.parseTodo(command);
            return addTask(todo);
        } catch (LobbyException e) {
            return " " + e.getMessage();
        }
    }

    private String handleDeadline(String command) {
        try {
            Deadline deadline = parser.parseDeadline(command);
            return addTask(deadline);
        } catch (LobbyException e) {
            return " " + e.getMessage();
        }
    }

    private String handleEvent(String command) {
        try {
            Event event = parser.parseEvent(command);
            return addTask(event);
        } catch (LobbyException e) {
            return " " + e.getMessage();
        }
    }

    private String addTask(Task task) {
        tasks.add(task);
        if (!trySaveTasks()) {
            tasks.delete(tasks.size());
            return getSaveErrorMessage();
        }
        return responseFormatter.formatTaskAdded(task, tasks.size());
    }

    private String handleDelete(String command) {
        try {
            int taskNumber = parser.parseTaskNumber(command, "delete");
            if (!tasks.containsTaskNumber(taskNumber)) {
                return " Please enter the number of a task in the list.";
            }

            Task removedTask = tasks.delete(taskNumber);
            if (!trySaveTasks()) {
                tasks.add(taskNumber, removedTask);
                return getSaveErrorMessage();
            }
            return responseFormatter.formatTaskDeleted(removedTask, tasks.size());
        } catch (LobbyException e) {
            return " " + e.getMessage();
        }
    }

    private String getSaveErrorMessage() {
        return " I couldn't save your changes. Please check that data/lobby.txt is writable.";
    }

}
