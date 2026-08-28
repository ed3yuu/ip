package lobby.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import lobby.exception.LobbyException;
import lobby.task.Deadline;
import lobby.task.Event;
import lobby.task.Todo;

/**
 * Interprets user command text and validates command arguments.
 */
public class Parser {

    /**
     * Represents the commands Lobby understands.
     */
    public enum Command {
        /** Ends the application. */
        BYE,
        /** Displays all saved tasks. */
        LIST,
        /** Marks a task as completed. */
        MARK,
        /** Marks a task as incomplete. */
        UNMARK,
        /** Adds a to-do task. */
        TODO,
        /** Adds a deadline task. */
        DEADLINE,
        /** Adds an event task. */
        EVENT,
        /** Removes a task. */
        DELETE,
        /** Represents input that does not match a supported command. */
        UNKNOWN
    }

    /**
     * Creates a parser for interpreting Lobby commands.
     */
    public Parser() {
    }

    /**
     * Determines which command a user command line represents.
     * Only the first word is considered, so arguments do not affect matching.
     *
     * @param command the complete command entered by the user
     * @return the matching command, or {@link Command#UNKNOWN} if none matches
     */
    public Command parseCommand(String command) {
        String commandWord = command.split("\\s+", 2)[0];
        try {
            return Command.valueOf(commandWord.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Command.UNKNOWN;
        }
    }

    /**
     * Reads a task number following a command word.
     *
     * @param command the complete command entered by the user
     * @param commandWord the command word to remove before parsing the number
     * @return the one-based task number entered by the user
     * @throws LobbyException if the argument is not an integer
     */
    public int parseTaskNumber(String command, String commandWord) throws LobbyException {
        String taskNumberText = command.substring(commandWord.length()).trim();
        try {
            return Integer.parseInt(taskNumberText);
        } catch (NumberFormatException e) {
            throw new LobbyException("Please use " + commandWord + " followed by a task number.");
        }
    }

    /**
     * Creates a to-do after validating that the command has a description.
     *
     * @param command the complete command entered by the user
     * @return the new to-do
     * @throws LobbyException if the command does not include a description
     */
    public Todo parseTodo(String command) throws LobbyException {
        String description = command.substring("todo".length()).trim();
        if (description.isEmpty()) {
            throw new LobbyException("A to-do needs a description. Try: todo <description>.");
        }
        return new Todo(description);
    }

    /**
     * Creates a deadline after validating all required command parts.
     *
     * @param command the complete command entered by the user
     * @return the new deadline
     * @throws LobbyException if the command lacks a description, {@code /by}, or valid date
     */
    public Deadline parseDeadline(String command) throws LobbyException {
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
     * Creates an event after validating all required command parts.
     *
     * @param command the complete command entered by the user
     * @return the new event
     * @throws LobbyException if the command lacks a description, start time, or end time
     */
    public Event parseEvent(String command) throws LobbyException {
        String eventDetails = command.substring("event".length()).trim();
        int fromIndex = findMarker(eventDetails, "/from");
        if (fromIndex < 0) {
            throw new LobbyException(
                    "An event needs a /from start time. Try: event <description> /from <start> /to <end>.");
        }

        String description = eventDetails.substring(0, fromIndex).trim();
        String timeDetails = eventDetails.substring(fromIndex + "/from".length()).trim();
        int toIndex = findMarker(timeDetails, "/to");
        if (toIndex < 0) {
            throw new LobbyException(
                    "An event needs a /to end time. Try: event <description> /from <start> /to <end>.");
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
     * Finds a case-insensitive marker surrounded by whitespace or string boundaries.
     *
     * @param text command details to search
     * @param marker marker to find, such as {@code /by}
     * @return the marker's starting index, or {@code -1} if it is absent
     */
    private int findMarker(String text, String marker) {
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
