---
layout: page
title: Developer Guide
---

FitBot is a Java 25 desktop application for recording and reviewing running, cycling, and gym workouts. This document describes the design of the current release, how the main components work together, and the software engineering practices used to develop the system.

## Table of Contents

1. [Product overview](#1-product-overview)
2. [Architecture](#2-architecture)
   1. [UI layer](#ui-layer)
   2. [Service layer](#service-layer)
   3. [Parser and command layer](#parser-and-command-layer)
   4. [Model layer](#model-layer)
   5. [Storage layer](#storage-layer)
3. [Important design decisions](#3-important-design-decisions)
   1. [Single source of truth](#single-source-of-truth)
   2. [Polymorphic workout model](#polymorphic-workout-model)
   3. [Command results instead of UI-specific responses](#command-results-instead-of-ui-specific-responses)
   4. [Automatic persistence](#automatic-persistence)
   5. [Index-based user references](#index-based-user-references)
4. [Requirements](#4-requirements)
   1. [Product scope](#product-scope)
   2. [User stories](#user-stories)
   3. [Current use cases](#current-use-cases)
   4. [Non-functional requirements](#non-functional-requirements)
5. [Acknowledgements](#5-acknowledgements)
6. [Appendix: Instructions for Manual Testing](#appendix-instructions-for-manual-testing)
   1. [Launch and initial state](#launch-and-initial-state)
   2. [Help and listing workouts](#help-and-listing-workouts)
   3. [Logging workouts](#logging-workouts)
   4. [Finding and filtering workouts](#finding-and-filtering-workouts)
   5. [Editing workouts](#editing-workouts)
   6. [Deleting workouts and persistence](#deleting-workouts-and-persistence)
   7. [Exiting FitBot](#exiting-fitbot)

--------------------------------------------------------------------------------------------------------------------

## 1. Product overview

FitBot uses a JavaFX graphical user interface with a command box. Users can log, list, inspect, filter, edit, and delete workouts. Workout data is persisted automatically as JSON in `data/workouts.json` relative to the application's working directory.

The current release supports:

* Run workouts, with distance, optional elevation, and calculated pace.
* Cycle workouts, with distance, optional elevation and maximum speed, and calculated average speed.
* Gym workouts, with exercise blocks and sets, plus calculated total volume and estimated one-repetition maximums.
* The commands `help`, `list`, `log`, `find`, `filter`, `edit`, `delete`, and `bye`.

--------------------------------------------------------------------------------------------------------------------

## 2. Architecture

FitBot follows a lightweight layered design:

```text
JavaFX UI (FitBotApplication, controllers, views)
                |
                v
       WorkoutService
          /       \
         v         v
      Parser    WorkoutStorage
         |           |
         v           v
      Commands    JSON file
         |
         v
       Workout model classes
```

The main request paths through these layers are:

```text
User action
    |
    +--> GUI form ------------------+
    |                               |
    +--> Command panel --> Parser --+--> Command --> WorkoutService
                                                         |
                           +-----------------------------+---------------+
                           |                                             |
                           v                                             v
                    Workout model                              WorkoutStorage
                           |                                             |
                           +---------------> calculated output <---------+
```

### UI layer

`Launcher` is the Java entry point. It starts JavaFX and launches `FitBotApplication`.

`FitBotApplication` creates the initial stage, loads `main-view.fxml`, attaches the stylesheet, and supplies the shared `WorkoutService` to `MainController`.

`MainController` coordinates the main list, detail view, filters, command panel, and add/edit/delete dialogs. `WorkoutListView` renders summaries, while `WorkoutDetailView` renders the selected workout and its calculated values. FXML files define the static layouts and controller bindings; Java code handles behaviour.

### Service layer

`WorkoutService` owns the in-memory list of workouts. It executes parsed commands, saves the list when a command reports that data was modified, and exposes the shared operations used by the GUI.

Keeping this logic in a service means that the command interface and GUI dialogs use the same validation and persistence behaviour.

### Parser and command layer

`Parser` separates the first word of an input into a command keyword and tokenizes the remaining arguments. Quoted text is preserved as one argument, which allows gym blocks containing spaces to be entered as one value.

Each command extends the abstract `Command` class and provides:

* a keyword;
* a description;
* a usage string;
* an example; and
* an `execute` method that validates arguments and returns a `CommandResult`.

`ArgumentParser` parses option/value pairs such as `-date 2026-09-01` and validates supported option names. `CommandResult` communicates the output message, whether the application should exit, and whether the data changed.

### Model layer

`Workout` is the common base class for `RunWorkout`, `CycleWorkout`, and `GymWorkout`. `WorkoutType` identifies the three supported types.

`GymWorkout` contains `WorkoutBlock` objects, and each block contains `WorkoutSet` objects. The model classes validate their own invariants, such as positive durations, distances, repetitions, and weights. Calculated properties such as pace, average cycling speed, total volume, and estimated one-repetition maximum are derived from stored values rather than persisted separately.

The workout model uses inheritance for shared workout data and composition for gym-specific data:

```text
                          Workout
                      (date, duration)
                              |
          +-------------------+-------------------+
          |                   |                   |
          v                   v                   v
    RunWorkout          CycleWorkout         GymWorkout
 (distance, pace)     (distance, speed)       (blocks)
                                                  |
                                                  v
                                            WorkoutBlock
                                           (exercise name)
                                                  |
                                                  v
                                             WorkoutSet
                                           (reps, weight)
```

### Storage layer

`WorkoutStorage` uses Jackson to serialize and deserialize the workout list. `JavaTimeModule` enables `LocalDate` support. If the data file does not exist, FitBot starts with an empty list. Read and write failures are converted into `FitBotException` so that the service and UI can report them consistently.

Mutating commands follow this persistence flow:

```text
User submits log/edit/delete
              |
              v
       Command validates input
              |
              v
       Service updates list
              |
              v
       data changed? ---- no ----> Return result
              |
             yes
              v
   WorkoutStorage saves JSON
              |
              v
        Return result
```

--------------------------------------------------------------------------------------------------------------------

## 3. Important design decisions

### Single source of truth

The `WorkoutService` owns the current workout list. Views display that data and request changes through the service instead of directly modifying storage. This prevents the command box and GUI dialogs from implementing separate versions of business rules.

### Polymorphic workout model

Run, cycle, and gym workouts share common date and duration fields through `Workout`, while each subclass owns type-specific fields and calculations. This keeps type-specific validation close to the data it protects and avoids one large class filled with unrelated optional fields.

### Command results instead of UI-specific responses

Commands return `CommandResult` rather than directly printing to a terminal or changing JavaFX controls. The same command behaviour can therefore be invoked by the command panel and tested independently from the GUI.

### Automatic persistence

Commands report whether they changed data. `WorkoutService` uses that flag to save only after successful mutations. Read-only commands do not rewrite the JSON file.

### Index-based user references

The displayed list number is one-based, matching how users naturally refer to items in the interface. Commands validate that the number is within the current list bounds before accessing the zero-based Java list.

--------------------------------------------------------------------------------------------------------------------

## 4. Requirements

### Product scope

**Target User Profile**:

Casual fitness enthusiasts who:

- participate in running, cycling, or gym workouts;
- want a simple way to record and review their workout history; and
- have basic computer literacy and prefer a lightweight desktop application.

**Value Proposition**:

- FitBot provides one simple place to record different types of workouts and
  automatically calculates useful metrics such as pace, speed, workout volume,
  and estimated one-repetition maximums.
- FitBot saves workout data locally and automatically, allowing users to review
  and manage their history without maintaining a separate spreadsheet.

### User stories

| As a... | I can... | So that...
| ------- | -------- | ----------
| fitness enthusiast | log a running, cycling, or gym workout | I can keep a record of my exercise activities |
| runner or cyclist | record distance and duration | I can review my pace or average speed |
| gym-goer | record exercise blocks and sets | I can track my strength-training volume and estimated one-repetition maximums |
| fitness enthusiast | list and inspect my stored workouts | I can review my workout history and details |
| fitness enthusiast | filter workouts by type | I can focus on running, cycling, or gym workouts |
| fitness enthusiast | edit or delete a workout | I can correct mistakes and keep my records accurate |
| user | receive help for available commands | I can learn how to use FitBot without external assistance |

### Current use cases

| ID | Use Case                      | Actor | Result |
| --- |-------------------------------| --- | --- |
| [UC1](#uc1--view-general-help) | View general help             | User | Available commands are displayed |
| [UC2](#uc2--view-command-specific-help) | View command-specific help    | User | Usage and examples are displayed |
| [UC3](#uc3--list-all-workouts) | List all workouts             | User | Stored workouts are displayed |
| [UC4](#uc4--log-a-running-workout) | Log a running workout         | User | A validated running workout is saved |
| [UC5](#uc5--log-a-cycling-workout) | Log a cycling workout         | User | A validated cycling workout is saved |
| [UC6](#uc6--log-a-gym-workout) | Log a gym workout             | User | A validated gym workout is saved |
| [UC7](#uc7--find-and-view-workout-details) | Find and view workout details | User | Workout details and metrics are displayed |
| [UC8](#uc8--filter-workouts-by-type) | Filter workouts by type       | User | Matching workouts are displayed |
| [UC9](#uc9--edit-a-running-workout) | Edit a running workout        | User | The selected workout is updated |
| [UC10](#uc10--edit-a-cycling-workout) | Edit a cycling workout        | User | The selected workout is updated |
| [UC11](#uc11--edit-a-gym-workout) | Edit a gym workout            | User | The selected workout is updated |
| [UC12](#uc12--delete-a-workout) | Delete a workout              | User | The selected workout is removed |
| [UC13](#uc13--exit-fitbot) | Exit FitBot                   | User | The application closes |

#### UC1 – View general help

**Actor:** User

**Main Success Scenario (MSS)**

1. User requests general help.
2. FitBot displays all available commands and their descriptions.

Use case ends.

#### UC2 – View command-specific help

**Actor:** User

**Main Success Scenario (MSS)**

1. User requests help for a specific command.
2. FitBot displays the command's usage, description, and example.

Use case ends.

**Extensions**

* 1a. User requests help for an unrecognised command.
  * 1a1. FitBot displays an error message.
  * Use case ends.

#### UC3 – List all workouts

**Actor:** User

**Main Success Scenario (MSS)**

1. User requests the workout list.
2. FitBot displays all stored workouts with one-based list numbers.

Use case ends.

**Extensions**

* 1a. User uses the command panel.
  * 1a1. User supplies arguments with the `list` command.
  * 1a2. FitBot displays an error message explaining that `list` does not accept arguments.
  * Use case ends.

#### UC4 – Log a running workout

**Actor:** User

**Main Success Scenario (MSS)**

1. User provides the date, duration, distance, and optional elevation.
2. FitBot validates the values and creates the running workout.
3. FitBot calculates the running pace, saves the workout, and displays the result.

Use case ends.

**Extensions**

* 1a. User enters details through the GUI; FitBot builds the corresponding log command.
* 1b. User enters details through the command panel; FitBot parses the command arguments.
* 2a. Required details are missing or invalid.
  * 2a1. FitBot displays an error and does not save the workout.
  * Use case ends.

#### UC5 – Log a cycling workout

**Actor:** User

**Main Success Scenario (MSS)**

1. User provides the date, duration, distance, and optional elevation and maximum speed.
2. FitBot validates the values and creates the cycling workout.
3. FitBot calculates the average speed, saves the workout, and displays the result.

Use case ends.

**Extensions**

* 1a. User enters details through the GUI or command panel; FitBot collects or parses them.
* 2a. Required details are missing or invalid.
  * 2a1. FitBot displays an error and does not save the workout.
  * Use case ends.

#### UC6 – Log a gym workout

**Actor:** User

**Main Success Scenario (MSS)**

1. User provides the date, duration, exercise blocks, and sets.
2. FitBot validates the values and creates the gym workout.
3. FitBot calculates total volume and estimated one-repetition maximums, saves the workout, and displays the result.

Use case ends.

**Extensions**

* 1a. User enters blocks and sets through the GUI; FitBot converts the form entries into the gym format.
* 1b. User enters blocks and sets through the command panel; FitBot parses the quoted specification.
* 2a. A block or set is malformed or contains a non-positive value.
  * 2a1. FitBot displays an error and does not save the workout.
  * Use case ends.

#### UC7 – Find and view workout details

**Actor:** User

**Main Success Scenario (MSS)**

1. User provides a workout's displayed list number.
2. FitBot validates the number.
3. FitBot displays the workout's details and calculated metrics.

Use case ends.

**Extensions**

* 2a. User supplies a non-numeric or out-of-range workout number.
  * 2a1. FitBot displays an error and leaves the list unchanged.
  * Use case ends.

#### UC8 – Filter workouts by type

**Actor:** User

**Main Success Scenario (MSS)**

1. User selects a workout type to filter by.
2. FitBot validates the type.
3. FitBot displays all workouts matching that type.

Use case ends.

**Extensions**

* 1a. User selects an unsupported type or supplies arguments in the wrong format.
  * 1a1. FitBot displays an error and leaves the list unchanged.
  * Use case ends.

#### UC9 – Edit a running workout

**Actor:** User

**Main Success Scenario (MSS)**

1. User provides a running workout number and the fields to change.
2. FitBot validates the number and new values.
3. FitBot updates the workout, recalculates its pace, saves it, and displays the result.

Use case ends.

**Extensions**

* 2a. User supplies an invalid workout number, field, or value.
  * 2a1. FitBot displays an error and leaves the workout unchanged.
  * Use case ends.

#### UC10 – Edit a cycling workout

**Actor:** User

**Main Success Scenario (MSS)**

1. User provides a cycling workout number and the fields to change.
2. FitBot validates the number and new values.
3. FitBot updates the workout, recalculates its average speed, saves it, and displays the result.

Use case ends.

**Extensions**

* 2a. User supplies an invalid workout number, field, or value.
  * 2a1. FitBot displays an error and leaves the workout unchanged.
  * Use case ends.

#### UC11 – Edit a gym workout

**Actor:** User

**Main Success Scenario (MSS)**

1. User provides a gym workout number and the fields, blocks, or sets to change.
2. FitBot validates the number and new values.
3. FitBot updates the workout, recalculates its metrics, saves it, and displays the result.

Use case ends.

**Extensions**

* 2a. User supplies an invalid workout number, block, set, field, or value.
  * 2a1. FitBot displays an error and leaves the workout unchanged.
  * Use case ends.

#### UC12 – Delete a workout

**Actor:** User

**Main Success Scenario (MSS)**

1. User provides a workout's displayed list number.
2. FitBot validates the number and removes the workout.
3. FitBot saves the updated list and displays a success message.

Use case ends.

**Extensions**

* 2a. User supplies a non-numeric or out-of-range workout number.
  * 2a1. FitBot displays an error and leaves the workout list unchanged.
  * Use case ends.

#### UC13 – Exit FitBot

**Actor:** User

**Main Success Scenario (MSS)**

1. User requests to exit FitBot.
2. FitBot closes the application.

Use case ends.

### Non-functional requirements

1. The application shall use Java 25 and run as a desktop JavaFX application.
2. The application shall respond to valid user commands and GUI actions within
   two seconds for a locally stored list of up to 10,000 workouts.
3. The application shall validate user input and display clear, actionable
   error messages without terminating unexpectedly.
4. The application shall save changed workout data automatically to a local
   JSON file after a successful mutation.
5. The application shall preserve workout data across restarts when the local
   data file is valid.
6. The application shall keep the command interface and GUI consistent by
   applying the same validation and workout-management logic to both.
7. The application shall use one-based workout numbers in user-facing lists
   and commands so that users can identify workouts easily.

--------------------------------------------------------------------------------------------------------------------

## 5. Acknowledgements

* The idea of exposing application functionality through discrete commands with keywords, was inspired by the CS2103/T iP and tP.
* The project uses JavaFX for the GUI, Jackson for JSON serialization, and Gradle for build automation. Their APIs and standard usage patterns are used under their respective licenses; no source code was copied from those projects.
* AI was used to assist in creating this guide.

--------------------------------------------------------------------------------------------------------------------

## Appendix: Instructions for Manual Testing

The instructions below are a starting point. Testers should also perform exploratory testing with different valid and invalid values.

### Launch and initial state

1. Create or use an empty folder and run `java -jar fitbot.jar`.
2. Expected: FitBot opens with an empty workout list if `data/workouts.json` does not exist.
3. If an existing data file is present, make a backup before testing, or use a fresh folder.

### Help and listing workouts

1. Enter `help` in the command panel. Expected: all available commands are displayed.
2. Enter `help log`. Expected: the `log` usage and example are displayed.
3. Enter `help unknown`. Expected: an unknown-command error is displayed.
4. Enter `list`. Expected: FitBot reports that no workouts have been logged yet.
5. Enter `list extra`. Expected: FitBot reports that `list` does not accept arguments.

### Logging workouts

1. Enter `log -type run -date 2026-09-01 -duration 1800 -distance 5 -elevation 35`.
   Expected: a run is added and its pace is calculated.
2. Enter `log -type cycle -date 2026-09-01 -duration 3600 -distance 25 -elevation 100 -max 42`.
   Expected: a cycling workout is added and its average speed is calculated.
3. Enter `log -type gym -date 2026-09-01 -duration 3600 -blocks "Curls:10@12,2x10@10;Squats:3x8@60"`.
   Expected: a gym workout is added with its total volume and estimated one-repetition maximums.
4. Use the GUI add-workout controls to create one run, one cycle, and one gym workout.
   Expected: the same workout types and calculated values are produced.
5. Try `log` with an unsupported type, a negative duration, a zero distance, or malformed gym set data.
   Expected: an error is displayed and no workout is added.

### Finding and filtering workouts

:information_source: **Note:**
Command output is displayed in the command panel and does not change the GUI workout list. For example, `filter -type run` shows filtered results in the command output, but the GUI list continues to display its existing contents.

1. Enter `list` after logging the sample workouts. Expected: workouts are shown with one-based numbers.
2. Enter `find 1`. Expected: all details and calculated metrics for workout 1 are displayed.
3. Try `find`, `find abc`, `find 0`, and `find 999`.
   Expected: an error is displayed and no workout details are shown.
4. Enter `filter -type run`, `filter -type cycle`, and `filter -type gym`.
   Expected: only workouts of the selected type are displayed.
5. Use the GUI filter control to show only the selected type of workout. Expected: only workouts of the selected type are displayed.
5. Try `filter`, `filter -type swimming`, and `filter -type run extra`.
   Expected: an error is displayed.
6. Filter by a valid type with no matching workouts. Expected: FitBot reports that no workouts of that type were found.

### Editing workouts

1. Edit a run with `edit 1 -distance 6`. Expected: the distance and calculated pace update.
2. Edit a cycle with `edit 2 -max 45`. Expected: the maximum speed updates.
3. Edit a gym workout with `edit 3 -block 1 -set 1 -weight 65`. Expected: the set, volume, and estimated one-repetition maximum update.
4. Repeat an edit using the GUI controls where available. Expected: the selected workout is updated.
5. Try an invalid workout number, unsupported field, missing value, and non-positive value.
   Expected: an error is displayed and the workout remains unchanged.

### Deleting workouts and persistence

1. Enter `delete 1`. Expected: workout 1 is removed and the remaining list is renumbered.
2. Try `delete`, `delete abc`, and `delete 999`. Expected: an error is displayed and no workout is removed.
3. Close and relaunch FitBot. Expected: successfully logged, edited, and deleted data remains saved in `data/workouts.json`.

### Exiting FitBot

1. Enter `bye`. Expected: FitBot closes.
2. Relaunch FitBot and close the window using the window close control. Expected: the application exits without losing previously saved data.
