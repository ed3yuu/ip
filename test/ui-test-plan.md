# UI Test Plan

This file is the source of truth for console UI test cases. Add one section per case and run them in the order shown. Use exact expected output unless a case explicitly defines a comparison rule.

## Test environment

- Java version: 25
- Working directory: repository root
- Default comparison rule: exact output, including whitespace and line breaks
- Line-ending rule: platform line-ending differences may be normalized to `\n`
- Before each case: remove `data/lobby.txt` whether it is a file or directory, then ensure `data` is a directory, unless the case specifies different storage setup.

## Test cases

### UI-001 Add and list every task type

- Aim: Verify that to-dos, deadlines, and events are stored and displayed with their type-specific details. Deadline dates must be accepted in `yyyy-MM-dd` format and displayed as `MMM dd yyyy`.
- Command: `java -cp out lobby.Lobby`
- Inputs, in order:
  1. `todo borrow book`
  2. `deadline do homework /by 2019-10-15`
  3. `event project meeting /from Mon 2pm /to 4pm`
  4. `mark 2`
  5. `list`
  6. `bye`
- Expected output:

  ```text
  ____________________________________________________________
   _           _     _
  | |    ___  | |__ | |__  _   _
  | |   / _ \ | '_ \| '_ \| | | |
  | |__| (_) | |_) | |_) | |_| |
  |_____\___/|_.__/|_.__/ \__, |
                           |___/
  Hello! I'm Lobby.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
   Got it. I've added this task:
     [T][ ] borrow book
   Now you have 1 task in the list.
  ____________________________________________________________
  ____________________________________________________________
   Got it. I've added this task:
     [D][ ] do homework (by: Oct 15 2019)
   Now you have 2 tasks in the list.
  ____________________________________________________________
  ____________________________________________________________
   Got it. I've added this task:
     [E][ ] project meeting (from: Mon 2pm to: 4pm)
   Now you have 3 tasks in the list.
  ____________________________________________________________
  ____________________________________________________________
   Nice! I've marked this task as done:
     [D][X] do homework (by: Oct 15 2019)
  ____________________________________________________________
  ____________________________________________________________
   Here are the tasks in your list:
   1.[T][ ] borrow book
   2.[D][X] do homework (by: Oct 15 2019)
   3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
  ____________________________________________________________
  ____________________________________________________________
   Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

- Comparison rule: exact, after normalizing Windows line endings to `\n`.
- Setup: Compile all files in `src/main/java` to the `out` folder with Java 25 before running the command.
- Expected saved file (`data/lobby.txt`), after normalizing line endings to `\n`:

  ```text
  T | 0 | borrow book
  D | 1 | do homework | 2019-10-15
  E | 0 | project meeting | Mon 2pm | 4pm
  ```

### UI-002 Reject a to-do without a description

- Aim: Verify that a command beginning with `todo` but missing its description shows a helpful error and does not add a task.
- Command: `java -cp out lobby.Lobby`
- Inputs, in order:
  1. `todo`
  2. `list`
  3. `bye`
- Expected output:

  ```text
  ____________________________________________________________
   _           _     _
  | |    ___  | |__ | |__  _   _
  | |   / _ \ | '_ \| '_ \| | | |
  | |__| (_) | |_) | |_) | |_| |
  |_____\___/|_.__/|_.__/ \__, |
                           |___/
  Hello! I'm Lobby.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
   A to-do needs a description. Try: todo <description>.
  ____________________________________________________________
  ____________________________________________________________
   Here are the tasks in your list:
  ____________________________________________________________
  ____________________________________________________________
   Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

- Comparison rule: exact, after normalizing Windows line endings to `\n`.
- Setup: Compile all files in `src/main/java` to the `out` folder with Java 25 before running the command.

### UI-005 Save the result of every task-list mutation

- Aim: Verify that successful add, mark, unmark, and delete commands leave the save file matching the final task list.
- Command: `java -cp out lobby.Lobby`
- Inputs, in order:
  1. `todo first task`
  2. `mark 1`
  3. `unmark 1`
  4. `todo second task`
  5. `delete 2`
  6. `bye`
