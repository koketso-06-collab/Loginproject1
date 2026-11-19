package com.mycompany.loginproject1;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/** Part 3 prescribed five-message test data, populated through message actions. */
class MessageArchiveTest {
    @TempDir Path tempFolder;

    @BeforeEach
    void resetState() {
        Message.resetSessionForTests();
    }

    /** Test-only data from Part 3 pages 18-20; not hard-coded in the application. */
    private MessageArchive rubricData() {
        MessageArchive archive = new MessageArchive(tempFolder.resolve("messages.json"));
        archive.addSent(new Message("0012345678", 0, "+27834557896",
                "Did you get the cake?", "Developer"));
        archive.addStored(new Message("0112345678", 1, "+27838884567",
                "Where are you? You are late! I have asked you to be on time.", "Developer"));
        archive.addDisregarded(new Message("0212345678", 2, "+27834484567",
                "Yohoooo, I am at your gate.", "Developer"));
        archive.addSent(new Message("0838884567", 3, "+27834557896",
                "It is dinner time !", "Developer"));
        archive.addStored(new Message("0412345678", 4, "+27838884567",
                "Ok, I am leaving without you.", "Developer"));
        return archive;
    }

    @Test
    void sentDisregardedStoredIdAndHashArraysContainCorrectData() {
        MessageArchive archive = rubricData();
        assertArrayEquals(new String[]{"Did you get the cake?", "It is dinner time !"},
                archive.getSentMessages());
        assertArrayEquals(new String[]{"Yohoooo, I am at your gate."},
                archive.getDisregardedMessages());
        assertArrayEquals(new String[]{"Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you."}, archive.getStoredMessages());
        assertEquals(5, archive.getMessageIds().length);
        assertEquals(5, archive.getMessageHashes().length);
        assertEquals("0838884567", archive.getMessageIds()[3]);
    }

    @Test
    void arraysAreDefensiveCopies() {
        MessageArchive archive = rubricData();
        String[] copy = archive.getSentMessages();
        copy[0] = "incorrect";
        assertEquals("Did you get the cake?", archive.getSentMessages()[0]);
    }

    @Test
    void readStoredJsonAndDisplaySenderAndRecipient() {
        rubricData();
        MessageArchive reloaded = new MessageArchive(tempFolder.resolve("messages.json"));
        assertEquals(2, reloaded.getStoredMessages().length);
        String text = reloaded.storedSendersAndRecipients();
        assertTrue(text.contains("Sender: Developer"));
        assertTrue(text.contains("Recipient: +27838884567"));
        assertEquals(2, text.split("Sender:", -1).length - 1);
    }

