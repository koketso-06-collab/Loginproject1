# Part 3 testing and lecturer-rubric notes

## Local checks performed while constructing this project

- Main Java sources compiled successfully with `javac --release 17`.
- A complete live console script exercised Part 1 registration/login, three Part 2 message actions, Part 3 menu option 4, stored senders, longest stored message, recipient search and report, then Quit. It produced a JSON file read successfully by a separate JSON parser.
- **39 test methods** across the 5 `*Test.java` classes were compiled and passed in a *temporary locally built JUnit-compatible assertions/annotation harness*. This is a code-level local check, **not a genuine Maven/JUnit 5 or GitHub Actions execution**. Maven/JUnit and GitHub still need to be run/verified by you.
- The full project includes JUnit 5 dependencies and standard `@Test` test classes, ready for `mvn clean test`.

## Test cases taken from the lecturer's Part 3 question pages

| # | Recipient / ID | Text | Status |
| --- | --- | --- | --- |
| 1 | `+27834557896` | `Did you get the cake?` | Sent |
| 2 | `+27838884567` | `Where are you? You are late! I have asked you to be on time.` | Stored |
| 3 | `+27834484567` | `Yohoooo, I am at your gate.` | Disregarded |
| 4 | ID `0838884567` (fixed for deterministic unit test) | `It is dinner time !` | Sent |
| 5 | `+27838884567` | `Ok, I am leaving without you.` | Stored |

The Part 3 images label message 4 `Developer 0838884567` rather than showing a valid `+27...` recipient. The test fixture interprets `0838884567` as the **message ID**, consistent with the subsequent Search-for-ID test, and supplies a valid example recipient so Part 1/2 cell validation remains intact. **No test fixture is inserted automatically when the real app starts.**

## Required expected behaviors

1. Sent array = `Did you get the cake?`, `It is dinner time !`.
2. Disregarded array = `Yohoooo, I am at your gate.`.
3. Stored array = `Where are you? You are late! I have asked you to be on time.`, `Ok, I am leaving without you.`.
4. Longest stored message = `Where are you? You are late! I have asked you to be on time.`.
5. Search for ID `0838884567` = `It is dinner time !`.
6. Search recipient `+27838884567` = both stored texts listed above.
7. Delete using message 2's hash = message 2 removed from the stored array and `messages.json`.
8. Report contains sender, status, recipient, ID, hash, and text for every sent/stored entry.
9. JSON persists across application restarts. Existing Part 2 JSON without `Sender` is still readable.
10. Registration, login, 250-character message limit, recipient validation, ten-digit ID, hash format, menu/loops and send count remain functional.

## Test classes

- `LoginTest` (Part 1 regression)
- `MessageTest` (Part 2 regression)
- `QuickChatTest` (Part 2 interactive regression; updated menu range to 1–4)
- `MessageArchiveTest` (Part 3 required arrays, longest, search, ID, hash deletion, JSON reload, report, edge cases)
- `QuickChatPart3Test` (Part 3 console integration and restart)

## GitHub Actions

`.github/workflows/TestJava.yml` runs `mvn --batch-mode --no-transfer-progress clean test` on pushes and pull requests, which executes the complete test source set (including the new Part 3 tests). The project's default package path and main class remain consistent with `Loginproject1`.

Verify this workflow after you push to GitHub. It has not been run in the user's repository from this environment.
