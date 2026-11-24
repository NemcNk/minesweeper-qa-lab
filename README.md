# Minesweeper (QA Lab)
![Core Coverage](https://raw.githubusercontent.com/NemcNk/minesweeper-qa-lab/badges/core-coverage.svg)
![Mutation](https://raw.githubusercontent.com/NemcNk/minesweeper-qa-lab/badges/mutation.svg)

This project is a Java implementation of the classic Minesweeper game with a JavaFX graphical user interface.

The main architectural principle is the separation of game logic from the UI, which makes the application easier to maintain, extend, and test.
The project was created as a hands-on environment for practicing unit testing, integration testing, test design, and test automation.

## Features

- Five difficulty levels (Easy to God) with a time limit per level
- Safe first click, flood fill of empty areas, chord (open neighbours by number)
- Flags with a limit equal to the number of bombs
- Pause, restart, win and loss screens
- Session statistics (wins, losses, best time)

| Package | Responsibility |
|---|---|
| `model` | Plain data: `Coord`, `CellContent`, `CellState`, `GameState`, `GameConfig`, `Difficulty` |
| `core`  | Game rules, no UI dependencies. `Game` is the single public entry point |
| `stats` | Game results behind the `StatsRepository` interface |
| `ui`    | JavaFX screens, rendering and input |

## Requirements

- JDK 25
- Maven 3.9+

## Build, test, run

```bash
mvn test       # run unit tests
mvn verify     # tests + coverage report (target/site/jacoco)
```

Run the application from your IDE by launching `app.Main`.

## Testing

- JUnit 5 and AssertJ
- Tests for `core` use a fixed bomb layout (`TestBoards.game("*..", "...")`),
  so every scenario is deterministic
- Timer logic is tested without JavaFX through the `Ticker` abstraction

Known and fixed defects are documented in [`docs/qa/bugs`](docs/qa/bugs).