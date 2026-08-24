package gh.ug.hospital.app;

import gh.ug.hospital.algo.Dijkstra;
import gh.ug.hospital.algo.KnapsackFill;
import gh.ug.hospital.algo.KnapsackReconstruct;
import gh.ug.hospital.algo.Kruskal;
import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.model.Resource;
import gh.ug.hospital.model.ServiceRequest;
import gh.ug.hospital.ops.BudgetSelector;
import gh.ug.hospital.ops.ClinicDeferral;
import gh.ug.hospital.ops.Dispatcher;
import gh.ug.hospital.ops.ReferralOrdering;
import gh.ug.hospital.ops.TriageQueue;
import gh.ug.hospital.util.AlgorithmResult;
import gh.ug.hospital.util.Parameters;
import gh.ug.hospital.util.TablePrinter;

import java.time.LocalDateTime;

/**
 * Owner: 22248029
 *
 * Menu option 4. The real hospital scenarios.
 *
 * Options 2 and 3 show the pieces working. This one shows them doing the job
 * they were built for, which is what an examiner will want to see.
 *
 * Note that this file holds no rules of its own. It asks the classes in the
 * ops package and prints what they say. That is deliberate: rules living
 * inside a menu cannot be tested.
 */
public class OperationsMenu {

    private static final String[] OPTIONS = {
            "Order the treatment queue        (rules BR1, BR2, BR6)",
            "Assign a resource to a job       (rules BR9, BR10)",
            "Find the quickest route          (rule BR12)",
            "Put referrals in a workable order (rule BR5)",
            "Choose what fits the budget      (rule BR11)",
            "Show the cheapest set of paths   (constraint C17)",
            "Show a clinic being closed       (rules BR3, BR4)"
    };

    private final Session session;

    public OperationsMenu(Session session) {
        this.session = session;
    }

    public void show() {
        while (true) {
            Console.heading("4. HOSPITAL WORK");
            Console.listOptions(OPTIONS);
            Console.blank();

            int choice = Console.askNumber("Which scenario", 1, OPTIONS.length);
            if (choice == Console.GO_BACK) {
                return;
            }
            if (!session.requireLoaded()) {
                Console.pause();
                continue;
            }
            runScenario(choice);
            Console.pause();
        }
    }

    private void runScenario(int choice) {
        Console.heading("4." + choice + "  " + OPTIONS[choice - 1]);

        if (choice == 1) {
            orderTheQueue();
        } else if (choice == 2) {
            assignSomebody();
        } else if (choice == 3) {
            findRoute();
        } else if (choice == 4) {
            orderReferrals();
        } else if (choice == 5) {
            chooseWithinBudget();
        } else if (choice == 6) {
            showSpanningTree();
        } else if (choice == 7) {
            showClosedClinic();
        }
    }

    // ---- 4.1 ----

    private void orderTheQueue() {
        Console.say("Sorting every waiting request by how urgent it is.");
        Console.say("Red first, then orange, yellow, green, blue. Ties are broken");
        Console.say("by deadline, then by when it arrived, then by code, so the");
        Console.say("order comes out the same every single time.");
        Console.blank();
        Console.say("Red currently carries a multiplier of "
                + Parameters.getRedPriorityMultiplier() + " (setting P5).");
        Console.blank();

        AlgorithmResult result = TriageQueue.orderAll(session.getRequests());
        TablePrinter.printTrace(result, null);
        Console.say("  " + result.summary());
    }

    // ---- 4.2 ----

    private void assignSomebody() {
        String requestId = Console.askText("Which request (for example Q042)");
        ServiceRequest request = session.findRequest(requestId);

        if (request == null) {
            Console.problem("There is no request called " + requestId + ".");
            return;
        }

        Console.say("Request " + request.getRequestId() + ": " + request.getCategory()
                + ", " + request.getSatsColour() + ", starting at "
                + request.getSourceLocationId());
        Console.blank();

        Dispatcher dispatcher = new Dispatcher(session.getListMap(),
                session.getResources());

        String previousStatus = request.getStatus();
        String previousResource = request.getAssignedResourceId();

        AlgorithmResult result = dispatcher.assign(
                request, Resource.PORTER, LocalDateTime.now());

        TablePrinter.printTrace(result, null);

        session.getUndoService().recordDispatch(
                request, previousStatus, previousResource, "console");

        Console.blank();
        Console.say("Recorded, so menu option 7 can undo it.");
    }

    // ---- 4.3 ----

    private void findRoute() {
        String from = Console.askText("From which place (for example L001)");
        String to = Console.askText("To which place (for example L004)");

        AlgorithmResult result = Dijkstra.run(session.getListMap(), from, to, true);

        Console.blank();
        Console.say("Route: " + result.getAnswer());
        Console.say(String.format("Total time: %.2f minutes",
                Dijkstra.lastTotalMinutes()));
        Console.blank();
        Console.say("  " + result.summary());
        Console.blank();
        Console.say("Unpaved paths currently cost "
                + Parameters.getRoughRoadPenalty() + " times as much (setting P3).");
        Console.say("Change that in menu option 8 and run this again. A different");
        Console.say("route may well come out.");
    }

