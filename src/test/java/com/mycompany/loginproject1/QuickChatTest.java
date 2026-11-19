package com.mycompany.loginproject1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Run complete console sessions: these integration tests cover the lost menu/loop marks. */
class QuickChatTest {
    @BeforeEach
    void startFreshSession() {
        Message.resetSessionForTests();
    }

    private String runScript(String script) {
        ByteArrayInputStream input = new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try (Scanner scanner = new Scanner(input, StandardCharsets.UTF_8);
             PrintStream print = new PrintStream(result, true, StandardCharsets.UTF_8)) {
            new QuickChat(scanner, print).run();
        }
        return result.toString(StandardCharsets.UTF_8);
    }

    private String registrationAndLogin() {
        return "Kyle\nSmith\nkyl_1\nCh&&sec@ke99!\n+27838968976\n"
                + "kyl_1\nCh&&sec@ke99!\n";
    }

    @Test
    void welcomeMenuComingSoonAndQuitWork() {
        String output = runScript(registrationAndLogin() + "0\n2\n3\n");
        assertTrue(output.contains("Welcome to QuickChat."));
        assertTrue(output.contains("1) Send Messages"));
        assertTrue(output.contains("2) Show recently sent messages"));
        assertTrue(output.contains("3) Quit"));
        assertTrue(output.contains("Coming Soon."));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void forLoopGeneratesSuccessiveMessageNumbersAndCountsOnlySent() {
        String script = registrationAndLogin() + "2\n1\n"
                + "+27718693002\nHi Mike, can you join us for dinner tonight?\n1\n"
                + "+27718693002\nHi Keegan, did you receive the payment?\n2\n0\n"
                + "1\n3\n";
        String output = runScript(script);
        assertTrue(output.contains("--- Message number 0 ---"));
        assertTrue(output.contains("--- Message number 1 ---"));
        assertTrue(output.matches("(?s).*Message ID generated: <\\d{10}>.*"));
        assertTrue(output.matches("(?s).*Message Hash: \\d{2}:0:HITONIGHT.*"));
        assertTrue(output.contains("Message successfully sent."));
        assertTrue(output.contains("Press 0 to delete the message."));
        assertTrue(output.contains("Message disregarded."));
        assertTrue(output.contains("All 2 message entries have been processed."));
        assertTrue(output.contains("Total messages sent: 1"));
        assertTrue(output.contains("You have already entered your requested number of messages."));
        assertEquals(1, Message.getTotalSentCount());
    }

    @Test
    void invalidMenuRecipientAndMessageTextReprompt() {
        String script = registrationAndLogin() + "1\n9\nnope\n1\n"
                + "not a phone\n+27718693002\n\nHi there\n1\n3\n";
        String output = runScript(script);
        assertTrue(output.contains("Please enter a whole number from 1 to 4."));
        assertTrue(output.contains("Cell phone number is incorrectly formatted"));
        assertTrue(output.contains("Please enter a message of less than 250 characters."));
        assertTrue(output.contains("Total messages sent: 1"));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void userCannotEnterMessagesBeforeSuccessfulLogin() {
        String script = "Kyle\nSmith\nkyl_1\nCh&&sec@ke99!\n+27838968976\n"
                + "bad_user\nbadPassword\n"
                + "kyl_1\nCh&&sec@ke99!\n0\n3\n";
        String output = runScript(script);
        assertTrue(output.contains("Username or password incorrect, please try again."));
        assertTrue(output.indexOf("Welcome to QuickChat.")
                > output.indexOf("Username or password incorrect"));
    }

    @Test
    void eofStopsApplicationRatherThanLoopingForever() {
        String output = runScript("Kyle\n");
        assertTrue(output.contains("Input ended. Goodbye!"));
    }
}
