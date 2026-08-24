# Trace tables — sorting and graph algorithms (menu 3, tracing enabled)

Produced by: Main menu → 3 (Algorithms), options 3–6 and 11–13, after
turning on `withTrace=true` for these calls (previously these ran with
tracing off, showing only summary statistics). Satisfies the brief's
"at least six trace tables: binary search, insertion sort, merge
sort/quick sort, Dijkstra, Kruskal/Prim, DP" requirement — combined with
the binary search trace (menu 3.2, already captured separately) and the
DP trace (`dp-knapsack-fill-and-reconstruct.md`), all six are now
covered with genuine step-by-step evidence rather than summary counts.

Sorted on 200 shuffled items throughout, unless noted. All trace tables
below are auto-capped to the first 25 steps by `TablePrinter` — the full
row count is shown in the header of each and the "(shortened at step 25
of N)" line is original console output, not an edit.

---

## Selection sort (22372680) — bonus, not required but included

```
comparisons=19900  swaps=196  time=34.071 ms  trace rows=199
```

| Step | Operation | Comparisons | Note |
|---|---|---|---|
| 1 | pass 1: smallest of the rest was at 80 | 199 | positions 0 to 0 are now final |
| 2 | pass 2: smallest of the rest was at 43 | 397 | positions 0 to 1 are now final |
| 3 | pass 3: smallest of the rest was at 28 | 594 | positions 0 to 2 are now final |
| 5 | pass 5: smallest of the rest was at 21 | 985 | positions 0 to 4 are now final |
| 10 | pass 10: smallest of the rest was at 38 | 1945 | positions 0 to 9 are now final |
| 15 | pass 15: smallest of the rest was at 122 | 2880 | positions 0 to 14 are now final |
| 20 | pass 20: smallest of the rest was at 82 | 3790 | positions 0 to 19 are now final |
| 25 | pass 25: smallest of the rest was at 33 | 4675 | positions 0 to 24 are now final |

(full trace: 199 steps; the comparison count climbs by roughly 200 each
pass no matter what the data looks like — selection sort always makes
the same n² comparisons regardless of input order, which is the whole
point of showing it next to insertion sort below.)

## Insertion sort (22370298) — REQUIRED

```
comparisons=10076  swaps=9884  time=11.883 ms  trace rows=199
```

| Step | Operation | Comparisons | Shift count | Note |
|---|---|---|---|---|
| 1 | place item from position 1 | 1 | 1 | positions 0 to 1 are sorted |
| 2 | place item from position 2 | 3 | 2 | positions 0 to 2 are sorted |
| 5 | place item from position 5 | 10 | 7 | positions 0 to 5 are sorted |
| 10 | place item from position 10 | 35 | 27 | positions 0 to 10 are sorted |
| 15 | place item from position 15 | 80 | 68 | positions 0 to 15 are sorted |
| 20 | place item from position 20 | 140 | 123 | positions 0 to 20 are sorted |
| 25 | place item from position 25 | 204 | 183 | positions 0 to 25 are sorted |

The loop invariant this file owes as a proof sketch (from the class's
own comment): before each pass with the key at position `i`, positions
`0` to `i-1` hold the same items that started there, now in sorted
order. Base case: `i=1`, one item, trivially sorted. Inductive step: the
pass slides everything bigger than the key one place right, then drops
the key into the gap, so positions `0` to `i` end up sorted. Termination:
the loop stops at `i=n`, so the whole array is sorted.

## Merge sort (22367946) — REQUIRED (either merge or quick)

```
comparisons=1292  swaps=1544  time=9.574 ms  trace rows=199
```

| Step | Operation | Comparisons | Note |
|---|---|---|---|
| 1 | merge 0..0 with 1..1 | 1 | this section is now sorted |
| 3 | merge 0..1 with 2..3 | 5 | this section is now sorted |
| 6 | merge 0..3 with 4..6 | 14 | this section is now sorted |
| 12 | merge 0..6 with 7..12 | 35 | this section is now sorted |
| 24 | merge 0..12 with 13..24 | 86 | this section is now sorted |

The doubling span (0..0, then 0..1, then 0..3, then 0..6, then 0..12)
is the recursive divide-and-conquer structure showing directly in the
trace: each level merges two already-sorted halves twice the size of
the level before.