- Expected output:

  ```text
  ____________________________________________________________
   _           _     _
  | |    ___  | |__ | |__  _   _
  | |   / _ \ | '_ \| '_ \| | | |
  | |__| (_) | |_) | |_) | |_| |
  |_____\___/|_.__/|_.__/ \__, |
                           |___/
  Hello! I'm Lobby.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
   Got it. I've added this task:
     [T][ ] first task
   Now you have 1 task in the list.
  ____________________________________________________________
  ____________________________________________________________
   Nice! I've marked this task as done:
     [T][X] first task
  ____________________________________________________________
  ____________________________________________________________
   OK, I've marked this task as not done yet:
     [T][ ] first task
  ____________________________________________________________
  ____________________________________________________________
   Got it. I've added this task:
     [T][ ] second task
   Now you have 2 tasks in the list.
  ____________________________________________________________
  ____________________________________________________________
   Noted. I've removed this task:
     [T][ ] second task
   Now you have 1 task in the list.
  ____________________________________________________________
  ____________________________________________________________
   Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

- Comparison rule: exact, after normalizing Windows line endings to `\n`.
- Setup: Compile all files in `src/main/java` to the `out` folder with Java 25 before running the command.
- Expected saved file (`data/lobby.txt`), after normalizing line endings to `\n`:

  ```text
  T | 0 | first task
  ```

### UI-006 Load saved tasks on startup

- Aim: Verify that all task types and their completion states are restored from the save file when Lobby starts.
- Command: `java -cp out lobby.Lobby`
- Inputs, in order:
  1. `list`
  2. `bye`
- Expected output:

  ```text
  ____________________________________________________________
   _           _     _
  | |    ___  | |__ | |__  _   _
  | |   / _ \ | '_ \| '_ \| | | |
  | |__| (_) | |_) | |_) | |_| |
  |_____\___/|_.__/|_.__/ \__, |
                           |___/
  Hello! I'm Lobby.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
   Here are the tasks in your list:
   1.[T][X] read book
   2.[D][ ] return book (by: Jun 06 2019)
   3.[E][X] project meeting (from: Aug 6th 2pm to: 4pm)
  ____________________________________________________________
  ____________________________________________________________
   Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

- Comparison rule: exact, after normalizing Windows line endings to `\n`.
- Setup:
  1. Compile all files in `src/main/java` to the `out` folder with Java 25.
  2. Create `data/lobby.txt` with this exact UTF-8 content:

     ```text
     T | 1 | read book
     D | 0 | return book | 2019-06-06
     E | 1 | project meeting | Aug 6th 2pm | 4pm
     ```

### UI-007 Recover valid tasks from a partially corrupted save file

- Aim: Verify that blank and malformed records are skipped while valid escaped fields, task types, and completion states still load.
- Command: `java -cp out lobby.Lobby`
- Inputs, in order:
  1. `list`
  2. `bye`
- Expected output:

  ```text
  ____________________________________________________________
   _           _     _
  | |    ___  | |__ | |__  _   _
  | |   / _ \ | '_ \| '_ \| | | |
  | |__| (_) | |_) | |_) | |_| |
  |_____\___/|_.__/|_.__/ \__, |
                           |___/
  Hello! I'm Lobby.
  What can I do for you?
  ____________________________________________________________
   I skipped 5 invalid lines while loading data/lobby.txt.
  ____________________________________________________________
  ____________________________________________________________
   Here are the tasks in your list:
   1.[T][X] valid | todo
   2.[T][ ] open C:\Temp
   3.[D][ ] return book (by: Jun 06 2019)
   4.[E][X] project meeting (from: Aug 6th 2pm to: 4pm)
  ____________________________________________________________
  ____________________________________________________________
   Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

- Comparison rule: exact, after normalizing Windows line endings to `\n`.
- Setup:
  1. Compile all files in `src/main/java` to the `out` folder with Java 25.
  2. Create `data/lobby.txt` with this exact UTF-8 content, including the blank line:

     ```text
     T | 1 | valid \| todo
     T | 0 | open C:\\Temp
     D | 0 | return book | 2019-06-06
     E | 1 | project meeting | Aug 6th 2pm | 4pm

     T | 2 | invalid status
     D | 0 | missing date
     E | 0 | blank end | Mon |
     X | 0 | unknown type
     T | 0 | too | many fields
     ```

### UI-008 Roll back a task when saving fails

- Aim: Verify that a failed write reports a helpful error, keeps the chatbot running, and does not retain the unsaved task in memory.
- Command: `java -cp out lobby.Lobby`
- Inputs, in order:
  1. `todo cannot save`
  2. `list`
  3. `bye`
- Expected output:

  ```text
  ____________________________________________________________
   _           _     _
  | |    ___  | |__ | |__  _   _
  | |   / _ \ | '_ \| '_ \| | | |
  | |__| (_) | |_) | |_) | |_| |
  |_____\___/|_.__/|_.__/ \__, |
                           |___/
  Hello! I'm Lobby.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
   I couldn't save your changes. Please check that data/lobby.txt is writable.
  ____________________________________________________________
  ____________________________________________________________
   Here are the tasks in your list:
  ____________________________________________________________
  ____________________________________________________________
   Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

