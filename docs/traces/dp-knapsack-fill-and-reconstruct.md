# M4.5 — Dynamic programming: budget selection (fill + reconstruct)

Produced by: Main menu → 4 (Hospital work) → 5 (Choose what fits the budget).
Satisfies the brief's dynamic programming requirement: "knapsack-style
request selection... memoisation or tabulation table and reconstruction."

## The problem

The porters have 497 minutes between them today (setting P4). There are
282 candidate jobs, each costing some number of porter-minutes and worth
some value based on urgency. Pick the subset that fits the budget and
maximises total value — the 0/1 knapsack problem.

## Step one — filling the table (tabulation)

`KnapsackFill` builds a table `best[i][b]` = the most value achievable
using only the first `i` jobs within budget `b`. Each job is either left
out (`best[i][b] = best[i-1][b]`) or taken (`best[i][b] = value +
best[i-1][b - cost]`), whichever is larger. The table is filled
bottom-up so every answer is built from smaller answers already solved.

First 25 of 282 rows (showing the running best value at the full budget,
497, as each job is folded in):

| Step | Job | Best value so far (budget 497) |
|---|---|---|
| 1 | Q001 | 30 |
| 2 | Q002 | 50 |
| 3 | Q003 | 70 |
| 4 | Q004 | 110 |
| 5 | Q005 | 140 |
| 6 | Q007 | 160 |
| 7 | Q008 | 190 |
| 8 | Q009 | 230 |
| 9 | Q010 | 250 |
| 10 | Q011 | 270 |
| 11 | Q012 | 310 |
| 12 | Q013 | 340 |
| 13 | Q014 | 360 |
| 14 | Q015 | 390 |
| 15 | Q016 | 420 |
| 16 | Q017 | 450 |
| 17 | Q018 | 480 |
| 18 | Q020 | 500 |
| 19 | Q021 | 530 |
| 20 | Q022 | 540 |
| 21 | Q023 | 570 |
| 22 | Q024 | 580 |
| 23 | Q025 | 590 |
| 24 | Q026 | 610 |
| 25 | Q027 | 610 |

(row 25's value did not increase over row 24 — Q027 did not improve on
what was already achievable, so it was correctly left out; full table
covers all 282 rows, shortened here for length)

## Step two — reading the table back (reconstruction)

The table alone gives the best possible value, not which jobs make it
up. `KnapsackReconstruct` starts at the bottom-right corner (all jobs,
full budget) and walks upward: if the value is unchanged from the row
above, that job was not taken; if the value rose, it was taken, and its
cost is subtracted from the remaining budget before continuing.

First 25 of 282 checks:

| Step | Job | Budget left | Table value | Chosen? |
|---|---|---|---|---|
| 1 | Q300 | 497 | 1870 | no |
| 2 | Q299 | 497 | 1870 | no |
| 3 | Q298 | 490 | 1870 | **yes** |
| 4 | Q296 | 490 | 1840 | no |
| 5 | Q295 | 490 | 1840 | no |
| 6 | Q294 | 490 | 1840 | no |
| 7 | Q293 | 490 | 1840 | no |
| 8 | Q292 | 490 | 1840 | no |
| 9 | Q291 | 490 | 1840 | no |
| 10 | Q290 | 490 | 1840 | no |
| 11 | Q289 | 490 | 1840 | no |
| 12 | Q288 | 490 | 1840 | no |
| 13 | Q287 | 490 | 1840 | no |
| 14 | Q286 | 485 | 1840 | **yes** |
| 15 | Q285 | 485 | 1810 | no |
| 16 | Q284 | 476 | 1810 | **yes** |
| 17 | Q283 | 443 | 1710 | **yes** |
| 18 | Q282 | 443 | 1610 | no |
| 19 | Q281 | 443 | 1610 | no |
| 20 | Q280 | 436 | 1610 | **yes** |
| 21 | Q279 | 436 | 1580 | no |
| 22 | Q278 | 431 | 1580 | **yes** |
| 23 | Q277 | 415 | 1560 | **yes** |
| 24 | Q276 | 408 | 1520 | **yes** |
| 25 | Q275 | 404 | 1500 | **yes** |

(full walk covers all 282 checks, shortened here for length)

## Result

Chose 55 jobs, using all 497 of 497 porter-minutes, worth 1870 in total.
A sample of the chosen jobs: Q018 (5 min, worth 30), Q020 (7 min, worth
20), Q021 (7 min, worth 30), Q023 (4 min, worth 30), Q026 (6 min, worth
20) — 50 more chosen jobs follow the same pattern.

## Why this is correct

`best[i][b]` only ever depends on `best[i-1][...]`, values already
computed and known correct, so by induction every row is correct once
the row above it is — row 0 (no jobs) is trivially correct at 0 for
every budget, so the whole table is correct by the time it reaches
`best[282][497]`, the final answer. `checkWithinBudget()` in
`BudgetSelector` re-adds the porter-minutes of every chosen job and
throws if the total ever exceeds 497, as a runtime safety net on top of
the proof — that check passed here (55 jobs, exactly 497 of 497
minutes used, nothing over budget).

## Why not greedy

Greedy (sort by value-per-minute, take from the top while it fits)
cannot back out of an early choice: it can commit to a job that later
turns out to block a better pair of jobs, and it has no way to undo
that decision. `KnapsackFill.greedyTotal()` exists specifically to run
the greedy version on the same data for a side-by-side comparison in
the report.
