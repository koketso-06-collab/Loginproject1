# QuickChat — Programming POE Part 3 (Parts 1 + 2 + 3 integrated)

This **Java 17 console application** continues the existing `Loginproject1` Maven/NetBeans project, keeping the Part 1 `Login` class and the Part 2 `Message` and `QuickChat` method names. It adds the Part 3 arrays, message searches, JSON reload, deletion and reports. There is **no hard-coded demonstration data in the application**: data is entered by the user or read from `messages.json`.

## Run in Apache NetBeans

1. Extract the ZIP archive.
2. In NetBeans select **File > Open Project** and select its `Loginproject1` directory (the one containing `pom.xml`).
3. Use **Run Project**. The Maven main class in `pom.xml` is `com.mycompany.loginproject1.QuickChat`.
4. Register a user with the same Part 1 requirements (username with underscore and at most five characters; complex password; `+27` + nine digits), then log in using the registered credentials.
5. Enter the number of messages to process. Main menu:
   - **1 Send Messages** — repeats for that number of entries; choose Send, Disregard (confirm `0`), or Store for each.
   - **2 Show recently sent messages** — displays `Coming Soon.` (this is retained verbatim from the Part 2 specification).
   - **3 Quit** — ends the application.
   - **4 Stored Messages** — Part 3 submenu with the six required actions, plus Return.
6. For Part 3 choose main menu `4`; the actions are:
   1. Display senders and recipients of stored messages.
   2. Display the longest stored message with its ID, hash, recipient and text.
   3. Search by message ID and show recipient and message text.
   4. Search by recipient across **sent and stored** messages (matching the Part 3 test table).
   5. Delete a stored message by its hash, from both the arrays **and** `messages.json`.
   6. Display the report of sent and stored message details: status, sender, ID, hash, recipient, text.
   7. Return to the main menu.

## Build and run using Maven

From the `Loginproject1` directory:

```sh
mvn clean test
mvn compile exec:java
```

If Maven is not installed but JDK 17+ is present, the **application only** can be compiled using the JDK:

```sh
javac -encoding UTF-8 -d target/classes src/main/java/com/mycompany/loginproject1/*.java
java -cp target/classes com.mycompany.loginproject1.QuickChat
```

The JUnit 5 tests still require Maven and dependency downloads (or the JUnit jars through NetBeans). No external dependency is required for the main application.

## Source layout and what each class does

| Path | Purpose |
| --- | --- |
| `src/main/java/com/mycompany/loginproject1/Login.java` | Original Part 1 registration / login validations; provides full name for stored-message sender |
| `src/main/java/com/mycompany/loginproject1/Message.java` | Part 2 ID, hash, validation, sending, disregarding, storing and sent totals |
| `src/main/java/com/mycompany/loginproject1/QuickChat.java` | Console UI: Part 1 login + Part 2 `while`/`for` loops + Part 3 menu |
| `src/main/java/com/mycompany/loginproject1/MessageArchive.java` | Part 3 sent / disregarded / stored message arrays, parallel IDs / hashes / recipients, searches, deletion, report |
| `src/main/java/com/mycompany/loginproject1/MessageJsonStore.java` | JSON file reading, escaping, validation and safe on-disk updates |
| `src/test/java/com/mycompany/loginproject1/*Test.java` | Part 1, Part 2 and Part 3 JUnit 5 unit/integration tests |
| `.github/workflows/TestJava.yml` | GitHub Actions Maven test workflow |
| `TESTING_NOTES.md` | Test cases, expected outputs, and limitations |
| `messages.example.json` | Empty JSON example; the program writes the real `messages.json` automatically |

## Part 3 rubric cross-check

| Requirement | Where it is implemented/tested |
| --- | --- |
| Arrays populated by user actions and JSON, not hard-coded | `MessageArchive.refreshArrays()`, `MessageArchiveTest.sentDisregardedStoredIdAndHashArraysContainCorrectData` |
| Display sender and recipient for stored messages | `MessageArchive.storedSendersAndRecipients()` |
| Longest stored message | `MessageArchive.longestStoredMessage()` |
| Search message ID | `MessageArchive.findById()` |
| Find all messages for recipient | `MessageArchive.findByRecipient()` searches both sent and stored |
| Delete by message hash | `MessageArchive.deleteByHash()` and `MessageJsonStore.deleteByHash()` |
| Read existing JSON into the stored array | `MessageJsonStore.read()` called by `MessageArchive(Path)` |
| Full message report | `MessageArchive.messageReport()` |
| Five prescribed test examples | `MessageArchiveTest.rubricData()` uses examples for sent, stored, disregarded |
| JUnit tests for original and new features | `LoginTest`, `MessageTest`, `QuickChatTest`, `MessageArchiveTest`, `QuickChatPart3Test` |
| Automated unit tests | `.github/workflows/TestJava.yml` runs `mvn clean test` |
| Full end-to-end functionality | `QuickChatPart3Test` runs actual console scripts through Parts 1, 2 and 3 |

### Notes about the test data

- The assignment's **message 4** gives `0838884567` as an ID to search for. The JUnit test creates message 4 with this exact ID so that the search has a deterministic expected result. The real app continues to generate **random ten-digit IDs** as required by Part 2; copy the ID from the console if you want to look it up.
- The assignment test for a particular recipient expects *both* stored messages addressed to `+27838884567`. Recipient search includes sent and stored but excludes disregarded messages.
- `messages.json` created under the **working directory** persists stored records between app launches. Sent and disregarded arrays only exist for the current run. A stored message's `Sender` is the registered full name. Earlier Part 2 JSON files lacked a `Sender` field; those records display `Unknown sender` rather than inventing a name.
- The report includes sent **and** stored messages, meeting both the prose requirement about stored messages and the test table's reference to sent messages. Disregarded messages do not appear.
- If an existing JSON file is malformed, the app reports an error rather than overwriting the user's file. Duplicate IDs are rejected; deleting by a hash removes one matching record, including in the JSON file.

## Important GitHub instructions

The ZIP is a **project folder, not a Git repository**. To retain Part 1 and Part 2 commit history, copy the completed files into your existing GitHub repository checkout, then commit and push. Do not replace your `.git` folder. The `.github/workflows/TestJava.yml` workflow is included but has **not** been executed on the user's GitHub repository by this build. Verify a successful green Actions run after pushing.

## Attribution / references

- Existing `Login` and `Message` public API names and required output strings were carried forward from the user's earlier Part 1 and Part 2 projects and the supplied POE question-sheet images.
- Java file handling and UTF-8 use the standard [Java NIO Files API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html); `MessageJsonStore` is an original, small JSON reader/writer limited to the application's array-of-flat-string-objects schema, not a general-purpose JSON library.
- GitHub workflow uses [actions/checkout](https://github.com/actions/checkout) and [actions/setup-java](https://github.com/actions/setup-java).

**Before submitting:** run the project yourself, inspect the source, execute `mvn clean test`, and confirm GitHub Actions succeeds. A packaged source project alone does not guarantee a particular lecturer mark.
