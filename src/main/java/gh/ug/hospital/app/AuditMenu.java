package gh.ug.hospital.app;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.model.AuditEvent;
import gh.ug.hospital.ops.UndoService;

/**
 * Owner: 22372639
 *
 * Menu option 7. Shows what the program has done and takes back the last
 * decision.
 *
 * Every decision is pushed onto a stack, and each one carries what the data
 * looked like beforehand. Undo pops the newest and puts the old state back.
 * Because a stack is last in first out, that is always the most recent
 * decision, which is what anyone would expect.
 */
public class AuditMenu {

    private static final String[] OPTIONS = {
            "Show the last 20 things that happened",
            "Undo the last decision",
            "Show everything that happened to one request"
    };

    private final Session session;

    public AuditMenu(Session session) {
        this.session = session;
    }

    public void show() {
        while (true) {
            UndoService undo = session.getUndoService();

            Console.heading("7. AUDIT AND UNDO");
            Console.say("  " + undo.toTraceString());
            Console.blank();
            Console.listOptions(OPTIONS);
            Console.blank();

            int choice = Console.askNumber("Choose", 1, OPTIONS.length);
            if (choice == Console.GO_BACK) {
                return;
            }
            handle(choice, undo);
            Console.pause();
        }
    }

    private void handle(int choice, UndoService undo) {
        if (choice == 1) {
            showRecent(undo);
        } else if (choice == 2) {
            undoLast(undo);
        } else if (choice == 3) {
            showOneRequest(undo);
        }
    }

    private void showRecent(UndoService undo) {
        DynamicArray<AuditEvent> recent = undo.recent(20);

        if (recent.size() == 0) {
            Console.say("Nothing has happened yet. Assign a job from menu option 4.2");
            Console.say("or change a setting from option 8, then come back.");
            return;
        }

        Console.say("Newest first:");
        Console.blank();
        for (int i = 0; i < recent.size(); i++) {
            AuditEvent event = recent.get(i);
            Console.say("  " + event.getEventType() + "  " + event.getEntityId());
            Console.say("      now:    " + event.getPayload());
            Console.say("      before: " + event.getUndoPayload());
        }
    }

    private void undoLast(UndoService undo) {
        if (!undo.canUndo()) {
            Console.problem("There is nothing to undo. No decisions have been made yet.");
            return;
        }

        AuditEvent reversed = undo.undoLast(session.getRequests(), session.getResources());

        Console.say("Took back: " + reversed.getEventType()
                + " on " + reversed.getEntityId());
        Console.say("Put back to: " + reversed.getUndoPayload());
        Console.blank();
        Console.say(undo.undoableCount() + " decisions can still be undone.");
    }

    private void showOneRequest(UndoService undo) {
        String requestId = Console.askText("Which request (for example Q042)");

        DynamicArray<AuditEvent> all = undo.all();
        int found = 0;

        for (int i = 0; i < all.size(); i++) {
            AuditEvent event = all.get(i);
            if (requestId.equals(event.getEntityId())) {
                Console.say("  " + event.getCreatedAt() + "  " + event.getEventType());
                Console.say("      " + event.getPayload());
                found++;
            }
        }

        if (found == 0) {
            Console.say("Nothing has happened to " + requestId + " yet.");
        }
    }
}
