package com.mycompany.loginproject1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/** User-facing tests: complete Part 1 + Part 2 + Part 3 console sessions. */
class QuickChatPart3Test {
    @TempDir Path folder;

    @BeforeEach
    void freshSession() {
        Message.resetSessionForTests();
    }

    private String run(String script, Path file) {
        ByteArrayInputStream input = new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try (Scanner scanner = new Scanner(input, StandardCharsets.UTF_8);
             PrintStream output = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
            new QuickChat(scanner, output, file).run();
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    private String registration() {
        return "Kyle\nSmith\nkyl_1\nCh&&sec@ke99!\n+27838968976\n"
                + "kyl_1\nCh&&sec@ke99!\n";
    }

    @Test
    void newMenuOptionSupportsDisplayLongestRecipientAndReport() {
        Path file = folder.resolve("messages.json");
        String script = registration() + "2\n1\n"
                + "+27834557896\nDid you get the cake?\n1\n"
                + "+27838884567\nWhere are you? You are late! I have asked you to be on time.\n3\n"
                + "4\n1\n2\n4\n+27838884567\n6\n7\n3\n";
        String output = run(script, file);
        assertTrue(output.contains("Welcome to QuickChat."));
        assertTrue(output.contains("4) Stored Messages"));
        assertTrue(output.contains("Total messages sent: 1"));
        assertTrue(output.contains("Sender: Kyle Smith | Recipient: +27838884567"));
        assertTrue(output.contains("Longest stored message:"));
        assertTrue(output.contains("Where are you? You are late!"));
        assertTrue(output.contains("QuickChat Message Report"));
        assertTrue(output.contains("Message Hash:"));
        assertTrue(output.contains("Goodbye!"));
        assertEquals(1, new MessageArchive(file).getStoredMessages().length);
    }

    @Test
    void previousStoredJsonIsReloadedAndCanBeSearchedAndDeleted() {
        Path file = folder.resolve("messages.json");
        Message message = new Message("0838884567", 3, "+27838884567", "It is dinner time !", "Developer");
        message.storeMessage(file);
        String script = registration() + "0\n4\n3\n0838884567\n"
                + "5\n" + message.getMessageHash() + "\n6\n7\n3\n";
        String output = run(script, file);
        assertTrue(output.contains("Message: It is dinner time !"));
        assertTrue(output.contains("successfully deleted."));
        assertTrue(output.contains("No sent or stored messages to report."));
        assertEquals(0, MessageJsonStore.read(file).size());
    }

    @Test
    void emptyStoredMenuAndInvalidChoiceHandled() {
        Path file = folder.resolve("messages.json");
        String output = run(registration() + "0\n4\n99\n1\n2\n3\ninvalid\n"
                + "4\n+27838884567\n5\nnotahash\n6\n7\n3\n", file);
        assertTrue(output.contains("Please enter a whole number from 1 to 7."));
        assertTrue(output.contains("No stored messages found."));
        assertTrue(output.contains("Message ID not found."));
        assertTrue(output.contains("No sent or stored messages found"));
        assertTrue(output.contains("No stored message found with that hash."));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void reportWorksWithOnlySentMessages() {
        Path file = folder.resolve("messages.json");
        String output = run(registration() + "1\n1\n+27834557896\nDid you get the cake?\n1\n"
                + "4\n6\n7\n3\n", file);
        assertTrue(output.contains("Status: Sent"));
        assertTrue(output.contains("Message: Did you get the cake?"));
        assertEquals(0, MessageJsonStore.read(file).size());
    }
}
