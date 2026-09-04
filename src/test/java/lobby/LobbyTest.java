package lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests Lobby's command-response interface used by the graphical UI.
 */
public class LobbyTest {

    @TempDir
    private Path temporaryDirectory;

    @Test
    public void getResponse_addAndListTask_returnsUpdatedResponses() {
        Lobby lobby = new Lobby(temporaryDirectory.resolve("lobby.txt").toString());

        String addResponse = lobby.getResponse("todo read the JavaFX tutorial");
        String listResponse = lobby.getResponse("list");

        assertTrue(addResponse.contains("[T][ ] read the JavaFX tutorial"));
        assertTrue(addResponse.contains("Now you have 1 task in the list."));
        assertTrue(listResponse.contains("1.[T][ ] read the JavaFX tutorial"));
    }

    @Test
    public void getResponse_unknownCommand_returnsGuidance() {
        Lobby lobby = new Lobby(temporaryDirectory.resolve("lobby.txt").toString());

        String response = lobby.getResponse("hello");

        assertEquals(" Please use todo, deadline, event, list, find, mark, unmark, delete, or bye.", response);
    }

    @Test
    public void getResponse_commandWithOuterWhitespace_processesTrimmedCommand() {
        Lobby lobby = new Lobby(temporaryDirectory.resolve("lobby.txt").toString());

        String response = lobby.getResponse("  todo revise notes  ");

        assertTrue(response.contains("[T][ ] revise notes"));
    }
}
