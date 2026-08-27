package lobby.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import lobby.exception.LobbyException;
import lobby.task.Deadline;

/**
 * Tests deadline command parsing and validation in {@link Parser}.
 */
public class ParserTest {
    private final Parser parser = new Parser();

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
}
