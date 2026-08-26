# University of Ghana Hospital, Legon — Operations Model

DCIT 204 and DCIT 308 joint semester project. 16 members.
Group leader: 22248029.

This program models how University of Ghana Hospital, Legon handles its
daily work: who gets seen next, the fastest way across the compound, which
jobs fit inside a budget. It runs in the console. There is no web page and
no graphics, because the marks are for correct data structures, correct
algorithms and clear evidence.

**This models the 140-bed University of Ghana Hospital, Legon.
It does not model the University of Ghana Medical Centre, which is a
separate and much larger hospital nearby.**

## What you need

- JDK 21 (we used Eclipse Temurin 21.0.5)
- MySQL 8
- Maven 3.8 or newer

## Setting up the database

Do these once, in this order.

1. Create the database and its six tables:
   `mysql -u root -p < sql/schema.sql`
2. Copy `config.properties.example`, rename the copy to `config.properties`,
   and put your own MySQL password in it. Never commit `config.properties`.
3. Build and start the program (see below).
4. Choose menu option 1, then option 1 again, to check the connection works.
5. Choose option 2, **Put the seed files into the database**. This reads the
   four CSV files in `sql/seed` and writes them to MySQL. It only needs
   doing once. Running it again is harmless.
6. Choose option 3, **Load everything from the database**. From now on this
   is the one you use each time the program starts.

After step 5 you should see these row counts:

| Table | Rows |
|---|---|
| locations | 52 |
| roads | 116 |
| resources | 46 |
| service_requests | 300 |

**Start the program from the project folder**, the one holding `pom.xml`.
The program looks for `config.properties` and `sql/seed` relative to
wherever it was started, so running it from somewhere else will fail to
find them.

## Building and running

```
mvn clean package
java -Xms2g -Xmx2g -jar target/hospital.jar
```

The two memory settings are not optional for the timing experiments. They
stop Java resizing its memory in the middle of a run, which would make the
measurements jump around. See the report, section 10.

## The menu

| Code | What it does |
|---|---|
| M1 | Database: connect, load, check, show row counts |
| M2 | Demonstrate each of the 17 data structures |
| M3 | Run each of the 17 algorithms |
| M4 | Hospital scenarios: ordering, assigning, routing, referrals, budget |
| M5 | Run the six timing experiments and save the results |
| M6 | Print trace tables, collision figures and counterexamples |
| M7 | View what has happened and undo the last decision |
| M8 | View and change the five settings while running |
| M9 | Run the test suite |
| M0 | Exit |

## The five settings that came from our index numbers

The project brief asks for at least three settings to be worked out from the
group's index numbers, so that no two groups can produce the same results.
We have five. They live in one file, `util/Parameters.java`, and menu option
M8 can change any of them while the program is running.

Base figures from all 16 index numbers: count 16, sum 345,271,958,
lowest 11,102,882, highest 22,399,601, average 21,579,497, digit sum 476.

The group was 17 when the project started and is now 16. The figures were
worked out again from the remaining numbers. Two settings moved: the hash
table size and the porter-minute budget. The other three came out the same
by chance, which is worth saying rather than leaving unexplained.

| Code | Setting | How it is worked out | Value |
|---|---|---|---|
| P1 | Hash table size | smallest prime not below (sum mod 20000) | 11959 |
| P2 | Random seed | the two lowest index numbers joined together | 1110288222015166 |
| P3 | Rough road penalty | 1 + ((digit sum mod 9) + 1) / 10 | 1.9 |
| P4 | Porter-minute budget | (average mod 400) + 200 | 497 |
| P5 | Red priority multiplier | (highest mod 5) + 1 | 2 |

The first formula we tried for P3 was `1 + ((lowest mod 7) / 10)`. With our
actual numbers that came out at exactly 1.0, which is a penalty that
penalises nothing. We replaced it with the digit-sum version, which always
gives a value between 1.1 and 2.0.

## Declarations

**No built-in Java collections are used in the marked code.** The packages
`ds`, `algo`, `ops` and `util` contain no ArrayList, HashMap, Stack, Queue,
Deque, Set, Map, PriorityQueue or anything similar from `java.util`. We
wrote all of those ourselves. `java.util.Comparator` is used, but that is an
interface for comparing two objects, not a collection. `java.time` is used
for dates, and `java.sql` in the `db` package.

**What we built and what the brief asks for.** The brief asks for at least
15 data structures and at least 14 algorithms. We have 16 of each. That is
not padding: there are 16 of us, and each member has to explain one
structure and one algorithm at the oral defence, so 16 of each is the
smallest number that gives everybody something of their own.

Six of our classes are named after how they are built rather than what they
are: `LinkedStack`, `LinkedQueue`, `LinkedDeque`, `HashBackedSet`,
`HashBackedMap` and `BinaryHeap`. Java already has classes with the obvious
names, and a stray wildcard import would have quietly used Java's version
instead of ours.

**Errors.** There are three error classes, not one per situation.
`StructureException` means a data structure was used wrongly, which is a
programming mistake. `DomainException` means a hospital rule refused the
operation, which is normal while the program runs. Both sit under
`HospitalSystemException`. Every error carries a message naming what went
wrong and which request or place it happened to, so the type being general
costs nothing when you are reading the output.

**All SQL lives in the `db` package** and nowhere else.

**All 300 service requests are made up.** They were generated from the
hospital's published clinic timetable, the South African Triage Scale
urgency levels, the published list of services and our surveyed location
codes. The structure is real. The individual records are not. No patient
information was requested or obtained at any stage.

**Where the data came from.** Six coordinates were read from Google Earth
imagery dated 10 April 2025 and are recorded as measured. Positions of
departments inside the grounds were estimated between those measured points.
Operational figures that are not published were worked out rather than
observed. Every row in the database carries a `source` and a `confidence`
column saying which is which. The full explanation is in the report,
section 2, and the register of estimated values is in the project guide,
Part 29.

**AI tools.** The declaration of AI tools used, including the exact prompts,
is in the report appendix, as the brief requires.

## Where the evidence lives

| What | Where |
|---|---|
| Trace tables | `docs/traces/`, produced by menu M6 |
| Proof sketches | `docs/proofs/` |
| Timing results | `output/`, produced by menu M5 |
| Graphs | `docs/graphs/` |
| Who wrote what | `docs/CONTRIBUTIONS.md` |
| Weekly progress | `docs/development-log.md` |
| Attendance | `docs/attendance/` |