- Comparison rule: exact, after normalizing Windows line endings to `\n`.
- Setup:
  1. Compile all files in `src/main/java` to the `out` folder with Java 25.
  2. Replace the `data` directory with a regular file named `data`, preventing creation of `data/lobby.txt`.

### UI-009 Recover when the save path cannot be read

- Aim: Verify that an unreadable save path reports a helpful warning and starts with an empty task list instead of crashing.
- Command: `java -cp out lobby.Lobby`
- Inputs, in order:
  1. `list`
  2. `bye`
- Expected output:

  ```text
  ____________________________________________________________
   _           _     _
  | |    ___  | |__ | |__  _   _
  | |   / _ \ | '_ \| '_ \| | | |
  | |__| (_) | |_) | |_) | |_| |
  |_____\___/|_.__/|_.__/ \__, |
                           |___/
  Hello! I'm Lobby.
  What can I do for you?
  ____________________________________________________________
   I couldn't read data/lobby.txt, so I started with an empty task list.
  ____________________________________________________________
  ____________________________________________________________
   Here are the tasks in your list:
  ____________________________________________________________
  ____________________________________________________________
   Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

- Comparison rule: exact, after normalizing Windows line endings to `\n`.
- Setup:
  1. Compile all files in `src/main/java` to the `out` folder with Java 25.
  2. Create a directory at `data/lobby.txt`, so that path cannot be read as a file.

### UI-010 Escape storage separators and backslashes

- Aim: Verify that literal pipe and backslash characters in a task description are escaped in the save file without changing the displayed task.
- Command: `java -cp out lobby.Lobby`
- Inputs, in order:
  1. `todo use A | B \ C`
  2. `bye`
- Expected output:

  ```text
  ____________________________________________________________
   _           _     _
  | |    ___  | |__ | |__  _   _
  | |   / _ \ | '_ \| '_ \| | | |
  | |__| (_) | |_) | |_) | |_| |
  |_____\___/|_.__/|_.__/ \__, |
                           |___/
  Hello! I'm Lobby.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
   Got it. I've added this task:
     [T][ ] use A | B \ C
   Now you have 1 task in the list.
  ____________________________________________________________
  ____________________________________________________________
   Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

- Comparison rule: exact, after normalizing Windows line endings to `\n`.
- Setup: Compile all files in `src/main/java` to the `out` folder with Java 25 before running the command.
- Expected saved file (`data/lobby.txt`), after normalizing line endings to `\n`:

  ```text
  T | 0 | use A \| B \\ C
  ```

### UI-011 Exit cleanly at end of input

- Aim: Verify that Lobby shows its normal farewell instead of throwing when the input stream ends without a `bye` command.
- Command: `java -cp out lobby.Lobby`
- Inputs, in order:
  1. `list`
  2. End the input stream.
- Expected output:

  ```text
  ____________________________________________________________
   _           _     _
  | |    ___  | |__ | |__  _   _
  | |   / _ \ | '_ \| '_ \| | | |
  | |__| (_) | |_) | |_) | |_| |
  |_____\___/|_.__/|_.__/ \__, |
                           |___/
  Hello! I'm Lobby.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
   Here are the tasks in your list:
  ____________________________________________________________
  ____________________________________________________________
   Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

- Comparison rule: exact, after normalizing Windows line endings to `\n`.
- Setup: Compile all files in `src/main/java` to the `out` folder with Java 25 before running the command.

### UI-012 Start when the data folder and file do not exist

- Aim: Verify that a first run starts with an empty task list and creates both `data` and `data/lobby.txt` when the first task is added.
- Command: `java -cp out lobby.Lobby`
- Inputs, in order:
  1. `list`
  2. `todo first run`
  3. `bye`
- Expected output:

  ```text
  ____________________________________________________________
   _           _     _
  | |    ___  | |__ | |__  _   _
  | |   / _ \ | '_ \| '_ \| | | |
  | |__| (_) | |_) | |_) | |_| |
  |_____\___/|_.__/|_.__/ \__, |
                           |___/
  Hello! I'm Lobby.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
   Here are the tasks in your list:
  ____________________________________________________________
  ____________________________________________________________
   Got it. I've added this task:
     [T][ ] first run
   Now you have 1 task in the list.
  ____________________________________________________________
  ____________________________________________________________
   Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

