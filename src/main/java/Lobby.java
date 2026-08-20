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
        String banner = " _           _     _          \n"
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
        String[] tasks = new String[100];
        int taskCount = 0;
        while (true) {
            String command = scanner.nextLine();
            System.out.println(divider);

            if (command.equals("bye")) {
                System.out.println(" Bye. Hope to see you again soon!");
                System.out.println(divider);
                break;
            } else if (command.equals("list")) {
                for (int i = 0; i < taskCount; i++) {
                    System.out.println(" " + (i + 1) + ". " + tasks[i]);
                }
            }else {
                tasks[taskCount] = command;
                taskCount++;
                System.out.println(" added: " + command);
            }

            System.out.println(divider);
        }
    }
}
