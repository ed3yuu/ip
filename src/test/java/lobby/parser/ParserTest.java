package lobby.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import lobby.exception.LobbyException;
import lobby.task.Deadline;
import lobby.task.Event;
import lobby.task.Todo;

/**
 * Tests command parsing and validation in {@link Parser}.
 */
public class ParserTest {
    private final Parser parser = new Parser();

    @Test
    public void parseCommand_knownCommands_returnsMatchingCommands() {
        assertAll(
                () -> assertEquals(Parser.Command.BYE, parser.parseCommand("bye")),
                () -> assertEquals(Parser.Command.LIST, parser.parseCommand("list")),
                () -> assertEquals(Parser.Command.MARK, parser.parseCommand("mark")),
                () -> assertEquals(Parser.Command.UNMARK, parser.parseCommand("unmark")),
                () -> assertEquals(Parser.Command.TODO, parser.parseCommand("todo")),
                () -> assertEquals(Parser.Command.DEADLINE, parser.parseCommand("deadline")),
                () -> assertEquals(Parser.Command.EVENT, parser.parseCommand("event")),
                () -> assertEquals(Parser.Command.DELETE, parser.parseCommand("delete")),
                () -> assertEquals(Parser.Command.FIND, parser.parseCommand("find")));
    }

    @Test
    public void parseCommand_mixedCaseWithArguments_returnsMatchingCommand() {
        assertEquals(Parser.Command.TODO, parser.parseCommand("ToDo read book"));
    }

    @Test
    public void parseCommand_unknownOrEmptyCommand_returnsUnknown() {
        assertAll(
                () -> assertEquals(Parser.Command.UNKNOWN, parser.parseCommand("search book")),
                () -> assertEquals(Parser.Command.UNKNOWN, parser.parseCommand("")));
    }

    @Test
    public void parseFindKeyword_validCommand_returnsTrimmedKeyword() throws LobbyException {
        assertEquals("read book", parser.parseFindKeyword("find   read book   "));
    }

    @Test
    public void parseFindKeyword_missingKeyword_exceptionThrown() {
        LobbyException exception = assertThrows(LobbyException.class,
                () -> parser.parseFindKeyword("find   "));

        assertEquals("Please use find followed by a keyword.", exception.getMessage());
    }

    @Test
    public void parseTaskNumber_integerArgument_returnsInteger() throws LobbyException {
        assertAll(
                () -> assertEquals(3, parser.parseTaskNumber("mark 3", "mark")),
                () -> assertEquals(0, parser.parseTaskNumber("delete   0  ", "delete")),
                () -> assertEquals(-2, parser.parseTaskNumber("unmark -2", "unmark")));
    }

    @Test
    public void parseTaskNumber_missingArgument_exceptionThrown() {
        LobbyException exception = assertThrows(LobbyException.class,
                () -> parser.parseTaskNumber("mark", "mark"));

        assertEquals("Please use mark followed by a task number.", exception.getMessage());
    }

    @Test
    public void parseTaskNumber_nonIntegerArgument_exceptionThrown() {
        assertAll(
                () -> assertThrows(LobbyException.class,
                        () -> parser.parseTaskNumber("mark first", "mark")),
                () -> assertThrows(LobbyException.class,
                        () -> parser.parseTaskNumber("mark 1.5", "mark")),
                () -> assertThrows(LobbyException.class,
                        () -> parser.parseTaskNumber("mark 2147483648", "mark")));
    }

    @Test
    public void parseTodo_validCommand_returnsTrimmedTodo() throws LobbyException {
        Todo todo = parser.parseTodo("todo   read a book   ");

        assertEquals("read a book", todo.getDescription());
        assertFalse(todo.isDone());
        assertEquals("T | 0 | read a book", todo.toDataString());
    }

    @Test
    public void parseTodo_emptyDescription_exceptionThrown() {
        LobbyException exception = assertThrows(LobbyException.class,
                () -> parser.parseTodo("todo    "));

        assertEquals("A to-do needs a description. Try: todo <description>.",
                exception.getMessage());
    }

    @Test
    public void parseDeadline_validCommand_returnsDeadline() throws LobbyException {
        Deadline deadline = parser.parseDeadline("deadline return book /by 2026-09-30");

        assertEquals("return book", deadline.getDescription());
        assertFalse(deadline.isDone());
        assertEquals("D | 0 | return book | 2026-09-30", deadline.toDataString());
        assertEquals("[D][ ] return book (by: Sep 30 2026)", deadline.toString());
    }

    @Test
    public void parseDeadline_extraWhitespaceAndUppercaseMarker_returnsTrimmedDeadline()
            throws LobbyException {
        Deadline deadline = parser.parseDeadline(
                "deadline   submit report   /BY   2024-02-29   ");

        assertEquals("submit report", deadline.getDescription());
        assertEquals("D | 0 | submit report | 2024-02-29", deadline.toDataString());
    }

