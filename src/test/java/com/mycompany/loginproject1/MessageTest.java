package com.mycompany.loginproject1;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/** Unit tests matching the Part 2 question-sheet data and failure cases. */
class MessageTest {
    @TempDir Path temporaryFolder;

    @BeforeEach
    void startFreshSession() {
        Message.resetSessionForTests();
    }

    @Test
    void recipientMatchesPrescribedTestCasesAndRejectsLetters() {
        Message first = new Message("0011223344", 0, "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        Message second = new Message("0011223345", 1, "0857595889",
                "Hi Keegan, did you receive the payment?");
        assertEquals("Cell phone number successfully captured.", first.checkRecipientCell());
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.", second.checkRecipientCell());
        assertFalse(new Message("0011223345", 2, "+27abcdefghi", "Hello")
                .checkRecipientCell().equals("Cell phone number successfully captured."));
    }

    @Test
    void hashMatchesBothPrescribedExamples() {
        Message first = new Message("0011223344", 0, "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        Message second = new Message("0011223345", 1, "+27718693002",
                "Hi Keegan, did you receive the payment?");
        assertEquals("00:0:HITONIGHT", first.createMessageHash());
        assertEquals("00:1:HIPAYMENT", second.createMessageHash());
        assertEquals(first.createMessageHash(), first.getMessageHash());
    }

    @Test
    void generatedIdContainsExactlyTenDigits() {
        for (int number = 0; number < 100; number++) {
            Message message = new Message(number, "+27718693002", "Hello");
            assertTrue(message.checkMessageID());
            assertTrue(message.getMessageID().matches("\\d{10}"));
        }
        assertFalse(new Message("123456789012", 0, "+27718693002", "Hello")
                .checkMessageID());
    }

    @Test
    void lengthsAtAndBeyondBoundaryAreValidated() {
        assertEquals("Message ready to send.",
                new Message(0, "+27718693002", "x".repeat(250)).checkMessageLength());
        assertEquals("Message exceeds 250 characters by 10, please reduce the size.",
                new Message(0, "+27718693002", "x".repeat(260)).checkMessageLength());
        assertEquals("Please enter a message of less than 250 characters.",
                new Message(0, "+27718693002", "").checkMessageLength());
    }

    @Test
    void sendDisregardAndStoreProduceRequiredMessages() {
        Message sent = new Message(0, "+27718693002", "Hello friend");
        Message discarded = new Message(1, "+27718693002", "Not going out");
        assertEquals("Message successfully sent.", sent.SentMessage("Send"));
        assertEquals("Press 0 to delete the message.", discarded.SentMessage("Disregard"));
        assertEquals(1, sent.returnTotalMessages());
        assertEquals(1, Message.getTotalSentCount());
        assertTrue(sent.printMessages().contains("Message: Hello friend"));
        assertFalse(sent.printMessages().contains("Not going out"));
    }

    @Test
    void duplicateSendDoesNotInflateTotal() {
        Message one = new Message(0, "+27718693002", "First text");
        assertEquals("Message successfully sent.", one.SentMessage(1));
        assertEquals("Message successfully sent.", one.SentMessage(1));
        assertEquals(1, one.returnTotalMessages());
    }

    @Test
    void detailsAppearInRequiredOrder() {
        Message message = new Message("0011223344", 0, "+27718693002", "Hi Mike");
        String details = message.toString();
        assertTrue(details.indexOf("Message ID:") < details.indexOf("Message Hash:"));
        assertTrue(details.indexOf("Message Hash:") < details.indexOf("Recipient:"));
        assertTrue(details.indexOf("Recipient:") < details.indexOf("Message:"));
    }

    @Test
    void jsonIsEscapedAndPreviousStoredMessagesSurvive() throws IOException {
        Path file = temporaryFolder.resolve("messages.json");
        Message first = new Message("0011223344", 0, "+27718693002", "Hi \\\"Sam\\\"\nnext line\\backslash");
        Message second = new Message("0011223345", 1, "+27718693002", "Another message");
        assertTrue(first.storeMessage(file).contains("MessageID"));
        assertTrue(second.storeMessage(file).contains("MessageID"));
        String json = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(json.startsWith("["));
        assertTrue(json.stripTrailing().endsWith("]"));
        assertTrue(json.contains("\\\\\\\"")); // escaped backslash and escaped quote
        assertTrue(json.contains("\\n"));
        assertTrue(json.contains("0011223344"));
        assertTrue(json.contains("0011223345"));
        assertEquals(1, json.split("0011223344", -1).length - 1);
        assertEquals(0, Message.getTotalSentCount()); // Stored != Sent.
    }

    @Test
    void invalidExistingStorageFileDoesNotGetOverwritten() throws IOException {
        Path file = temporaryFolder.resolve("messages.json");
        Files.writeString(file, "bad existing data", StandardCharsets.UTF_8);
        Message message = new Message(0, "+27718693002", "Hi");
        assertThrows(UncheckedIOException.class, () -> message.storeMessage(file));
        assertEquals("bad existing data", Files.readString(file, StandardCharsets.UTF_8));
    }
}
