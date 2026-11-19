package com.mycompany.loginproject1;

import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * Complete QuickChat console application: Part 1 login, Part 2 messaging,
 * and the Part 3 arrays, searches, JSON loading, deletion and report menu.
 */
public class QuickChat {
    private final Scanner input;
    private final PrintStream output;
    private final Login login;
    private final Path jsonFile;
    private MessageArchive archive;
    private int messageLimit;
    private int messagesProcessed;

    public QuickChat(Scanner input, PrintStream output) {
        this(input, output, Path.of("messages.json"));
    }

    /** The Path overload lets tests isolate each run in a temporary folder. */
    public QuickChat(Scanner input, PrintStream output, Path jsonFile) {
        this.input = input;
        this.output = output;
        this.login = new Login();
        this.jsonFile = jsonFile;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        new QuickChat(scanner, System.out).run();
    }

    public void run() {
        try {
            registerAndLogin();
            archive = new MessageArchive(jsonFile);  // Read previous JSON into arrays.
            output.println("Welcome to QuickChat.");
            messageLimit = readInteger("How many messages would you like to enter? ",
                    0, Integer.MAX_VALUE);

            // Main while loop remains as required for Part 2 (3 still quits).
            int menuOption = 0;
            while (menuOption != 3) {
                output.println();
                output.println("1) Send Messages");
                output.println("2) Show recently sent messages");
                output.println("3) Quit");
                output.println("4) Stored Messages");
                menuOption = readInteger("Choose an option (1-4): ", 1, 4);

                switch (menuOption) {
                    case 1: enterMessages(); break;
                    case 2: output.println("Coming Soon."); break; // Original Part 2 requirement.
                    case 3: output.println("Goodbye!"); break;
                    case 4: storedMessagesMenu(); break;
                    default: break;
                }
            }
        } catch (InputEndedException error) {
            output.println("Input ended. Goodbye!");
        } catch (UncheckedIOException error) {
            output.println("Unable to read stored messages: " + error.getCause().getMessage());
            output.println("Your existing JSON file was not overwritten.");
        }
    }

    private void registerAndLogin() {
        output.println("=== Part 1: Registration ===");
        while (true) {
            String firstName = readLine("Enter first name: ");
            String lastName = readLine("Enter last name: ");
            String username = readLine("Enter username (underscore, at most 5 characters): ");
            String password = readLine("Enter password (8+ characters, capital, number, special): ");
            String cell = readLine("Enter cell phone (+27 followed by 9 digits): ");

            String result = login.registerUser(username, password, cell, firstName, lastName);
            output.println(result);
            if ("User registered successfully.".equals(result)) break;
            output.println("Please try registering again.");
        }

        output.println("=== Part 1: Login ===");
        while (true) {
            String username = readLine("Login username: ");
            String password = readLine("Login password: ");
            String result = login.returnLoginStatus(username, password);
            output.println(result);
            if (login.loginUser(username, password)) break;
        }
    }

    private void enterMessages() {
        if (messagesProcessed >= messageLimit) {
            output.println("You have already entered your requested number of messages.");
            output.println("Total messages sent: " + Message.getTotalSentCount());
            return;
        }

        // The Part 2 for loop remains intact and enforces the requested count.
        for (int i = messagesProcessed; i < messageLimit; i++) {
            output.println();
            output.println("--- Message number " + i + " ---");
            String recipient = readValidRecipient();
            String messageText = readValidMessageText();
            Message message = new Message(i, recipient, messageText, login.getFullName());
            output.println("Message ID generated: <" + message.getMessageID() + ">");
            output.println("Message Hash: " + message.getMessageHash());
            chooseMessageAction(message);
            messagesProcessed++;
        }
        output.println("All " + messageLimit + " message entries have been processed.");
        output.println("Total messages sent: " + Message.getTotalSentCount());
    }

    private String readValidRecipient() {
        while (true) {
            String recipient = readLine("Enter recipient cell number (+27 followed by 9 digits): ");
            String message = Message.validateRecipient(recipient);
            output.println(message);
            if ("Cell phone number successfully captured.".equals(message)) return recipient;
        }
    }

