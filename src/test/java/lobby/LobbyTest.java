package lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    @Test
    public void getResponse_saveFails_restoresTasksForEveryMutation() throws IOException {
        List<String> commands = List.of("todo unsaved", "mark 2", "unmark 1", "delete 2",
                "mark 1", "unmark 2");
        for (int i = 0; i < commands.size(); i++) {
            Path saveFile = temporaryDirectory.resolve("case-" + i).resolve("lobby.txt");
            Files.createDirectories(saveFile.getParent());
            Files.write(saveFile, List.of("T | 1 | first", "T | 0 | middle", "T | 0 | last"));
            Lobby lobby = new Lobby(saveFile.toString());
            String originalList = lobby.getResponse("list");
            // A non-empty directory at the save path reliably prevents replacement on every platform.
            Files.delete(saveFile);
            Files.createDirectory(saveFile);
            Files.writeString(saveFile.resolve("blocker.txt"), "Prevent replacement");

            String response = lobby.getResponse(commands.get(i));

            assertTrue(response.contains("I couldn't save your changes."), commands.get(i));
            assertEquals(originalList, lobby.getResponse("list"), commands.get(i));
        }
    }

    @Test
    public void getResponse_invalidTaskNumbers_returnsGuidance() {
        Lobby lobby = new Lobby(temporaryDirectory.resolve("lobby.txt").toString());
        lobby.getResponse("todo existing task");

        for (String command : List.of("mark 0", "unmark 2", "delete -1")) {
            assertEquals(" Please enter the number of a task in the list.", lobby.getResponse(command));
        }
    }
}
