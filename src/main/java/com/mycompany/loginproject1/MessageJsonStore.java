package com.mycompany.loginproject1;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Read and update the existing messages.json array (including the older Part 2
 * four-field format). No third-party JSON dependency is required.
 *
 * The small JSON parser intentionally accepts only an array of flat objects
 * with string keys/values, the exact schema this application produces. It
 * checks the complete input before writing, to avoid corrupting earlier data.
 * Uses Java NIO Files / atomic replacement where supported by the filesystem.
 */
public final class MessageJsonStore {
    private MessageJsonStore() {}

    public static List<Message> read(Path file) {
        if (!Files.exists(file)) return new ArrayList<>();
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            List<Map<String, String>> records = new Parser(json).parse();
            List<Message> messages = new ArrayList<>();
            Set<String> ids = new HashSet<>();
            for (Map<String, String> record : records) {
                Message message;
                try {
                    message = Message.fromStoredJson(record.get("MessageID"),
                            record.get("MessageHash"), record.get("Recipient"),
                            record.get("Message"), record.get("Sender"));
                } catch (IllegalArgumentException error) {
                    throw new IOException("Stored record has missing or invalid fields", error);
                }
                if (!ids.add(message.getMessageID())) {
                    throw new IOException("Duplicate stored message ID: " + message.getMessageID());
                }
                messages.add(message);
            }
            return messages;
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read " + file, exception);
        }
    }

    public static String append(Path file, Message message) {
        List<Message> messages = read(file);
        for (Message existing : messages) {
            if (existing.getMessageID().equals(message.getMessageID())) {
                throw new UncheckedIOException(new IOException("Duplicate message ID"));
            }
        }
        messages.add(message);
        save(file, messages);
        return toJsonObject(message);
    }

    public static boolean deleteByHash(Path file, String hash) {
        List<Message> messages = read(file);
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getMessageHash().equals(hash)) {
                messages.remove(i);
                save(file, messages);
                return true;
            }
        }
        return false;
    }

    private static void save(Path file, List<Message> messages) {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < messages.size(); i++) {
            if (i > 0) json.append(",\n");
            json.append("  ").append(toJsonObject(messages.get(i)));
        }
        json.append("\n]\n");

        Path absolute = file.toAbsolutePath();
        Path parent = absolute.getParent();
        Path temp = null;
        try {
            if (parent != null) Files.createDirectories(parent);
            temp = Files.createTempFile(parent, "quickchat-", ".json.tmp");
            Files.writeString(temp, json.toString(), StandardCharsets.UTF_8);
            try {
                Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not save " + file, exception);
        } finally {
            if (temp != null) {
                try { Files.deleteIfExists(temp); } catch (IOException ignored) { }
            }
        }
    }

    static String toJsonObject(Message message) {
        return "{\"MessageID\":" + quote(message.getMessageID())
                + ",\"MessageHash\":" + quote(message.getMessageHash())
                + ",\"Recipient\":" + quote(message.getRecipient())
                + ",\"Message\":" + quote(message.getMessageText())
                + ",\"Sender\":" + quote(message.getSender()) + "}";
    }

    private static String quote(String raw) {
        StringBuilder result = new StringBuilder("\"");
        for (int i = 0; i < raw.length(); i++) {
            char character = raw.charAt(i);
            switch (character) {
                case '\"': result.append("\\\""); break;
                case '\\': result.append("\\\\"); break;
                case '\n': result.append("\\n"); break;
                case '\r': result.append("\\r"); break;
                case '\t': result.append("\\t"); break;
                case '\b': result.append("\\b"); break;
                case '\f': result.append("\\f"); break;
                default:
                    if (character < 32) {
                        result.append(String.format(java.util.Locale.ROOT, "\\u%04x", (int) character));
                    } else result.append(character);
            }
        }
        return result.append('"').toString();
    }

    /** Strict reader for the flat JSON array format generated above. */
    private static final class Parser {
        private final String text;
        private int index;

        private Parser(String text) { this.text = text; }

        private List<Map<String, String>> parse() throws IOException {
            List<Map<String, String>> result = new ArrayList<>();
            whitespace();
            expect('[');
            whitespace();
            if (!take(']')) {
                do {
                    whitespace();
                    result.add(object());
                    whitespace();
                    if (take(']')) break;
                    expect(',');
                    whitespace();
                } while (true);
            }
            whitespace();
            if (index != text.length()) throw invalid("Trailing content");
            return result;
        }

        private Map<String, String> object() throws IOException {
            Map<String, String> values = new HashMap<>();
            expect('{');
            whitespace();
            if (take('}')) return values;
            while (true) {
                String key = string();
                whitespace();
                expect(':');
                whitespace();
                String value = string();
                if (values.putIfAbsent(key, value) != null) throw invalid("Duplicate key");
                whitespace();
                if (take('}')) return values;
                expect(',');
                whitespace();
            }
        }

        private String string() throws IOException {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (index < text.length()) {
                char ch = text.charAt(index++);
                if (ch == '"') return result.toString();
                if (ch < 32) throw invalid("Unescaped control character");
                if (ch != '\\') { result.append(ch); continue; }
                if (index >= text.length()) throw invalid("Incomplete escape");
                char escape = text.charAt(index++);
                switch (escape) {
                    case '"': result.append('"'); break;
                    case '\\': result.append('\\'); break;
                    case '/': result.append('/'); break;
                    case 'n': result.append('\n'); break;
                    case 'r': result.append('\r'); break;
                    case 't': result.append('\t'); break;
                    case 'b': result.append('\b'); break;
                    case 'f': result.append('\f'); break;
                    case 'u':
                        if (index + 4 > text.length()) throw invalid("Incomplete Unicode escape");
                        String hex = text.substring(index, index + 4);
                        try { result.append((char) Integer.parseInt(hex, 16)); }
                        catch (NumberFormatException exception) { throw invalid("Invalid Unicode escape"); }
                        index += 4;
                        break;
                    default: throw invalid("Unsupported escape sequence");
                }
            }
            throw invalid("Unclosed string");
        }

        private void whitespace() {
            while (index < text.length() && " \t\r\n".indexOf(text.charAt(index)) >= 0) index++;
        }

        private boolean take(char ch) {
            if (index < text.length() && text.charAt(index) == ch) { index++; return true; }
            return false;
        }

        private void expect(char ch) throws IOException {
            if (!take(ch)) throw invalid("Expected '" + ch + "'");
        }

        private IOException invalid(String reason) {
            return new IOException("Invalid messages.json at character " + index + ": " + reason);
        }
    }
}