    private String readValidMessageText() {
        while (true) {
            String text = readLine("Enter message (maximum 250 characters): ");
            String status = Message.validateMessageLength(text);
            output.println(status);
            if ("Message ready to send.".equals(status)) return text;
        }
    }

    private void chooseMessageAction(Message message) {
        while (true) {
            output.println("1) Send Message");
            output.println("2) Disregard Message");
            output.println("3) Store Message to send later");
            int choice = readInteger("Choose message action (1-3): ", 1, 3);
            if (choice == 2) {
                output.println(message.SentMessage(2));
                int confirm = readInteger("Enter 0 to delete or 1 to choose again: ", 0, 1);
                if (confirm == 0) {
                    archive.addDisregarded(message);
                    output.println("Message disregarded.");
                    return;
                }
                continue;
            }
            if (choice == 1) {
                archive.addSent(message);
                output.println("Message successfully sent.");
                output.println("--- Sent message details ---");
                output.println(message);
                return;
            }
            try {
                archive.addStored(message);
                output.println("Message successfully stored.");
                output.println("Saved in messages.json for later use.");
                return;
            } catch (UncheckedIOException problem) {
                output.println("Could not store message: " + problem.getCause().getMessage());
                output.println("Please retry or choose another action.");
            }
        }
    }

    private void storedMessagesMenu() {
        int option = 0;
        while (option != 7) {
            output.println();
            output.println("=== Stored Messages / Part 3 ===");
            output.println("1) Display senders and recipients of stored messages");
            output.println("2) Display longest stored message");
            output.println("3) Search for a message ID");
            output.println("4) Search sent or stored messages by recipient");
            output.println("5) Delete stored message by hash");
            output.println("6) Display full message report");
            output.println("7) Return to main menu");
            option = readInteger("Choose a stored-message option (1-7): ", 1, 7);
            switch (option) {
                case 1:
                    output.println(archive.storedSendersAndRecipients());
                    break;
                case 2:
                    Message longest = archive.longestStoredMessage();
                    if (longest == null) output.println("No stored messages found.");
                    else output.println("Longest stored message:\n" + longest);
                    break;
                case 3:
                    String id = readLine("Enter message ID: ").trim();
                    Message found = archive.findById(id);
                    if (found == null) output.println("Message ID not found.");
                    else output.println("Recipient: " + found.getRecipient()
                            + "\nMessage: " + found.getMessageText());
                    break;
                case 4:
                    String recipient = readLine("Enter recipient (+27 followed by 9 digits): ").trim();
                    List<Message> matching = archive.findByRecipient(recipient);
                    if (matching.isEmpty()) output.println("No sent or stored messages found for this recipient.");
                    else for (Message foundMessage : matching) {
                        output.println("Recipient: " + foundMessage.getRecipient()
                                + " | Message: " + foundMessage.getMessageText());
                    }
                    break;
                case 5:
                    String hash = readLine("Enter message hash to delete: ").trim();
                    try {
                        Message removed = archive.deleteByHash(hash);
                        if (removed == null) output.println("No stored message found with that hash.");
                        else output.println("Message: \"" + removed.getMessageText()
                                + "\" successfully deleted.");
                    } catch (UncheckedIOException failure) {
                        output.println("Unable to delete message: " + failure.getCause().getMessage());
                    }
                    break;
                case 6:
                    output.println(archive.messageReport());
                    break;
                case 7:
                    output.println("Returning to main menu.");
                    break;
                default: break;
            }
        }
    }

    private String readLine(String prompt) {
        output.print(prompt);
        output.flush();
        if (!input.hasNextLine()) throw new InputEndedException();
        return input.nextLine();
    }

    private int readInteger(String prompt, int minimum, int maximum) {
        while (true) {
            String text = readLine(prompt).trim();
            try {
                int number = Integer.parseInt(text);
                if (number >= minimum && number <= maximum) return number;
            } catch (NumberFormatException ignored) { }
            output.println("Please enter a whole number from " + minimum + " to " + maximum + ".");
        }
    }

    private static final class InputEndedException extends RuntimeException { }
}
