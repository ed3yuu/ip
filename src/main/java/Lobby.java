import java.util.Scanner;

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
        Object[] tasks = new Object[100];
        int taskCount = 0;
        while (true) {
            String command = scanner.nextLine();
            System.out.println(divider);

            if (command.equals("bye")) {
                System.out.println(" Bye. Hope to see you again soon!");
                System.out.println(divider);
                break;
            } else if (command.equals("list")) {
                System.out.println(" Here are the tasks in your list:");
                for (int i = 0; i < taskCount; i++) {
                    System.out.println(" " + (i + 1) + "." + tasks[i]);
                }
            } else if (command.startsWith("mark ")) {
                String taskNumberText = command.substring("mark ".length()).trim();
                try {
                    int taskNumber = Integer.parseInt(taskNumberText);
                    if (taskNumber < 1 || taskNumber > taskCount) {
                        System.out.println(" Please enter the number of a task in the list.");
                    } else {
                        int taskIndex = taskNumber - 1;
                        markTaskAsDone(tasks[taskIndex]);
                        System.out.println(" Nice! I've marked this task as done:");
                        System.out.println("   " + tasks[taskIndex]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println(" Please use mark followed by a task number.");
                }
            } else if (command.startsWith("unmark ")) {
                String taskNumberText = command.substring("unmark ".length()).trim();
                try {
                    int taskNumber = Integer.parseInt(taskNumberText);
                    if (taskNumber < 1 || taskNumber > taskCount) {
                        System.out.println(" Please enter the number of a task in the list.");
                    } else {
                        int taskIndex = taskNumber - 1;
                        markTaskAsNotDone(tasks[taskIndex]);
                        System.out.println(" OK, I've marked this task as not done yet:");
                        System.out.println("   " + tasks[taskIndex]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println(" Please use unmark followed by a task number.");
                }
            } else if (command.startsWith("todo ")) {
                Todo todo = new Todo(command.substring("todo ".length()).trim());
                tasks[taskCount] = todo;
                taskCount++;
                printTaskAdded(todo, taskCount);
            } else if (command.startsWith("deadline ")) {
                String[] deadlineParts = command.substring("deadline ".length()).split(" /by ", 2);
                if (deadlineParts.length < 2) {
                    System.out.println(" Please use deadline <description> /by <when>.");
                } else {
                    Deadline deadline = new Deadline(deadlineParts[0].trim(), deadlineParts[1].trim());
                    tasks[taskCount] = deadline;
                    taskCount++;
                    printTaskAdded(deadline, taskCount);
                }
            } else if (command.startsWith("event ")) {
                String[] eventParts = command.substring("event ".length()).split(" /from | /to ", 3);
                if (eventParts.length < 3) {
                    System.out.println(" Please use event <description> /from <start> /to <end>.");
                } else {
                    Event event = new Event(eventParts[0].trim(), eventParts[1].trim(), eventParts[2].trim());
                    tasks[taskCount] = event;
                    taskCount++;
                    printTaskAdded(event, taskCount);
                }
            } else {
                System.out.println(" Please use todo, deadline, event, list, mark, unmark, or bye.");
            }

            System.out.println(divider);
        }
    }

    /**
     * Prints the confirmation shown after a new task is added.
     *
     * @param task the task that was added
     * @param taskCount the number of tasks currently stored
     */
    private static void printTaskAdded(Object task, int taskCount) {
        System.out.println(" Got it. I've added this task:");
        System.out.println("   " + task);
        String taskWord = taskCount == 1 ? "task" : "tasks";
        System.out.println(" Now you have " + taskCount + " " + taskWord + " in the list.");
    }

    /**
     * Marks an independent task-type object as complete.
     *
     * <p>This repeated type check is intentional for the pre-inheritance version of the program.
     * The inheritance extension will replace it with polymorphic method calls.</p>
     *
     * @param task the task object to update
     */
    private static void markTaskAsDone(Object task) {
        if (task instanceof Todo) {
            ((Todo) task).markAsDone();
        } else if (task instanceof Deadline) {
            ((Deadline) task).markAsDone();
        } else if (task instanceof Event) {
            ((Event) task).markAsDone();
        }
    }

    /**
     * Marks an independent task-type object as incomplete.
     *
     * @param task the task object to update
     */
    private static void markTaskAsNotDone(Object task) {
        if (task instanceof Todo) {
            ((Todo) task).markAsNotDone();
        } else if (task instanceof Deadline) {
            ((Deadline) task).markAsNotDone();
        } else if (task instanceof Event) {
            ((Event) task).markAsNotDone();
        }
    }
}
