import java.io.IOException;

/**
 * Starts the Lobby chatbot application.
 */
public class Lobby {
    /**
     * Displays a greeting, stores tasks, lists them on request, and ends when the user enters {@code bye}.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        Storage storage = new Storage("data/lobby.txt");
        Parser parser = new Parser();
        ui.showWelcome();
        Storage.LoadResult loadResult = storage.load();
        TaskList tasks = new TaskList(loadResult.tasks());
        showLoadWarning(loadResult, ui);
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
                case MARK: {
                    try {
                        int taskNumber = parser.parseTaskNumber(command, "mark");
                        if (!tasks.containsTaskNumber(taskNumber)) {
                            ui.showError("Please enter the number of a task in the list.");
                        } else {
                            Task task = tasks.get(taskNumber);
                            boolean wasDone = task.isDone();
                            tasks.mark(taskNumber);
                            if (!trySaveTasks(tasks, storage, ui)) {
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
                            if (!trySaveTasks(tasks, storage, ui)) {
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
                        if (!trySaveTasks(tasks, storage, ui)) {
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
                        if (!trySaveTasks(tasks, storage, ui)) {
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
                        if (!trySaveTasks(tasks, storage, ui)) {
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
                            if (!trySaveTasks(tasks, storage, ui)) {
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
    private static boolean trySaveTasks(TaskList tasks, Storage storage, Ui ui) {
        try {
            storage.save(tasks.asList());
            return true;
        } catch (IOException | SecurityException e) {
            ui.showError("I couldn't save your changes. Please check that data/lobby.txt is writable.");
            return false;
        }
    }

}
