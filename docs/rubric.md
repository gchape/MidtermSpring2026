# Midterm Technical Rubric

This document defines the technical internal mark for the midterm project.

The midterm is worth:

* `30` grading points

## Two-Layer Model

The technical evaluation first produces a base internal mark:

```text
0-100 internal marks
```

That internal mark is then converted to regular midterm grading points:

```text
0-30 grading points
```

The internal mark carries the detail. The grading points are the recorded midterm result.

## Internal Mark Categories

### 1. Behavior Preservation And Characterization Tests: 25 marks

Excellent:

* Tests cover important existing behavior before risky refactoring.
* Tests describe this implementation, including quirks.
* Tests are focused and readable.
* Tests can be run with a simple command.

Partial credit:

* Some tests exist, but major rule behavior is untested.
* Tests are too broad, fragile, or hard to understand.
* Tests were added only after refactoring.

Low credit:

* No meaningful characterization tests.
* Tests assert desired behavior instead of current behavior.
* Refactoring changes behavior without detection.

### 2. Incremental Refactoring Discipline: 20 marks

Excellent:

* Work is split into small behavior-preserving steps.
* Each step has a clear purpose.
* Refactoring and behavior changes are not mixed.
* The final design is easier to reason about than the original.

Partial credit:

* Some useful refactoring happened, but steps are too large or poorly explained.
* Some unrelated cleanup is mixed in.
* Behavior preservation is mostly assumed rather than demonstrated.

Low credit:

* Large rewrite.
* Refactoring is cosmetic only.
* The final code is harder to understand or verify.

### 3. Design Improvement: 25 marks

Excellent:

* Game rules are separated from console input/output.
* Duplicated legal-play logic is reduced.
* Card or rule behavior has a clearer home.
* At least one rule can be tested without running the full CLI game.
* The design moves toward MVC-like separation without superficial naming.

Partial credit:

* Some responsibilities are extracted, but important coupling remains.
* MVC names appear, but responsibilities are only partially separated.
* Duplication is reduced in one area but remains central elsewhere.

Low credit:

* Classes are created without meaningful responsibility.
* Patterns are applied mechanically.
* Console, rules, parsing, and state remain essentially tangled.

### 4. Code Quality: 15 marks

Excellent:

* Names are clear.
* Methods and classes have understandable responsibilities.
* The code avoids unnecessary cleverness.
* The solution fits the scale of the project.

Partial credit:

* Some names and boundaries improved.
* Some methods remain too large or unclear.
* The code works but still requires too much mental tracking.

Low credit:

* Code is brittle, obscure, or overengineered.
* The design adds more complexity than it removes.
* Important behavior is hidden behind unclear abstractions.

### 5. Report And Extension Readiness: 15 marks

Excellent:

* Refactoring report clearly explains the refactoring path, preserved behavior, and remaining risks. Up to `10` marks.
* Extension-readiness note identifies a realistic extension point and where the design still resists change. Up to `5`
  marks.

Partial credit:

* Refactoring report summarizes changes but does not explain tradeoffs. Award partial credit from the `10` report marks.
* Extension-readiness note is vague but identifies some plausible extension direction. Award partial credit from the `5`
  extension-readiness marks.
* Remaining risks are incomplete.

Low credit:

* No meaningful refactoring report for the `10` report marks.
* Claims are not supported by code or tests.
* Extension readiness is not addressed for the `5` extension-readiness marks.

## Internal Mark Calculation

Base internal mark:

```text
Behavior Preservation And Characterization Tests /25
+ Incremental Refactoring Discipline /20
+ Design Improvement /25
+ Code Quality /15
+ Report And Extension Readiness /15
= Base internal mark /100
```

## Conversion From Internal Mark To 30 Grading Points

Use this conversion table:

| Base internal mark | Regular grading points |
|--------------------|-----------------------:|
| `0-24`             |                 `0/30` |
| `25-39`            |                 `6/30` |
| `40-49`            |                `10/30` |
| `50-59`            |                `15/30` |
| `60-69`            |                `20/30` |
| `70-84`            |                `25/30` |
| `85-100`           |                `30/30` |

Interpretation:

* `0-24`: no meaningful submission
* `25-39`: very weak or mostly non-working submission
* `40-49`: incomplete but some relevant work exists
* `50-59`: minimal acceptable result
* `60-69`: satisfactory result
* `70-84`: solid result
* `85-100`: strong result

## Gating Rules

These rules prevent inflated grades.

Apply grading in this order:

1. Assign internal category marks.
2. Convert the internal mark using the conversion table.
3. Apply every relevant gate to the converted regular grade.
4. If multiple gates apply, the most restrictive maximum wins.

For gating purposes, `meaningful` means the submission has enough evidence to avoid the corresponding low-credit
description in the internal-mark category. For example, a few tests that do not characterize the existing UNO behavior
do not count as meaningful characterization tests.

### Rule 1: No Meaningful Refactoring, Maximum 10/30

If the submission mostly leaves the monolithic design intact and performs only cosmetic edits, the regular grade cannot
exceed `10/30`.

### Rule 2: No Characterization Tests, Maximum 15/30

If there are no meaningful characterization tests, the regular grade cannot exceed `15/30`, even if the code looks
cleaner.

### Rule 3: Behavior Not Preserved, Maximum 20/30

If important documented behavior changes without explanation and tests, the regular grade cannot exceed `20/30`.

### Rule 4: Full Rewrite, Maximum 15/30

If the student replaces the project with a new implementation instead of refactoring the provided code, the regular
grade cannot exceed `15/30`.

If the rewrite also provides no meaningful refactoring evidence, Rule 1 also applies and the final cap becomes `10/30`.

### Rule 5: Broken CLI, Maximum 20/30

If the refactored project no longer compiles or the CLI cannot run at all, the regular grade cannot exceed `20/30`.

### Rule 6: Missing Refactoring Report, Maximum 25/30

If no meaningful refactoring report is present, the regular grade cannot exceed `25/30`.

## Grading Summary Format

We will use this format in grading files:

```text
Behavior Preservation And Characterization Tests: __/25
Incremental Refactoring Discipline: __/20
Design Improvement: __/25
Code Quality: __/15
Report And Extension Readiness: __/15
Base internal mark: __/100
Gating rules applied: ... (list)
Regular midterm grade: __/30
```
