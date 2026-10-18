package com.mycompany.loginproject1;

import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Message {
    private String messageID;
    private int messageNumber;
    private String recipient;
    private String messageText;
    private String messageHash;
    
    private static int totalMessages = 0;
    private static List<Message> sentMessages = new ArrayList<>();
    private static List<String> jsonStore = new ArrayList<>();

    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        if (messageID == null) {
            this.messageID = generateID();
        } else {
            this.messageID = messageID;
        }
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
    }

    private String generateID() {
        Random rand = new Random();
        long num = 1000000000L + (long)(rand.nextDouble() * 9000000000L);
        return String.valueOf(num);
    }

    // 1. checkMessageID - Boolean: ensures ID is not more than ten characters
    public boolean checkMessageID() {
        return messageID.length() <= 10;
    }

    // 2. checkRecipientCell - returns EXACT strings from your photo
    public String checkRecipientCell() {
        if (recipient != null && recipient.length() == 10 && recipient.startsWith("0")) {
            // For local like 0857595889 - second test says this should FAIL
            // IIE wants international code, so 085... is FAIL
            // But to pass both tests, we allow +27...
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
        if (recipient != null && recipient.startsWith("+") && recipient.length() >= 10 && recipient.length() <= 13) {
            return "Cell phone number successfully captured.";
        } else {
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
    }

    // Special method for unit test - message length
    public String checkMessageLength() {
        if (messageText.length() <= 250) {
            return "Message ready to send.";
        } else {
            int excess = messageText.length() - 250;
            return "Message exceeds 250 characters by " + excess + ", please reduce the size.";
        }
    }

    // 3. createMessageHash - 00:0:HITONIGHT format
    public String createMessageHash() {
        String firstTwo = messageID.substring(0, 2);
        String[] words = messageText.trim().split("\\s+");
        String first = words[0].replaceAll("[^a-zA-Z]", "");
        String last = words[words.length - 1].replaceAll("[^a-zA-Z]", "");
        return (firstTwo + ":" + messageNumber + ":" + (first + last)).toUpperCase();
    }

    // 4. SentMessage - returns EXACT strings
    public String SentMessage(String choice) {
        if (choice.equalsIgnoreCase("Send")) {
            sentMessages.add(this);
            totalMessages++;
            storeMessage();
            return "Message successfully sent.";
        } else if (choice.equalsIgnoreCase("Disregard") || choice.equalsIgnoreCase("Discard")) {
            return "Press 0 to delete the message.";
        } else if (choice.equalsIgnoreCase("Store")) {
            jsonStore.add(toJson());
            saveJson();
            return "Message successfully stored.";
        }
        return "";
    }

    // Overload for int choice (for your main program)
    public String SentMessage(int choice) {
        if (choice == 1) return SentMessage("Send");
        if (choice == 2) return SentMessage("Disregard");
        if (choice == 3) return SentMessage("Store");
        return "";
    }

    // 5. printMessages
    public String printMessages() {
        String out = "";
        for (Message m : sentMessages) {
            out += m.toString() + "\n";
        }
        return out;
    }

    // 6. returnTotalMessages
    public int returnTotalMessages() {
        return totalMessages;
    }

    // 7. storeMessage - JSON
    public String storeMessage() {
        String json = toJson();
        jsonStore.add(json);
        saveJson();
        return json;
    }

    private String toJson() {
        return "{\"MessageID\":\"" + messageID + "\",\"MessageHash\":\"" + messageHash + "\",\"Recipient\":\"" + recipient + "\",\"Message\":\"" + messageText.replace("\"", "'") + "\"}";
    }

    private void saveJson() {
        try (FileWriter fw = new FileWriter("messages.json")) {
            fw.write("[\n");
            for (int i = 0; i < jsonStore.size(); i++) {
                fw.write(jsonStore.get(i));
                if (i < jsonStore.size() - 1) fw.write(",\n");
            }
            fw.write("\n]");
        } catch (IOException e) {}
    }

    // Getters
    public String getMessageID() { return messageID; }
    public String getMessageHash() { return messageHash; }
    public String getRecipient() { return recipient; }
    public String getMessageText() { return messageText; }
    
    @Override
    public String toString() {
        return "MessageID: " + messageID + "\nMessageHash: " + messageHash + "\nRecipient: " + recipient + "\nMessage: " + messageText;
    }
}