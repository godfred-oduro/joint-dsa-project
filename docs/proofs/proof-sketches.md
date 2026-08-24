# Proof Sketches

Three required proof sketches, written up from evidence already captured in
`trace-tables-sorting-and-graph.md` and `dp-knapsack-fill-and-reconstruct.md`.

---

## 1. Loop invariant — Insertion sort

**Claim:** After the pass that places the item at position `i`, positions
`0` to `i` hold the same items that started there, now in sorted order.

**Base case:** `i = 1`. A single item is trivially sorted, so the invariant
holds before any real work happens.

**Inductive step:** Assume positions `0` to `i-1` are sorted (the
invariant holding going into pass `i`). The pass takes the key at
position `i` and shifts every item bigger than it one slot to the right,
then drops the key into the gap that leaves. Every item that was in
`0..i-1` is still there, just possibly moved, and the key is now sitting
in its correct sorted spot among them. So positions `0` to `i` are sorted,
which is the invariant for `i+1`.

**Termination:** The outer loop runs until `i = n-1` (last index). Once
that pass finishes, the invariant says positions `0` to `n-1` — the whole
array — are sorted. That's the postcondition we want.

Trace evidence (n=200): at step 10, positions 0–10 are marked sorted;
at step 25, positions 0–25 are marked sorted. The invariant holds at
every checkpoint recorded, not just at the end.

---

## 2. Induction — DP correctness (0/1 knapsack, budget selection)

**Claim:** `best[i][b]`, the best value achievable using only the first
`i` jobs and a porter-minute budget of `b`, is correct for every `i` and
`b` once the table is filled.

**Base case:** `best[0][b] = 0` for every `b`. With zero jobs available
there's nothing to pick, so the best achievable value is 0. Trivially
correct.

**Inductive step:** Assume row `i-1` is entirely correct — every
`best[i-1][b]` is the true optimum using the first `i-1` jobs. Row `i` is
built by, for each `b`: either skip job `i` (value = `best[i-1][b]`) or
take it if it fits (value = `cost[i] + best[i-1][b - minutes[i]]`), and
keep the max. Both branches only read from row `i-1`, which is already
known correct by assumption. So `best[i][b]` is correct too — it's the
best of two options, both computed from correct data.

**Termination:** By induction this holds for every row up to `i = 282`
(all jobs), so `best[282][497]` — the final cell — is the true optimum
across the whole job list and budget. That's the reconstructed answer:
55 jobs, 1870 total value, exactly 497 of 497 minutes used.

**Runtime check backing the proof:** `checkWithinBudget()` re-sums the
porter-minutes of every chosen job and throws if it ever exceeds 497.
It passed on this run — the safety net agrees with what the induction
predicts.

---

## 3. Greedy correctness — Dijkstra's shortest path

**Claim:** When Dijkstra settles a location (fixes its shortest distance
from the source), that distance is final and can never be improved by a
later step.

**Argument:** Dijkstra always settles the unsettled location with the
smallest current tentative distance. Suppose, for contradiction, some
settled location `v` actually had a shorter path than the one just
fixed. Any such path has to pass through at least one location that
isn't settled yet, at the point `v` gets settled — call it `u`. Since
edge weights aren't negative, the distance to `u` alone is already at
least `v`'s settled distance (because Dijkstra picks the smallest
tentative distance at each step, and `u` was still unsettled, so its
tentative distance was ≥ `v`'s). Adding anything more to get from `u` to
`v` only makes the path longer. So no shorter path through an unsettled
node can exist, which contradicts the assumption. Hence `v`'s distance
is final the moment it's settled.

**Trace evidence:** each settle step in the trace is annotated "this
time can no longer improve" — that's the invariant being enforced in
code, and it holds at every one of the 41 steps recorded for the
L001→L004 route (3.02 minutes total, route confirmed via 10 waypoints).

**Why not any settle order:** this only works because non-negative edge
weights guarantee tentative distances only grow as you go further from
the source — that's the standard caveat, and it's satisfied here since
all road travel times are positive minutes.
