package gh.ug.hospital.ops;

import gh.ug.hospital.ds.BinaryHeap;
import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.model.ServiceRequest;
import gh.ug.hospital.util.AlgorithmResult;
import gh.ug.hospital.util.TriageScorer;

import java.util.Comparator;

/**
 * Owner: 22224236
 *
 * Decides who gets seen next. This is rule BR1.
 *
 * The order is:
 *   1. most urgent colour first: red, then orange, yellow, green, blue
 *   2. if two are the same colour, the earlier deadline goes first
 *   3. if the deadlines match, the one submitted earlier goes first
 *   4. if those match too, sort by request code
 *
 * That last step looks pointless but it is not. Without it, two requests
 * that tie on everything else could come out in either order depending on
 * how the heap happens to be arranged. Adding it means the program gives the
 * same answer every time it runs, which is what makes a trace table
 * reproducible in front of an examiner.
 *
 * Red carries an extra push from setting P5, which came from our index
 * numbers, so menu option M8 can change how hard red pushes.
 */
public class TriageQueue {

    /**
     * The rule for deciding which of two requests comes first.
     *
     * A negative answer means the first one goes first.
     */
    public static final Comparator<ServiceRequest> ORDER = new Comparator<ServiceRequest>() {
        @Override
        public int compare(ServiceRequest a, ServiceRequest b) {

            // Step 1: more urgent first. Higher weight should come first, so we
            // subtract the other way round from the usual.
            int weightA = TriageScorer.priorityWeight(a.getSatsColour());
            int weightB = TriageScorer.priorityWeight(b.getSatsColour());
            if (weightA != weightB) {
                return weightB - weightA;
            }

            // Step 2: earlier deadline first.
            int byDeadline = a.getDeadline().compareTo(b.getDeadline());
            if (byDeadline != 0) {
                return byDeadline;
            }

            // Step 3: submitted earlier first.
            int bySubmitted = a.getTimeSubmitted().compareTo(b.getTimeSubmitted());
            if (bySubmitted != 0) {
                return bySubmitted;
            }

            // Step 4: fall back on the code, so the order never wobbles.
            return a.getRequestId().compareTo(b.getRequestId());
        }
    };

    private final BinaryHeap<ServiceRequest> heap;

    public TriageQueue() {
        this.heap = new BinaryHeap<>(ORDER);
    }

    /** Adds a request to the waiting list. */
    public void add(ServiceRequest request) {
        heap.insert(request);
    }

    /** Who is next, without removing them. */
    public ServiceRequest peek() {
        return heap.peek();
    }

    /** Takes the next request off the list. */
    public ServiceRequest next() {
        return heap.extract();
    }

    public int size() {
        return heap.size();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public void clear() {
        heap.clear();
    }

    /** How many times the heap moved something. Used by the trace. */
    public long swapCount() {
        return heap.getSwapCount();
    }

    /**
     * Works out the whole order and records it as a trace.
     *
     * This empties a copy of the queue rather than the real one, so it can be
     * run from the menu without losing anything.
     */
    public static AlgorithmResult orderAll(DynamicArray<ServiceRequest> requests) {
        AlgorithmResult result = new AlgorithmResult();
        long startedAt = System.nanoTime();

        TriageQueue queue = new TriageQueue();
        for (int i = 0; i < requests.size(); i++) {
            queue.add(requests.get(i));
        }

        DynamicArray<ServiceRequest> inOrder = new DynamicArray<>();
        int position = 1;

        while (!queue.isEmpty()) {
            ServiceRequest next = queue.next();
            inOrder.add(next);

            // Only trace the first twenty five, as the guide asks.
            if (position <= 25) {
                result.addTrace(
                        "take position " + position,
                        next.getRequestId() + " " + next.getSatsColour(),
                        "deadline " + next.getDeadline()
                                + ", target " + TriageScorer.targetMinutes(next.getSatsColour())
                                + " min");
            }
            position++;
        }

        result.addComparisons(queue.swapCount());
        result.addSwaps(queue.swapCount());
        result.setElapsedNanos(System.nanoTime() - startedAt);
        result.setAnswer(inOrder);
        return result;
    }

    public String toTraceString() {
        return "triage queue holding " + heap.size() + " requests. " + heap.toTraceString();
    }
}
