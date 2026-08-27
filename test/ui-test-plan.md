# UI Test Plan

This file is the source of truth for console UI test cases. Add one section per case and run them in the order shown. Use exact expected output unless a case explicitly defines a comparison rule.

## Test environment

- Java version: 25
- Working directory: repository root
- Default comparison rule: exact output, including whitespace and line breaks
- Line-ending rule: platform line-ending differences may be normalized to `\n`
- Before each case: delete `data/lobby.txt` if it exists so each case starts without saved data.

## Test cases

### UI-001 Add and list every task type

- Aim: Verify that to-dos, deadlines, and events are stored and displayed with their type-specific details. Deadline and event dates/times must be kept as the exact strings entered.
- Command: `java -cp out Lobby`
- Inputs, in order:
  1. `todo borrow book`
  2. `deadline do homework /by no idea :-p`
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
     [D][ ] do homework (by: no idea :-p)
   Now you have 2 tasks in the list.
  ____________________________________________________________
  ____________________________________________________________
   Got it. I've added this task:
     [E][ ] project meeting (from: Mon 2pm to: 4pm)
   Now you have 3 tasks in the list.
  ____________________________________________________________
  ____________________________________________________________
   Nice! I've marked this task as done:
     [D][X] do homework (by: no idea :-p)
  ____________________________________________________________
  ____________________________________________________________
   Here are the tasks in your list:
   1.[T][ ] borrow book
   2.[D][X] do homework (by: no idea :-p)
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
  D | 1 | do homework | no idea :-p
  E | 0 | project meeting | Mon 2pm | 4pm
  ```

### UI-002 Reject a to-do without a description

- Aim: Verify that a command beginning with `todo` but missing its description shows a helpful error and does not add a task.
- Command: `java -cp out Lobby`
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
- Command: `java -cp out Lobby`
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

### UI-004 Reject malformed event commands

- Aim: Verify that event commands report specific missing parts and do not add an incomplete task.
- Command: `java -cp out Lobby`
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
   An event needs a /to end time. Try: event <description> /from <start> /to <end>.
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
- Command: `java -cp out Lobby`
- Inputs, in order:
  1. `deadline`
  2. `deadline /by Monday`
  3. `deadline submit report /by`
  4. `list`
  5. `bye`
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
   Here are the tasks in your list:
  ____________________________________________________________
  ____________________________________________________________
   Bye. Hope to see you again soon!
  ____________________________________________________________
  ```

- Comparison rule: exact, after normalizing Windows line endings to `\n`.
- Setup: Compile all files in `src/main/java` to the `out` folder with Java 25 before running the command.
