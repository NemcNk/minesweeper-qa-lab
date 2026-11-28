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

## Quality Assurance

### JaCoCo — Code Coverage

Line coverage by package.

| Package   | Coverage   |
|-----------|------------|
| `core`    | ![Core](https://raw.githubusercontent.com/NemcNk/minesweeper-qa-lab/badges/jacoco/core.svg)   |
| `logging` | ![Logging](https://raw.githubusercontent.com/NemcNk/minesweeper-qa-lab/badges/jacoco/logging.svg) |
| `session` | ![Session](https://raw.githubusercontent.com/NemcNk/minesweeper-qa-lab/badges/jacoco/session.svg) |
| `model`   | ![Model](https://raw.githubusercontent.com/NemcNk/minesweeper-qa-lab/badges/jacoco/model.svg)   |
| `stats`   | ![Stats](https://raw.githubusercontent.com/NemcNk/minesweeper-qa-lab/badges/jacoco/stats.svg)   |

### PIT — Mutation Testing

Mutation testing measures how effectively tests detect changes in application code.

| Package   | Mutation score |
|-----------|----------------|
| `core`    | ![Core](https://raw.githubusercontent.com/NemcNk/minesweeper-qa-lab/badges/pit/core.svg)        |
| `logging` | ![Logging](https://raw.githubusercontent.com/NemcNk/minesweeper-qa-lab/badges/pit/logging.svg)     |
| `session` | ![Session](https://raw.githubusercontent.com/NemcNk/minesweeper-qa-lab/badges/pit/session.svg)     |
| `model`   | ![Model](https://raw.githubusercontent.com/NemcNk/minesweeper-qa-lab/badges/pit/model.svg)       |
| `stats`   | ![Stats](https://raw.githubusercontent.com/NemcNk/minesweeper-qa-lab/badges/pit/stats.svg)       |


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

- JUnit 6 and AssertJ
- Tests for `core` use a fixed bomb layout (`TestBoards.game("*..", "...")`),
  so every scenario is deterministic
- Timer logic is tested without JavaFX through the `Ticker` abstraction
- JaCoCo provides package-level line coverage reports
- PIT performs mutation testing for the main application logic

## Continuous Integration

GitHub Actions automatically runs tests, generates JaCoCo and PIT reports, and publishes updated SVG badges after successful CI runs on main.

Known and fixed defects are documented in [`docs/qa/bugs`](docs/qa/bugs).