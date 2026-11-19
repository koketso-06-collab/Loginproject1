package com.mycompany.loginproject1;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Part 3 live message arrays and searches. Arrays are populated from user
 * actions (Send / Disregard / Store) and messages read from JSON, never from
 * hard-coded demonstration data. Parallel ID/hash arrays align with allMessages.
 */
public class MessageArchive {
    private final Path jsonFile;
    private final List<Message> sent = new ArrayList<>();
    private final List<Message> disregarded = new ArrayList<>();
    private final List<Message> stored;

    // Arrays explicitly required by Part 3, refreshed after any change.
    private String[] sentMessages = new String[0];
    private String[] disregardedMessages = new String[0];
    private String[] storedMessages = new String[0];
    private String[] messageHashes = new String[0];
    private String[] messageIds = new String[0];
    private String[] recipients = new String[0];
    private Message[] allMessages = new Message[0];

    public MessageArchive(Path jsonFile) {
        this.jsonFile = jsonFile;
        this.stored = new ArrayList<>(MessageJsonStore.read(jsonFile));
        refreshArrays();
    }

    public void addSent(Message message) {
        if (!sent.contains(message)) {
            message.SentMessage("Send");
            sent.add(message);
            refreshArrays();
        }
    }

    public void addDisregarded(Message message) {
        if (!disregarded.contains(message)) {
            disregarded.add(message);
            refreshArrays();
        }
    }

    public void addStored(Message message) {
        // Update the JSON first; never show an entry as stored if persistence failed.
        message.storeMessage(jsonFile);
        if (!stored.contains(message)) {
            stored.add(message);
            refreshArrays();
        }
    }

    /** Rebuild parallel arrays in exactly the same order after every action. */
    private void refreshArrays() {
        sentMessages = sent.stream().map(Message::getMessageText).toArray(String[]::new);
        disregardedMessages = disregarded.stream().map(Message::getMessageText).toArray(String[]::new);
        storedMessages = stored.stream().map(Message::getMessageText).toArray(String[]::new);

        List<Message> all = new ArrayList<>();
        all.addAll(stored);
        all.addAll(sent);
        all.addAll(disregarded);
        allMessages = all.toArray(new Message[0]);
        messageIds = Arrays.stream(allMessages).map(Message::getMessageID).toArray(String[]::new);
        recipients = Arrays.stream(allMessages).map(Message::getRecipient).toArray(String[]::new);
        messageHashes = Arrays.stream(allMessages).map(Message::getMessageHash).toArray(String[]::new);
    }

    public String[] getSentMessages() { return sentMessages.clone(); }
    public String[] getDisregardedMessages() { return disregardedMessages.clone(); }
    public String[] getStoredMessages() { return storedMessages.clone(); }
    public String[] getMessageHashes() { return messageHashes.clone(); }
    public String[] getMessageIds() { return messageIds.clone(); }

    public String storedSendersAndRecipients() {
        if (stored.isEmpty()) return "No stored messages found.";
        StringBuilder result = new StringBuilder();
        for (Message item : stored) {
            result.append("Sender: ").append(item.getSender())
                    .append(" | Recipient: ").append(item.getRecipient()).append('\n');
        }
        return result.toString().stripTrailing();
    }

    /** Longest stored text; when equal, the earliest stored message wins. */
    public Message longestStoredMessage() {
        if (storedMessages.length == 0) return null;
        int longestIndex = 0;
        for (int i = 1; i < storedMessages.length; i++) {
            if (storedMessages[i].length() > storedMessages[longestIndex].length()) {
                longestIndex = i;
            }
        }
        return stored.get(longestIndex);
    }

    /** Look up a message by ID in parallel arrays; exclude discarded messages. */
    public Message findById(String messageId) {
        for (int i = 0; i < messageIds.length; i++) {
            if (messageIds[i].equals(messageId)
                    && !disregarded.contains(allMessages[i])) {
                return allMessages[i];
            }
        }
        return null;
    }

    /** Searches sent AND stored, as requested by Part 3's unit-test table. */
    public List<Message> findByRecipient(String recipient) {
        List<Message> result = new ArrayList<>();
        for (int i = 0; i < recipients.length; i++) {
            if (recipients[i].equals(recipient) && !disregarded.contains(allMessages[i])) {
                result.add(allMessages[i]);
            }
        }
        return result;
    }

    /** Delete a stored record by hash, including the persisted JSON copy. */
    public Message deleteByHash(String hash) {
        for (Message message : stored) {
            if (message.getMessageHash().equals(hash)) {
                // File is updated before memory; on an I/O error memory stays unchanged.
                if (MessageJsonStore.deleteByHash(jsonFile, hash)) {
                    stored.remove(message);
                    refreshArrays();
                    return message;
                }
                return null;
            }
        }
        return null;
    }

    public String messageReport() {
        StringBuilder report = new StringBuilder("=== QuickChat Message Report ===\n");
        int count = 0;
        for (Message message : allMessages) {
            if (disregarded.contains(message)) continue;
            count++;
            report.append("\nMessage ").append(count)
                    .append("\nStatus: ").append(stored.contains(message) ? "Stored" : "Sent")
                    .append("\nSender: ").append(message.getSender())
                    .append("\n").append(message)
                    .append('\n');
        }
        if (count == 0) report.append("No sent or stored messages to report.\n");
        return report.toString().stripTrailing();
    }
}