    @Test
    void displayLongestStoredMessageWithFullDetails() {
        MessageArchive archive = rubricData();
        Message longest = archive.longestStoredMessage();
        assertNotNull(longest);
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                longest.getMessageText());
        assertTrue(longest.toString().contains("Message Hash:"));
    }

    @Test
    void searchByMessageIdFindsPrescribedMessageFour() {
        MessageArchive archive = rubricData();
        assertEquals("It is dinner time !", archive.findById("0838884567").getMessageText());
        assertEquals("+27834557896", archive.findById("0838884567").getRecipient());
        assertNull(archive.findById("nonexistent"));
        assertNull(archive.findById("0212345678")); // discarded is not searchable
    }

    @Test
    void searchByRecipientReturnsBothStoredMessagesButNotDisregarded() {
        MessageArchive archive = rubricData();
        List<Message> results = archive.findByRecipient("+27838884567");
        assertEquals(2, results.size());
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                results.get(0).getMessageText());
        assertEquals("Ok, I am leaving without you.", results.get(1).getMessageText());
        assertTrue(archive.findByRecipient("+27000000000").isEmpty());
    }

    @Test
    void searchesBothSentAndStoredMessages() {
        MessageArchive archive = rubricData();
        assertEquals(2, archive.findByRecipient("+27834557896").size());
        assertEquals("Did you get the cake?", archive.findByRecipient("+27834557896").get(0).getMessageText());
    }

    @Test
    void deleteHashRemovesStoredMessageFromArraysAndJson() throws IOException {
        MessageArchive archive = rubricData();
        String hash = archive.findByRecipient("+27838884567").get(0).getMessageHash();
        Message removed = archive.deleteByHash(hash);
        assertEquals("Where are you? You are late! I have asked you to be on time.", removed.getMessageText());
        assertEquals(1, archive.getStoredMessages().length);
        assertEquals(4, archive.getMessageIds().length);
        assertNull(archive.deleteByHash(hash));
        String json = Files.readString(tempFolder.resolve("messages.json"));
        assertFalse(json.contains("Where are you?"));
        MessageArchive restarted = new MessageArchive(tempFolder.resolve("messages.json"));
        assertEquals("Ok, I am leaving without you.", restarted.getStoredMessages()[0]);
    }

    @Test
    void reportListsIdsHashesRecipientsSenderAndSentOrStoredStatus() {
        MessageArchive archive = rubricData();
        String report = archive.messageReport();
        assertTrue(report.contains("Message Hash:"));
        assertTrue(report.contains("Message ID: 0838884567"));
        assertTrue(report.contains("Recipient: +27838884567"));
        assertTrue(report.contains("Sender: Developer"));
        assertTrue(report.contains("Status: Sent"));
        assertTrue(report.contains("Status: Stored"));
        assertFalse(report.contains("Yohoooo"));
    }

    @Test
    void emptyArraysAndEmptyResultsAreHandled() {
        MessageArchive archive = new MessageArchive(tempFolder.resolve("new.json"));
        assertEquals(0, archive.getSentMessages().length);
        assertEquals(0, archive.getDisregardedMessages().length);
        assertEquals(0, archive.getStoredMessages().length);
        assertNull(archive.longestStoredMessage());
        assertEquals("No stored messages found.", archive.storedSendersAndRecipients());
        assertTrue(archive.messageReport().contains("No sent or stored messages"));
    }

    @Test
    void acceptsPreviousPartTwoFourFieldJsonWithoutSender() throws IOException {
        Path file = tempFolder.resolve("legacy.json");
        Files.writeString(file,
                "[{\"MessageID\":\"0012345678\",\"MessageHash\":\"00:0:HELLONOW\","
                + "\"Recipient\":\"+27834557896\",\"Message\":\"Hello now\"}]",
                StandardCharsets.UTF_8);
        MessageArchive archive = new MessageArchive(file);
        assertEquals("Hello now", archive.getStoredMessages()[0]);
        assertTrue(archive.storedSendersAndRecipients().contains("Unknown sender"));
    }

    @Test
    void jsonEscapedCharactersAreReadBackAndRoundTrip() {
        MessageArchive archive = new MessageArchive(tempFolder.resolve("escape.json"));
        String tricky = "Quotes \"hello\", slash \\ and newline\nnext";
        archive.addStored(new Message("0012345678", 0, "+27834557896", tricky, "Developer"));
        MessageArchive reloaded = new MessageArchive(tempFolder.resolve("escape.json"));
        assertEquals(tricky, reloaded.getStoredMessages()[0]);
        assertEquals(tricky, reloaded.findById("0012345678").getMessageText());
    }

    @Test
    void invalidExistingJsonIsRejectedWithoutOverwriting() throws IOException {
        Path file = tempFolder.resolve("invalid.json");
        Files.writeString(file, "[{bad]", StandardCharsets.UTF_8);
        assertThrows(UncheckedIOException.class, () -> new MessageArchive(file));
        assertEquals("[{bad]", Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void duplicateMessageIdsDoNotOverwriteAnEarlierRecord() {
        MessageArchive archive = new MessageArchive(tempFolder.resolve("dupe.json"));
        archive.addStored(new Message("0012345678", 0, "+27834557896", "First", "Developer"));
        assertThrows(UncheckedIOException.class,
                () -> archive.addStored(new Message("0012345678", 1, "+27838884567", "Second", "Developer")));
        assertEquals(1, new MessageArchive(tempFolder.resolve("dupe.json")).getStoredMessages().length);
    }

    @Test
    void hashCollisionDeletesOnlyOneMatchingRecord() {
        MessageArchive archive = new MessageArchive(tempFolder.resolve("hash.json"));
        archive.addStored(new Message("0012345678", 0, "+27834557896", "Hi there", "Developer"));
        archive.addStored(new Message("0099999999", 0, "+27838884567", "Hi there", "Developer"));
        String hash = archive.findById("0012345678").getMessageHash();
        assertEquals(hash, archive.findById("0099999999").getMessageHash());
        archive.deleteByHash(hash);
        assertEquals(1, archive.getStoredMessages().length);
        assertEquals(1, new MessageArchive(tempFolder.resolve("hash.json")).getStoredMessages().length);
    }
}