    @Test
    public void parseDeadline_missingByMarker_exceptionThrown() {
        LobbyException exception = assertThrows(LobbyException.class,
                () -> parser.parseDeadline("deadline return book 2026-09-30"));

        assertEquals(
                "A deadline needs a /by time. Try: deadline <description> /by <when>.",
                exception.getMessage());
    }

    @Test
    public void parseDeadline_byWithinWord_exceptionThrown() {
        assertThrows(LobbyException.class,
                () -> parser.parseDeadline("deadline return/by 2026-09-30"));
    }

    @Test
    public void parseDeadline_emptyDescription_exceptionThrown() {
        LobbyException exception = assertThrows(LobbyException.class,
                () -> parser.parseDeadline("deadline /by 2026-09-30"));

        assertEquals("A deadline needs a description before /by.", exception.getMessage());
    }

    @Test
    public void parseDeadline_emptyDate_exceptionThrown() {
        LobbyException exception = assertThrows(LobbyException.class,
                () -> parser.parseDeadline("deadline return book /by"));

        assertEquals("A deadline needs a time after /by.", exception.getMessage());
    }

    @Test
    public void parseDeadline_wrongDateFormat_exceptionThrown() {
        LobbyException exception = assertThrows(LobbyException.class,
                () -> parser.parseDeadline("deadline return book /by 30-09-2026"));

        assertEquals(
                "Please enter the deadline date as yyyy-MM-dd, for example 2019-10-15.",
                exception.getMessage());
    }

    @Test
    public void parseDeadline_impossibleDate_exceptionThrown() {
        assertThrows(LobbyException.class,
                () -> parser.parseDeadline("deadline return book /by 2025-02-29"));
    }

    @Test
    public void parseEvent_validCommand_returnsEvent() throws LobbyException {
        Event event = parser.parseEvent(
                "event project meeting /from Monday 2pm /to Monday 3pm");

        assertEquals("project meeting", event.getDescription());
        assertFalse(event.isDone());
        assertEquals("E | 0 | project meeting | Monday 2pm | Monday 3pm",
                event.toDataString());
        assertEquals("[E][ ] project meeting (from: Monday 2pm to: Monday 3pm)",
                event.toString());
    }

    @Test
    public void parseEvent_extraWhitespaceAndUppercaseMarkers_returnsTrimmedEvent()
            throws LobbyException {
        Event event = parser.parseEvent(
                "event   project meeting   /FROM   2pm   /TO   3pm   ");

        assertEquals("E | 0 | project meeting | 2pm | 3pm", event.toDataString());
    }

    @Test
    public void parseEvent_missingFromMarker_exceptionThrown() {
        LobbyException exception = assertThrows(LobbyException.class,
                () -> parser.parseEvent("event meeting 2pm /to 3pm"));

        assertEquals(
                "An event needs a /from start time. Try: event <description> /from <start> /to <end>.",
                exception.getMessage());
    }

    @Test
    public void parseEvent_fromWithinWord_exceptionThrown() {
        assertThrows(LobbyException.class,
                () -> parser.parseEvent("event meeting/from 2pm /to 3pm"));
    }

    @Test
    public void parseEvent_missingToMarker_exceptionThrown() {
        LobbyException exception = assertThrows(LobbyException.class,
                () -> parser.parseEvent("event meeting /from 2pm 3pm"));

        assertEquals(
                "An event needs a /to end time. Try: event <description> /from <start> /to <end>.",
                exception.getMessage());
    }

    @Test
    public void parseEvent_toWithinWord_exceptionThrown() {
        assertThrows(LobbyException.class,
                () -> parser.parseEvent("event meeting /from 2pm/to 3pm"));
    }

    @Test
    public void parseEvent_emptyDescription_exceptionThrown() {
        LobbyException exception = assertThrows(LobbyException.class,
                () -> parser.parseEvent("event /from 2pm /to 3pm"));

        assertEquals("An event needs a description before /from.", exception.getMessage());
    }

    @Test
    public void parseEvent_emptyStartTime_exceptionThrown() {
        LobbyException exception = assertThrows(LobbyException.class,
                () -> parser.parseEvent("event meeting /from /to 3pm"));

        assertEquals("An event needs a start time after /from.", exception.getMessage());
    }

    @Test
    public void parseEvent_emptyEndTime_exceptionThrown() {
        LobbyException exception = assertThrows(LobbyException.class,
                () -> parser.parseEvent("event meeting /from 2pm /to"));

        assertEquals("An event needs an end time after /to.", exception.getMessage());
    }
}
