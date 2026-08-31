---
layout: page
title: Developer Guide
---

FitBot is a Java 25 desktop application for recording and reviewing running, cycling, and gym workouts. This document describes the design of the current release, how the main components work together, and the software engineering practices used to develop the system.

## Table of Contents

1. [Product overview](#1-product-overview)
2. [Architecture](#2-architecture)
   1. [UI layer](#21-ui-layer)
   2. [Service layer](#22-service-layer)
   3. [Parser and command layer](#23-parser-and-command-layer)
   4. [Model layer](#24-model-layer)
   5. [Storage layer](#25-storage-layer)
3. [Important design decisions](#3-important-design-decisions)
   1. [Single source of truth](#single-source-of-truth)
   2. [Command results instead of UI-specific responses](#command-results-instead-of-ui-specific-responses)
   3. [Automatic persistence](#automatic-persistence)
   4. [Index-based user references](#index-based-user-references)
4. [Typical execution flows](#4-typical-execution-flows)
   1. [Logging a workout](#logging-a-workout)
   2. [Starting the GUI](#starting-the-gui)
5. [Build and development process](#5-build-and-development-process)
   1. [Prerequisites](#prerequisites)
   2. [Common Gradle tasks](#common-gradle-tasks)
   3. [Engineering practices](#engineering-practices)
6. [Release process](#6-release-process)
7. [Acknowledgements](#7-acknowledgements)

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

### 2.1 UI layer

`Launcher` is the Java entry point. It starts JavaFX and launches `FitBotApplication`.

`FitBotApplication` creates the initial stage, loads `main-view.fxml`, attaches the stylesheet, and supplies the shared `WorkoutService` to `MainController`.

`MainController` coordinates the main list, detail view, filters, command panel, and add/edit/delete dialogs. `WorkoutListView` renders summaries, while `WorkoutDetailView` renders the selected workout and its calculated values. FXML files define the static layouts and controller bindings; Java code handles behaviour.

### 2.2 Service layer

`WorkoutService` owns the in-memory list of workouts. It executes parsed commands, saves the list when a command reports that data was modified, and exposes the shared operations used by the GUI.

Keeping this logic in a service means that the command interface and GUI dialogs use the same validation and persistence behaviour.

### 2.3 Parser and command layer

`Parser` separates the first word of an input into a command keyword and tokenizes the remaining arguments. Quoted text is preserved as one argument, which allows gym blocks containing spaces to be entered as one value.

Each command extends the abstract `Command` class and provides:

* a keyword;
* a description;
* a usage string;
* an example; and
* an `execute` method that validates arguments and returns a `CommandResult`.

`ArgumentParser` parses option/value pairs such as `-date 2026-09-01` and validates supported option names. `CommandResult` communicates the output message, whether the application should exit, and whether the data changed.

### 2.4 Model layer

`Workout` is the common base class for `RunWorkout`, `CycleWorkout`, and `GymWorkout`. `WorkoutType` identifies the three supported types.

`GymWorkout` contains `WorkoutBlock` objects, and each block contains `WorkoutSet` objects. The model classes validate their own invariants, such as positive durations, distances, repetitions, and weights. Calculated properties such as pace, average cycling speed, total volume, and estimated one-repetition maximum are derived from stored values rather than persisted separately.

### 2.5 Storage layer

`WorkoutStorage` uses Jackson to serialize and deserialize the workout list. `JavaTimeModule` enables `LocalDate` support. If the data file does not exist, FitBot starts with an empty list. Read and write failures are converted into `FitBotException` so that the service and UI can report them consistently.

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

## 4. Typical execution flows

### Logging a workout

1. The user enters a command in `CommandPanelController`.
2. `Parser.parseCommand` tokenizes the input and identifies `LogWorkoutCommand`.
3. The command validates options and creates the appropriate `Workout` subclass.
4. `WorkoutService` adds the workout and saves the list through `WorkoutStorage`.
5. The command panel displays the result and refreshes the workout list.

### Starting the GUI

1. `Launcher.main` calls `Application.launch`.
2. JavaFX calls `FitBotApplication.start`.
3. The service loads `data/workouts.json`, if present.
4. `main-view.fxml` creates the main controller and controls.
5. The controller connects the service, child views, filters, and command panel.

--------------------------------------------------------------------------------------------------------------------

## 5. Build and development process

### Prerequisites

* Java 25.
* Gradle Wrapper, supplied as `gradlew` or `gradlew.bat`.
* A JavaFX-capable desktop environment.

### Common Gradle tasks

```bash
./gradlew clean build       # compile, check, test, and package
./gradlew run               # run the application through Gradle
./gradlew check             # run verification tasks, including Checkstyle
./gradlew installDist       # create an application distribution with dependencies
```

The `application` plugin uses `fitbot.Launcher` as the main class. The ordinary JAR does not include external dependencies; use the installed distribution or configure a fat JAR before distributing a standalone JAR.

### Engineering practices

* Keep validation close to the command or model object responsible for the rule.
* Reuse `FitBotException` for user-correctable command and storage errors.
* Add Javadoc to classes and non-obvious methods or fields.
* Run `./gradlew check` before creating a release.
* Keep changes focused and review the generated JSON and UI behaviour after changes to persistence or FXML.
* Do not commit generated build output or user data files unless a release artifact is explicitly required.

There are currently no repository-level automated tests under `src/test`; future work should add unit tests for parser tokenization, command validation, model calculations, and storage round trips before expanding the UI test suite.

--------------------------------------------------------------------------------------------------------------------

## 6. Release process

1. Run `./gradlew clean build` with Java 25.
2. Run the application and manually verify logging, listing, filtering, editing, deleting, persistence, and application exit.
3. Confirm that FXML and stylesheet resources are present in the packaged artifact.
4. Distribute the JAR together with its required dependencies, or use the application distribution under `build/install/mp1/`.
5. Update the user-facing documentation when commands or data formats change.

--------------------------------------------------------------------------------------------------------------------

## 7. Acknowledgements

* The idea of exposing application functionality through discrete commands with keywords, was inspired by the CS2103/T iP and tP.
* The project uses JavaFX for the GUI, Jackson for JSON serialization, and Gradle for build automation. Their APIs and standard usage patterns are used under their respective licenses; no source code was copied from those projects.
* AI was used to assist in creating this guide.