## Quick sort (22395395) — REQUIRED (either merge or quick)

```
comparisons=1668  swaps=856  time=13.281 ms  trace rows=113
```

| Step | Operation | Comparisons | Note |
|---|---|---|---|
| 1 | partition 0..199 | 202 | the pivot is now settled at position 119 |
| 2 | partition 0..118 | 323 | the pivot is now settled at position 29 |
| 3 | partition 0..28 | 354 | the pivot is now settled at position 13 |
| 10 | partition 14..28 | 417 | the pivot is now settled at position 17 |
| 18 | partition 30..118 | 553 | the pivot is now settled at position 99 |
| 25 | partition 32..33 | 762 | the pivot is now settled at position 32 |

Quick sort needed only 113 trace rows to fully sort 200 items, against
merge sort's 199 and insertion sort's 199 — fewer, larger partition
steps rather than many small merge/shift steps, which lines up with
quick sort measuring fastest of the four in the E2 timing results.

## Dijkstra shortest route (22248029) — REQUIRED

```
Route: [L001, L017, L015, L018, L038, L040, L041, L042, L044, L004]
comparisons=103  swaps=63  time=35.656 ms  trace rows=41
Total time: 3.02 minutes
```

| Step | Operation | Comparisons | Note |
|---|---|---|---|
| 1 | settle L001 | 0 | this time can no longer improve |
| 2 | settle L007 | 5 | this time can no longer improve |
| 5 | settle L017 | 15 | this time can no longer improve |
| 9 | settle L015 | 31 | this time can no longer improve |
| 13 | settle L018 | 45 | this time can no longer improve |
| 23 | settle L038 | 68 | this time can no longer improve |
| 25 | settle L019 | 74 | this time can no longer improve |

Each step permanently "settles" one location's shortest distance from
L001, in increasing order of distance — the standard Dijkstra
correctness property (once settled, a distance can never improve
further, because any other path would have to go through an
already-settled, farther node first).

## Prim spanning tree (22104879) — REQUIRED (either Prim or Kruskal)

```
Cheapest set of paths joining everything: 18.70 minutes total.
comparisons=116  swaps=51  time=25.653 ms  trace rows=116
```

| Step | Operation | Running total | Note |
|---|---|---|---|
| 1 | take L001-L007(0.04) | 0.04 min | added L007 to the tree |
| 5 | take L010-L009(0.18) | 0.66 min | added L009 to the tree |
| 12 | take L017-L052(0.03) | 1.69 min | added L052 to the tree |
| 13 | consider L015-L052(0.10) | (13 places) | skipped, both ends already in the tree |
| 19 | take L024-L023(0.24) | 2.76 min | added L023 to the tree |
| 20 | consider L014-L052(0.25) | (19 places) | skipped, both ends already in the tree |

Prim grows one connected tree outward, always taking the cheapest edge
that reaches a *new* place — the "skipped, both ends already in the
tree" rows are Prim correctly refusing an edge that would create a
cycle rather than extending reach.

## Kruskal spanning tree (22040255) — REQUIRED (either Prim or Kruskal)

```
Same job, different method: 18.70 minutes total, 1 groups left.
comparisons=765  swaps=851  time=23.053 ms  trace rows=116
```

| Step | Operation | Running total | Groups left | Note |
|---|---|---|---|---|
| 1 | consider L017-L052(0.03) | 0.03 min | 51 | (taken) |
| 5 | consider L015-L052(0.10) | 0.21 min | 48 | (taken) |
| 10 | consider L016-L018(0.14) | 0.84 min | 43 | (taken) |
| 15 | consider L003-L014(0.18) | 1.67 min | 38 | (taken) |
| 20 | consider L031-L032(0.20) | 2.61 min | 33 | (taken) |
| 25 | consider L035-L036(0.23) | 3.70 min | 28 | (taken) |

Kruskal considers every edge in ascending weight order (0.03, 0.04,
0.05, 0.09, 0.10, 0.11...) and uses the disjoint-set structure to reject
any edge that would join two places already in the same group — "groups
left" counts down from 52 toward 1 as the spanning tree completes.
**Prim and Kruskal reach the identical answer, 18.70 minutes total**, by
two structurally different methods — direct cross-check that both
implementations are correct.