- Comparison rule: exact, after normalizing Windows line endings to `\n`.
- Setup:
  1. Compile all files in `src/main/java` to the `out` folder with Java 25.
  2. Remove the entire `data` directory before running the command.
- Expected saved file (`data/lobby.txt`), after normalizing line endings to `\n`:

  ```text
  T | 0 | first run
  ```

### UI-004 Reject malformed event commands

- Aim: Verify that event commands report specific missing parts and do not add an incomplete task.
- Command: `java -cp out lobby.Lobby`
- Inputs, in order:
  1. `event`
  2. `event /from Mon 2pm /to 4pm`
  3. `event meeting /from /to 4pm`
  4. `event meeting /from Mon 2pm /to`
  5. `list`
  6. `bye`
- Expected output:

  ```text
  ____________________________________________________________
   _           _     _
  | |    ___  | |__ | |__  _   _
  | |   / _ \ | '_ \| '_ \| | | |
  | |__| (_) | |_) | |_) | |_| |
  |_____\___/|_.__/|_.__/ \__, |
                           |___/
  Hello! I'm Lobby.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
   An event needs a /from start time. Try: event <description> /from <start> /to <end>.
  ____________________________________________________________
  ____________________________________________________________
   An event needs a description before /from.
  ____________________________________________________________
  ____________________________________________________________
   An event needs a start time after /from.
  ____________________________________________________________
  ____________________________________________________________
   An event needs an end time after /to.
  ____________________________________________________________
  ____________________________________________________________
   Here are the tasks in your list:
  ____________________________________________________________
  ____________________________________________________________
   Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

- Comparison rule: exact, after normalizing Windows line endings to `\n`.
- Setup: Compile all files in `src/main/java` to the `out` folder with Java 25 before running the command.

### UI-003 Reject malformed deadline commands

- Aim: Verify that deadline commands report specific missing parts and do not add an incomplete task.
- Command: `java -cp out lobby.Lobby`
- Inputs, in order:
  1. `deadline`
  2. `deadline /by Monday`
  3. `deadline submit report /by`
  4. `deadline submit report /by next Monday`
  5. `list`
  6. `bye`
- Expected output:

  ```text
  ____________________________________________________________
   _           _     _
  | |    ___  | |__ | |__  _   _
  | |   / _ \ | '_ \| '_ \| | | |
  | |__| (_) | |_) | |_) | |_| |
  |_____\___/|_.__/|_.__/ \__, |
                           |___/
  Hello! I'm Lobby.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
   A deadline needs a /by time. Try: deadline <description> /by <when>.
  ____________________________________________________________
  ____________________________________________________________
   A deadline needs a description before /by.
  ____________________________________________________________
  ____________________________________________________________
   A deadline needs a time after /by.
  ____________________________________________________________
  ____________________________________________________________
   Please enter the deadline date as yyyy-MM-dd, for example 2019-10-15.
  ____________________________________________________________
  ____________________________________________________________
   Here are the tasks in your list:
  ____________________________________________________________
  ____________________________________________________________
   Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

- Comparison rule: exact, after normalizing Windows line endings to `\n`.
- Setup: Compile all files in `src/main/java` to the `out` folder with Java 25 before running the command.

### UI-013 Find tasks by a keyword in their descriptions

- Aim: Verify that `find` displays only tasks whose descriptions contain the keyword, preserves their order, and rejects a missing keyword.
- Command: `java -cp out lobby.Lobby`
- Inputs, in order:
  1. `find book`
  2. `find`
  3. `bye`
- Expected output:

  ```text
  ____________________________________________________________
   _           _     _
  | |    ___  | |__ | |__  _   _
  | |   / _ \ | '_ \| '_ \| | | |
  | |__| (_) | |_) | |_) | |_| |
  |_____\___/|_.__/|_.__/ \__, |
                           |___/
  Hello! I'm Lobby.
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
   Here are the matching tasks in your list:
   1.[T][X] read book
   2.[D][ ] return book (by: Jun 06 2019)
  ____________________________________________________________
  ____________________________________________________________
   Please use find followed by a keyword.
  ____________________________________________________________
  ____________________________________________________________
   Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

- Comparison rule: exact, after normalizing Windows line endings to `\n`.
- Setup:
  1. Compile all files in `src/main/java` to the `out` folder with Java 25.
  2. Create `data/lobby.txt` with this exact UTF-8 content:

     ```text
     T | 1 | read book
     D | 0 | return book | 2019-06-06
     T | 0 | buy groceries
     ```
