# M6.8 — Where greedy fails

Produced by: Menu 6 (Evidence) → option 8, on the running `ghana-hospital-dsa` app.
Counterexample required by the project brief, showing greedy assignment is not always optimal.

## The scenario

Map: `B --9-- J1 --1-- A --2-- J2` (roads are two-way)

Two jobs, one at J1 (urgency 5) and one at J2 (urgency 3).
Two porters, one starting at A and one starting at B.

## What greedy does

Greedy always assigns the nearest free porter to the job currently being
handled, most urgent job first.

| Step | Job | Assigned | Distance | Note |
|---|---|---|---|---|
| 1 | JOB-J1 (urgency 5) | P-at-A, from A | 1.00 min | nearest free worker |
| 2 | JOB-J2 (urgency 3) | P-at-B, from B | 12.00 min | nearest free worker (only one left) |

**Greedy total: 13.0 minutes**

Greedy commits P-at-A to the very close job (J1, 1 minute), because that
looks best in the moment. That leaves P-at-B as the only porter left for
J2, and B is far away — the road from B to J2 only exists via J1 and A,
so it costs 9 + 1 + 2 = 12 minutes.

## What the optimal plan does

Swap the assignments: send P-at-B to J1 (9 minutes, slower but it's the
job B is actually close to) and save P-at-A for J2 (2 minutes).

**Best possible total: 11.0 minutes**

## Why this matters

Greedy only ever looks at the job in front of it. It cannot see that
grabbing the nearby porter for the first job forces a worse outcome for
the second job. This is a genuine, reproducible gap (13.0 vs 11.0
minutes) — not a tie that happens to break the right way. It demonstrates
that greedy algorithms trade optimality for speed: fast, and usually
good enough, but not guaranteed best.
