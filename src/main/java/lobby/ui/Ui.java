package lobby.ui;

import java.util.Scanner;

import lobby.task.Task;
import lobby.task.TaskList;

/**
 * Handles all text-based interactions between Lobby and the user.
 */
public class Ui {
    private static final String DIVIDER = "____________________________________________________________";
    private static final String BANNER = " _           _     _\n"
            + "| |    ___  | |__ | |__  _   _\n"
            + "| |   / _ \\ | '_ \\| '_ \\| | | |\n"
            + "| |__| (_) | |_) | |_) | |_| |\n"
            + "|_____\\___/|_.__/|_.__/ \\__, |\n"
            + "                         |___/";

    private final Scanner scanner;

    /**
     * Creates a console UI that reads commands from standard input.
     */
    public Ui() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Shows the application banner and initial greeting.
     */
    public void showWelcome() {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println("Hello! I'm Lobby.");
        System.out.println("What can I do for you?");
        System.out.println(DIVIDER);
    }

    /**
     * Reads the next trimmed command, or returns {@code null} at the end of input.
     *
     * @return the next command, or {@code null} when no more input is available
     */
    public String readCommand() {
        return scanner.hasNextLine() ? scanner.nextLine().trim() : null;
    }

    /**
     * Starts a response with the standard divider.
     */
    public void startResponse() {
        System.out.println(DIVIDER);
    }

    /**
     * Ends a response with the standard divider.
     */
    public void endResponse() {
        System.out.println(DIVIDER);
    }

    /**
     * Shows a complete response produced by Lobby.
     *
     * @param response response to display.
     */
    public void showResponse(String response) {
        System.out.println(response);
    }

    /**
     * Shows all tasks with one-based numbering.
     *
     * @param tasks tasks to display.
     */
    public void showTaskList(TaskList tasks) {
        System.out.println(" Here are the tasks in your list:");
        showNumberedTasks(tasks);
    }

    /**
     * Shows tasks whose descriptions matched a find command.
     *
     * @param matchingTasks tasks that matched the search keyword
     */
    public void showMatchingTasks(TaskList matchingTasks) {
        System.out.println(" Here are the matching tasks in your list:");
        showNumberedTasks(matchingTasks);
    }

    /**
     * Shows tasks with one-based numbering.
     *
     * @param tasks tasks to display
     */
    private void showNumberedTasks(TaskList tasks) {
        for (int taskNumber = 1; taskNumber <= tasks.size(); taskNumber++) {
            System.out.println(" " + taskNumber + "." + tasks.get(taskNumber));
        }
    }

    /**
     * Shows a successful task addition and the new list size.
     *
     * @param task the task that was added.
     * @param taskCount the number of tasks after the addition.
     */
    public void showTaskAdded(Task task, int taskCount) {
        System.out.println(" Got it. I've added this task:");
        System.out.println("   " + task);
        showTaskCount(taskCount);
    }

    /**
     * Shows a successful task deletion and the new list size.
     *
     * @param task the task that was removed.
     * @param taskCount the number of tasks after the deletion.
     */
    public void showTaskDeleted(Task task, int taskCount) {
        System.out.println(" Noted. I've removed this task:");
        System.out.println("   " + task);
        showTaskCount(taskCount);
    }

    /**
     * Shows that a task was marked as completed.
     *
     * @param task the updated task.
     */
    public void showTaskMarked(Task task) {
        System.out.println(" Nice! I've marked this task as done:");
        System.out.println("   " + task);
    }

    /**
     * Shows that a task was marked as incomplete.
     *
     * @param task the updated task.
     */
    public void showTaskUnmarked(Task task) {
        System.out.println(" OK, I've marked this task as not done yet:");
        System.out.println("   " + task);
    }

    /**
     * Shows a recoverable error or validation message.
     *
     * @param message explanation to show to the user.
     */
    public void showError(String message) {
        System.out.println(" " + message);
    }

    /**
     * Shows a warning that malformed saved records were ignored.
     *
     * @param skippedLines number of invalid records.
     */
    public void showSkippedLinesWarning(int skippedLines) {
        String lineWord = skippedLines == 1 ? "line" : "lines";
        System.out.println(" I skipped " + skippedLines + " invalid " + lineWord
                + " while loading data/lobby.txt.");
        endResponse();
    }

    /**
     * Shows a warning that the save file could not be read.
     */
    public void showLoadingError() {
        System.out.println(" I couldn't read data/lobby.txt, so I started with an empty task list.");
        endResponse();
    }

    /**
     * Shows the standard farewell.
     */
    public void showFarewell() {
        System.out.println(" Bye. Hope to see you again soon!");
        endResponse();
    }

    /**
     * Shows the list size with the correct singular or plural noun.
     *
     * @param taskCount current number of tasks.
     */
    private void showTaskCount(int taskCount) {
        String taskWord = taskCount == 1 ? "task" : "tasks";
        System.out.println(" Now you have " + taskCount + " " + taskWord + " in the list.");
    }
}
