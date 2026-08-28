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
            switch (commandType) {
                case BYE:
                    ui.showFarewell();
                    return;
                case LIST:
                    ui.showTaskList(tasks);
                    break;
                case FIND:
                    try {
                        String keyword = parser.parseFindKeyword(command);
                        ui.showMatchingTasks(tasks.find(keyword));
                    } catch (LobbyException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                case MARK: {
                    try {
                        int taskNumber = parser.parseTaskNumber(command, "mark");
                        if (!tasks.containsTaskNumber(taskNumber)) {
                            ui.showError("Please enter the number of a task in the list.");
                        } else {
                            Task task = tasks.get(taskNumber);
                            boolean wasDone = task.isDone();
                            tasks.mark(taskNumber);
                            if (!trySaveTasks()) {
                                if (!wasDone) {
                                    tasks.unmark(taskNumber);
                                }
                                break;
                            }
                            ui.showTaskMarked(task);
                        }
                    } catch (LobbyException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                }
                case UNMARK: {
                    try {
                        int taskNumber = parser.parseTaskNumber(command, "unmark");
                        if (!tasks.containsTaskNumber(taskNumber)) {
                            ui.showError("Please enter the number of a task in the list.");
                        } else {
                            Task task = tasks.get(taskNumber);
                            boolean wasDone = task.isDone();
                            tasks.unmark(taskNumber);
                            if (!trySaveTasks()) {
                                if (wasDone) {
                                    tasks.mark(taskNumber);
                                }
                                break;
                            }
                            ui.showTaskUnmarked(task);
                        }
                    } catch (LobbyException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                }
                case TODO:
                    try {
                        Todo todo = parser.parseTodo(command);
                        tasks.add(todo);
                        if (!trySaveTasks()) {
                            tasks.delete(tasks.size());
                            break;
                        }
                        ui.showTaskAdded(todo, tasks.size());
                    } catch (LobbyException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                case DEADLINE:
                    try {
                        Deadline deadline = parser.parseDeadline(command);
                        tasks.add(deadline);
                        if (!trySaveTasks()) {
                            tasks.delete(tasks.size());
                            break;
                        }
                        ui.showTaskAdded(deadline, tasks.size());
                    } catch (LobbyException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                case EVENT:
                    try {
                        Event event = parser.parseEvent(command);
                        tasks.add(event);
                        if (!trySaveTasks()) {
                            tasks.delete(tasks.size());
                            break;
                        }
                        ui.showTaskAdded(event, tasks.size());
                    } catch (LobbyException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                case DELETE: {
                    try {
                        int taskNumber = parser.parseTaskNumber(command, "delete");
                        if (!tasks.containsTaskNumber(taskNumber)) {
                            ui.showError("Please enter the number of a task in the list.");
                        } else {
                            Task removedTask = tasks.delete(taskNumber);
                            if (!trySaveTasks()) {
                                tasks.add(taskNumber, removedTask);
                                break;
                            }
                            ui.showTaskDeleted(removedTask, tasks.size());
                        }
                    } catch (LobbyException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                }
                case UNKNOWN:
                default:
                    ui.showError("Please use todo, deadline, event, list, find, mark, unmark, delete, or bye.");
                    break;
            }

            ui.endResponse();
        }
        ui.startResponse();
        ui.showFarewell();
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
            ui.showError("I couldn't save your changes. Please check that data/lobby.txt is writable.");
            return false;
        }
    }

}