    // ---- 4.4 ----

    private void orderReferrals() {
        Console.say("A patient cannot walk straight into a specialist clinic. They");
        Console.say("see a general practitioner first, who writes a referral note.");
        Console.say("Working out an order that respects every one of those rules is");
        Console.say("what a topological sort does.");
        Console.blank();

        AlgorithmResult result = ReferralOrdering.order(session.getRequests());
        Console.say(ReferralOrdering.summarise(session.getRequests()));
        Console.blank();
        Console.say("  " + result.summary());
    }

    // ---- 4.5 ----

    private void chooseWithinBudget() {
        int budget = Parameters.getPorterMinuteBudget();

        Console.say("The porters have " + budget + " minutes between them today");
        Console.say("(setting P4). Each job costs some of those minutes and is worth");
        Console.say("something based on how urgent it is. We want the most value we");
        Console.say("can get without going over.");
        Console.blank();

        DynamicArray<ServiceRequest> candidates = deferrableJobs();
        Console.say("There are " + candidates.size() + " jobs that could be done today.");
        Console.blank();

        // BudgetSelector.select() runs both halves of the DP with tracing
        // already turned on internally, but it only hands back the final
        // reconstruct result -- the fill-the-table trace was being thrown
        // away before it ever reached the screen. Running both steps here
        // instead means the report can show the table being built, not just
        // the answer read back out of it, which is what the brief asks for.
        KnapsackFill.Job[] jobs = BudgetSelector.toJobs(candidates);

        Console.say("Step one: filling the table (one row per job).");
        Console.say("Only the row for the full budget is shown; the table");
        Console.say("itself holds one entry for every budget from 0 up to "
                + budget + ".");
        Console.blank();
        AlgorithmResult filled = KnapsackFill.run(jobs, budget, true);
        TablePrinter.printTrace(filled, KnapsackFill.traceColumns());
        Console.blank();

        Console.say("Step two: reading the table back to find out which");
        Console.say("jobs were actually chosen.");
        Console.blank();
        AlgorithmResult chosen = KnapsackReconstruct.run(
                KnapsackFill.lastTable(), jobs, budget, true);
        TablePrinter.printTrace(chosen, KnapsackReconstruct.traceColumns());
        Console.blank();

        BudgetSelector.checkWithinBudget(chosen, candidates, budget);
        Console.say(BudgetSelector.describe(chosen, candidates, budget));
        Console.blank();

        DynamicArray<ServiceRequest> picked =
                BudgetSelector.chosenRequests(chosen, candidates);
        for (int i = 0; i < picked.size() && i < 15; i++) {
            ServiceRequest job = picked.get(i);
            Console.say("  " + job.getRequestId() + "  "
                    + job.getPorterMinutes() + " min, worth " + job.getValue());
        }
        if (picked.size() > 15) {
            Console.say("  ... and " + (picked.size() - 15) + " more");
        }
    }

    /** Jobs that can be put off, which is what the budget has to choose between. */
    private DynamicArray<ServiceRequest> deferrableJobs() {
        DynamicArray<ServiceRequest> candidates = new DynamicArray<>();

        for (int i = 0; i < session.getRequests().size(); i++) {
            ServiceRequest request = session.getRequests().get(i);

            // Emergencies are never optional, so they are not candidates.
            if (ServiceRequest.EMERGENCY_RESPONSE.equals(request.getCategory())) {
                continue;
            }
            if (request.getPorterMinutes() <= 0) {
                continue;
            }
            candidates.add(request);
        }
        return candidates;
    }

    // ---- 4.6 ----

    private void showSpanningTree() {
        Console.say("The cheapest set of paths that still joins every place to every");
        Console.say("other. Useful for asking which corridors matter most.");
        Console.blank();

        AlgorithmResult result = Kruskal.run(session.getListMap(), true);
        Console.say(String.format("Total: %.2f minutes across the chosen paths.",
                Kruskal.lastTotalMinutes()));
        Console.say("Groups left over: " + Kruskal.lastGroupCount()
                + "   (1 means everything is connected, which constraint C17 requires)");
        Console.blank();
        Console.say("  " + result.summary());
    }

    // ---- 4.8 ----

    private void showClosedClinic() {
        Console.say("The clinic timetable is uneven. Sickle Cell runs on Friday");
        Console.say("mornings only. ENT runs on Tuesdays. A request that arrives at");
        Console.say("the wrong time has to wait, and that waiting still counts");
        Console.say("against its deadline.");
        Console.blank();

        LocalDateTime now = LocalDateTime.now();
        int shown = 0;

        for (int i = 0; i < session.getRequests().size() && shown < 10; i++) {
            ServiceRequest request = session.getRequests().get(i);

            if (request.getTargetClinic() == null || request.getTargetClinic().isEmpty()) {
                continue;
            }

            try {
                ClinicDeferral.requireOpen(request, now);
                Console.say(request.getRequestId() + "  " + request.getTargetClinic()
                        + ": open now, can be dealt with straight away");
            } catch (Exception closed) {
                Console.say(request.getRequestId() + "  " + closed.getMessage());
            }
            shown++;
        }
    }

}
