---
layout: page
title: User Guide
---

FitBot is a **desktop fitness tracker for recording and reviewing workouts**. It provides a graphical user interface (GUI) with a command box for users who prefer to manage workouts using typed commands.

## Table of Contents

1. [Quick start](#quick-start)
2. [Features](#features)
   1. [Viewing help](#viewing-help-help)
   2. [Listing workouts](#listing-workouts-list)
   3. [Logging a run](#logging-a-run-log-type-run)
   4. [Logging a cycling workout](#logging-a-cycling-workout-log-type-cycle)
   5. [Logging a gym workout](#logging-a-gym-workout-log-type-gym)
   6. [Viewing workout details](#viewing-workout-details-find)
   7. [Filtering workouts](#filtering-workouts-filter)
   8. [Editing a workout](#editing-a-workout-edit)
   9. [Deleting a workout](#deleting-a-workout-delete)
   10. [Exiting the program](#exiting-the-program-bye)
   11. [Saving the data](#saving-the-data)
3. [FAQ](#faq)

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` is installed on your computer.

2. Download the latest FitBot `.jar` file.

3. Place the JAR file in the folder you want to use as FitBot's home folder.

4. Open a terminal, `cd` into that folder, and run the application. For a standalone JAR, use:

   ```bash
   java -jar fitbot.jar
   ```

   The FitBot window should appear after a few seconds.

5. Type a command in the command box and press Enter. For example, `help` displays the available commands.

   Some commands you can try:

   * `list` : Lists all logged workouts.
   * `log -type run -date 2026-09-01 -duration 1800 -distance 5` : Logs a run.
   * `find 1` : Shows the details and calculated metrics for workout 1.
   * `filter -type gym` : Lists only gym workouts.
   * `edit 1 -distance 6` : Edits workout 1.
   * `delete 1` : Deletes workout 1.
   * `bye` : Exits FitBot.

1. Refer to the [Features](#features) section for the complete command reference.

--------------------------------------------------------------------------------------------------------------------

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in angle brackets are values supplied by the user. For example, `find <workout number>` can be used as `find 2`.
* Items in square brackets are optional.
* Options begin with a hyphen, such as `-date` and `-duration`.
* Dates must use `YYYY-MM-DD` format.
* Duration is specified in seconds. Distances are in kilometres, elevations in metres, and weights in kilograms.
* Commands are case-sensitive and option values must be supplied after their option names.
* The displayed workout number is used by `find`, `edit`, and `delete`.
* Data is saved automatically after a command changes the workout list.
</div>

### Viewing help : `help`

Displays all available commands, or detailed usage for one command.

Format: `help [command]`

Examples:

* `help`
* `help log`

### Listing workouts : `list`

Lists all logged workouts and their list numbers.

Format: `list`

### Logging a run : `log -type run`

Logs a running workout. Pace is calculated from duration and distance.

Format: `log -type run -date <YYYY-MM-DD> -duration <seconds> -distance <kilometres> [-elevation <metres>]`

Example: `log -type run -date 2026-09-01 -duration 1800 -distance 5 -elevation 35`

### Logging a cycling workout : `log -type cycle`

Logs a cycling workout. Average speed is calculated from duration and distance.

Format: `log -type cycle -date <YYYY-MM-DD> -duration <seconds> -distance <kilometres> [-elevation <metres>] [-max <km/hr>]`

Example: `log -type cycle -date 2026-09-01 -duration 3600 -distance 25 -max 42`

### Logging a gym workout : `log -type gym`

Logs a strength-training workout containing one or more exercise blocks. Separate multiple sets with commas and multiple blocks with semicolons.

Format: `log -type gym -date <YYYY-MM-DD> -duration <seconds> -blocks "<exercise>:<set-entry>,...;..."`

Each set entry is either `<reps>@<kg>` or `<sets>x<reps>@<kg>`.

Example: `log -type gym -date 2026-09-01 -duration 3600 -blocks "Curls:10@12,2x10@10;Squats:3x8@60"`

### Viewing workout details : `find`

Displays all stored details and calculated metrics for one workout.

Format: `find <workout number>`

Example: `find 2`

### Filtering workouts : `filter`

Lists workouts of one type: `run`, `cycle`, or `gym`.

Format: `filter -type <run|cycle|gym>`

Example: `filter -type gym`

### Editing a workout : `edit`

Changes only the options supplied for an existing workout. The supported options depend on the workout type.

Format:

* Run: `edit <number> [-date <YYYY-MM-DD>] [-duration <seconds>] [-distance <kilometres>] [-elevation <metres>]`
* Cycle: `edit <number> [-date <YYYY-MM-DD>] [-duration <seconds>] [-distance <kilometres>] [-elevation <metres>] [-max <km/hr>]`
* Gym: `edit <number> [-date <YYYY-MM-DD>] [-duration <seconds>]` followed by a block or set-editing option.

Examples:

* `edit 2 -date 2026-09-01 -distance 15`
* `edit 3 -block 1 -set 2 -weight 65`

Use `help edit` for the complete gym workout syntax.

### Deleting a workout : `delete`

Deletes the workout at the specified list number.

Format: `delete <workout number>`

Example: `delete 3`

### Exiting the program : `bye`

Exits FitBot.

Format: `bye`

### Saving the data

FitBot saves workout data automatically after a successful `log`, `edit`, or `delete` command. The data is stored in `data/workouts.json` relative to the folder from which FitBot is launched.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
Do not edit `workouts.json` unless you understand its JSON format. Invalid data may prevent FitBot from loading existing workouts. Make a backup before editing the file manually.
</div>

--------------------------------------------------------------------------------------------------------------------

## Acknowledgements

* AI was used to assist in creating this guide.
