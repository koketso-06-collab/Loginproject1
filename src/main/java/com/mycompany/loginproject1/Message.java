package com.mycompany.loginproject1;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Part 2 message model retained for Part 3: ID, hash, validations and actions.
 * The sender field is added for the Part 3 stored-message report.
 */
public class Message {
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final Set<String> generatedIds = new HashSet<>();
    private static int totalMessages;

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;
    private final String sender;
    private boolean sent;
    private boolean stored;

    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this(messageID, messageNumber, recipient, messageText, "Unknown sender");
    }

    public Message(String messageID, int messageNumber, String recipient,
                   String messageText, String sender) {
        this(messageID, messageNumber, recipient, messageText, sender, null, false);
    }

    private Message(String id, int number, String recipient, String text,
                    String sender, String savedHash, boolean restored) {
        this.messageID = id == null ? generateID() : id;
        this.messageNumber = number;
        this.recipient = recipient;
        this.messageText = text;
        this.sender = sender == null || sender.isBlank() ? "Unknown sender" : sender;
        this.messageHash = savedHash == null ? createMessageHash() : savedHash;
        this.stored = restored;
    }

    public Message(int messageNumber, String recipient, String messageText) {
        this(null, messageNumber, recipient, messageText);
    }

    public Message(int messageNumber, String recipient, String messageText, String sender) {
        this(null, messageNumber, recipient, messageText, sender);
    }

    /** Restores the exact original message hash and ID from the existing JSON. */
    static Message fromStoredJson(String id, String hash, String recipient,
                                  String text, String sender) {
        if (id == null || !id.matches("\\d{10}") || hash == null || hash.isBlank()
                || recipient == null || text == null) {
            throw new IllegalArgumentException("Invalid stored message fields");
        }
        int number = 0;
        String[] parts = hash.split(":", 3);
        if (parts.length == 3) {
            try {
                number = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }
        return new Message(id, number, recipient, text, sender, hash, true);
    }

    private static synchronized String generateID() {
        String id;
        do {
            id = String.format(Locale.ROOT, "%010d",
                    ThreadLocalRandom.current().nextLong(10_000_000_000L));
        } while (!generatedIds.add(id));
        return id;
    }

    public boolean checkMessageID() {
        return messageID.matches("\\d{10}");
    }

    public String checkRecipientCell() {
        return validateRecipient(recipient);
    }

    public static String validateRecipient(String recipient) {
        if (recipient != null && recipient.matches("\\+27\\d{9}")) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    public String checkMessageLength() {
        return validateMessageLength(messageText);
    }

    public static String validateMessageLength(String messageText) {
        if (messageText != null && !messageText.isBlank() && messageText.length() <= 250) {
            return "Message ready to send.";
        }
        if (messageText != null && messageText.length() > 250) {
            return "Message exceeds 250 characters by " + (messageText.length() - 250)
                    + ", please reduce the size.";
        }
        return "Please enter a message of less than 250 characters.";
    }

    /** Example: 00:0:HITONIGHT using the first and last words. */
    public String createMessageHash() {
        String prefix = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;
        String text = messageText == null ? "" : messageText.trim();
        if (text.isEmpty()) {
            return (prefix + ":" + messageNumber + ":").toUpperCase(Locale.ROOT);
        }
        String[] words = text.split("\\s+");
        String first = words[0].replaceAll("[^\\p{L}\\p{N}]", "");
        String last = words[words.length - 1].replaceAll("[^\\p{L}\\p{N}]", "");
        return (prefix + ":" + messageNumber + ":" + first + last)
                .toUpperCase(Locale.ROOT);
    }

    /** Part 2 spelling and status messages are preserved for backwards compatibility. */
    public String SentMessage(String choice) {
        if (choice == null) return "Invalid message option.";
        switch (choice.trim().toLowerCase(Locale.ROOT)) {
            case "send":
                if (!sent) {
                    sentMessages.add(this);
                    totalMessages++;
                    sent = true;
                }
                return "Message successfully sent.";
            case "disregard":
            case "discard":
                return "Press 0 to delete the message.";
            case "store":
                try {
                    storeMessage();
                    return "Message successfully stored.";
                } catch (UncheckedIOException exception) {
                    return "Could not store message: " + exception.getCause().getMessage();
                }
            default:
                return "Invalid message option.";
        }
    }

    public String SentMessage(int choice) {
        switch (choice) {
            case 1: return SentMessage("Send");
            case 2: return SentMessage("Disregard");
            case 3: return SentMessage("Store");
            default: return "Invalid message option.";
        }
    }

    public String printMessages() {
        StringBuilder result = new StringBuilder();
        for (Message message : sentMessages) {
            if (result.length() > 0) result.append(System.lineSeparator()).append(System.lineSeparator());
            result.append(message);
        }
        return result.toString();
    }

    public int returnTotalMessages() { return totalMessages; }
    public static int getTotalSentCount() { return totalMessages; }

    public String storeMessage() { return storeMessage(Path.of("messages.json")); }

    /** Saves using a validated JSON reader/writer, preserving previous messages. */
    public String storeMessage(Path file) {
        if (stored) return MessageJsonStore.toJsonObject(this);
        String json = MessageJsonStore.append(file, this);
        stored = true;
        return json;
    }

    public String getMessageID() { return messageID; }
    public String getMessageHash() { return messageHash; }
    public String getRecipient() { return recipient; }
    public String getMessageText() { return messageText; }
    public int getMessageNumber() { return messageNumber; }
    public String getSender() { return sender; }
    public boolean isSent() { return sent; }
    public boolean isStored() { return stored; }

    static void resetSessionForTests() {
        sentMessages.clear();
        generatedIds.clear();
        totalMessages = 0;
    }

    @Override
    public String toString() {
        return "Message ID: " + messageID
                + "\nMessage Hash: " + messageHash
                + "\nRecipient: " + recipient
                + "\nMessage: " + messageText;
    }
}