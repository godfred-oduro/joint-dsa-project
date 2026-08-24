# M6.9 — Binary search on unsorted data

Produced by: Menu 6 (Evidence) → option 9, on the running `ghana-hospital-dsa` app.
Counterexample required by the project brief, showing binary search's sorted-input
precondition is not optional.

## Why this matters

Binary search works by discarding half the remaining data at every step,
based on comparing the target to the middle value. That logic only holds
if the data is sorted — on unsorted data, the half being discarded can
still contain the answer.

## The scenario

Data: `[50, 20, 90, 10, 7, 60, 30]` (deliberately unsorted)
Target: `7`, which is actually sitting at index 4.

## Trace

| Step | Low | High | Mid | Value at mid | Note |
|---|---|---|---|---|---|
| 1 | 0 | 6 | 3 | 10 | target is smaller, drop the right half |
| 2 | 0 | 2 | 1 | 20 | target is smaller, drop the right half |
| 3 | 0 | 0 | 0 | 50 | target is smaller, drop the right half |
| 4 | 0 | -1 | - | - | low passed high — item is not in the data |

**Answer returned: -1 (not found)**

## Reading

The item is there, at index 4. Binary search says it is not. At the
first step it looked at the middle (value 10), decided the target (7)
must be to the left since 7 < 10, and discarded the right half — which
is exactly where 7 was sitting. Every following step compounds the same
mistake, because the search space it kept was never going to contain
the answer.

## Note on the real implementation

The production `BinarySearch.run` method checks that its input is sorted
first and throws a `DomainException` if it is not, so this exact failure
cannot happen in the actual application. This demonstration deliberately
uses a version that skips that check, purely to show *why* the check
exists and what would go wrong without it.
