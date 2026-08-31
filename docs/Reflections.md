---
layout: page
title: Reflections
---

## Reflections on AI-assisted software engineering

FitBot was developed with support from an LLM for planning, implementation, debugging, and documentation. The most useful interactions were not simply requests to “write code”. They gave the LLM a goal, constraints, or a question about a design decision. The examples below show how prompting affected the development process.

## Interesting prompts and what they demonstrated

### 1. Planning the GUI without implementing it

> “I want to add GUI to the application. The main screen will have ... Let me know if I'm missing anything based on the current features of the application. Do not implement.”

This was a useful planning prompt because it separated requirements discovery from coding. The explicit “Do not implement” constraint encouraged the LLM to review the existing command and model features and identify missing GUI concerns, such as displaying workout details, filtering workouts, supporting edit and delete actions, and keeping the command panel available.

The prompt also shows that negative instructions can be valuable. At this stage, implementation would have made the design harder to change. The result was more useful as a checklist and discussion of trade-offs than as source code.

### 2. Choosing a colour palette before implementation

> “I want the overall background to be `#2f3542`, Run button to be `#1e90ff`, Card background to be `#747d8c`. Before implementing this, what should the command panel colours be?”

This prompt asked for design reasoning before making a change. The response proposed a contrasting panel, header, input, output, label, and border palette. The colours were then implemented and refined through follow-up prompts about the white list background, thick borders, spacing, alignment, and separators.

This interaction demonstrates the value of iterative prompting. A single request established the visual direction, while smaller follow-ups handled concrete usability issues. It also demonstrates a limitation: the LLM can suggest visually plausible colours, but the application still needs to be run and inspected because JavaFX controls may have default backgrounds, borders, and skins that are not obvious from the stylesheet alone.

### 3. Designing quoted command arguments

> “Should command arguments support quoting globally or selectively?”

This prompt addressed a small design decision with consequences for the whole parser. Gym workouts use compound values such as exercise names and multiple sets, so spaces inside one logical value must be preserved. Supporting quotes in the general tokenizer made the parser consistent and avoided special-case handling in the gym command.

This example shows how a short prompt can expose an architectural choice. The final implementation was simpler because the decision was made at the parser boundary instead of adding workarounds inside individual commands.

### 4. Making a broad set of Gradle changes with verification steps

> “Run me through what to edit if I want to add gradle plugins, and how to make sure that they work. Then look through my build.gradle file and identify any errors.”

This prompt had two related goals: learn the process for adding plugins and review the current configuration. It asked for both instructions and repository-specific diagnosis. The response discussed the `application`, `checkstyle`, and Shadow plugins, explained tasks such as `tasks`, `check`, `build`, and `shadowJar`, and identified compatibility risks between older Shadow versions and Gradle 9.7.1.

The main reflection is that asking how to verify a change is as important as asking how to make it. Plugin configuration is not complete when the file looks syntactically correct; the generated tasks and an actual build must also succeed.

## What worked well

* Prompts with explicit scope, such as “do not implement”, produced useful design discussion without unwanted changes.
* Providing concrete files, error messages, colours, and command examples reduced ambiguity.
* Follow-up prompts made it possible to refine one concern at a time instead of rewriting the entire design.
* Asking for explanations of generated configuration helped build understanding rather than only producing a patch.
* The LLM was useful for connecting separate layers: command design, service ownership, JavaFX controllers, storage, and documentation.

## Limitations and lessons learned

The LLM did not automatically know which parts of the repository were current, which dependency versions were compatible, or whether a visual change looked correct at runtime. Repository inspection, compiler output, stack traces, and manual UI checks remained necessary.

Some prompts were underspecified. For example, asking for a “standalone JAR” can mean a fat JAR, an application distribution, or a native installer. The target operating system and meaning of “standalone” should be stated explicitly.

The LLM can also carry assumptions from earlier project states. During the Gradle discussions, earlier source layouts and plugin configurations differed from the final project. This made it important to inspect the current files before accepting a recommendation.

Finally, AI-generated changes still require ownership by the developer. I had to review whether commands matched the actual parser, whether documentation matched the current release, whether dependencies were packaged correctly, and whether changes respected the project's Java 25 and Gradle requirements.

## Overall reflection

AI assistance was effective as a collaborative design and debugging partner. I formulated prompts with explicit constraints when I wanted a particular kind of help. For example, “Do not implement” kept the GUI discussion focused on requirements instead of code writing. Providing concrete details such as colour values, formulas, file names, stack traces, and desired UI locations also reduced ambiguity.

Even with these details, the LLM sometimes made assumptions. It could infer information from an earlier project state or suggest a solution before all dependency and platform constraints were known. I therefore verified its suggestions by inspecting the current files, examining stack traces, and manually checking the GUI. Manual inspection was especially important for visual changes because JavaFX controls have default skins, backgrounds, and borders. These checks ensured that a response that sounded convincing was also consistent with the actual codebase.

As development progressed, my prompts evolved from broad planning questions into targeted implementation and correction requests. After the initial GUI discussion, I addressed one issue at a time: colours, spacing, alignment, borders, separators, and the empty-state text. This made each change easier to review. However, prompting was less effective when a task depended on immediate visual feedback or a small edit. In those situations, inspecting the file and making a focused change was faster than describing the problem at length.

The main process I would continue using is:

1. Ask the LLM to inspect or explain the current state.
2. State whether the task is planning, implementation, or review.
3. Provide constraints and concrete examples.
4. Make changes incrementally.
5. Verify the result with tests and manual inspection.
6. Use follow-up prompts to correct specific problems.

In the future, I would make my prompts more detailed and specific from the beginning, especially by stating the constraints and expected verification steps. I would also involve the LLM more in overall planning before writing code.
