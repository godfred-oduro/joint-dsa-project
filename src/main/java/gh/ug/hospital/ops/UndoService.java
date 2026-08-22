package gh.ug.hospital.ops;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.LinkedStack;
import gh.ug.hospital.exception.StructureException;
import gh.ug.hospital.model.AuditEvent;
import gh.ug.hospital.model.Resource;
import gh.ug.hospital.model.ServiceRequest;
import gh.ug.hospital.util.Parameters;

/**
 * Owner: 22372639
 *
 * Records every decision and lets the last one be taken back. Rule BR14.
 *
 * This is what the stack is for. Every time the program assigns a job,
 * reassigns it, cancels it or changes a setting, an event is pushed on. Each
 * event carries two things: what the data looks like now, and what it looked
 * like before. Undo pops the top event and puts the old state back.
 *
 * Because a stack is last in first out, undo always reverses the most recent
 * decision, which is what anyone would expect.
 *
 * An examiner asking "can you undo that?" during the defence is answered by
 * menu option M7.2.
 */
public class UndoService {

    private final LinkedStack<AuditEvent> history;
    private final DynamicArray<AuditEvent> everything;

    public UndoService() {
        this.history = new LinkedStack<>();
        this.everything = new DynamicArray<>();
    }

    /** Records that a job was given to a resource. */
    public AuditEvent recordDispatch(ServiceRequest request, String previousStatus,
                                     String previousResourceId, String actor) {
        AuditEvent event = new AuditEvent(
                AuditEvent.DISPATCH,
                "service_requests",
                request.getRequestId(),
                actor,
                AuditEvent.buildState(
                        "status", request.getStatus(),
                        "assigned_resource_id", request.getAssignedResourceId()),
                AuditEvent.buildState(
                        "status", previousStatus,
                        "assigned_resource_id", previousResourceId));
        push(event);
        return event;
    }

    /** Records that a job was cancelled. */
    public AuditEvent recordCancel(ServiceRequest request, String previousStatus,
                                   String actor) {
        AuditEvent event = new AuditEvent(
                AuditEvent.CANCEL,
                "service_requests",
                request.getRequestId(),
                actor,
                AuditEvent.buildState("status", ServiceRequest.CANCELLED),
                AuditEvent.buildState("status", previousStatus));
        push(event);
        return event;
    }

    /** Records that a setting was changed from the menu. */
    public AuditEvent recordParameterChange(String parameterCode, String oldValue,
                                            String newValue, String actor) {
        AuditEvent event = new AuditEvent(
                AuditEvent.PARAM_CHANGE,
                "parameters",
                parameterCode,
                actor,
                AuditEvent.buildState("value", newValue),
                AuditEvent.buildState("value", oldValue));
        push(event);
        return event;
    }

    /** Records that both ambulances were out and the call went to 112. */
    public AuditEvent recordAmbulanceEscalation(ServiceRequest request, String actor) {
        AuditEvent event = new AuditEvent(
                AuditEvent.AMBULANCE_ESCALATION,
                "service_requests",
                request.getRequestId(),
                actor,
                AuditEvent.buildState(
                        "escalated_to", Dispatcher.NATIONAL_AMBULANCE_NUMBER,
                        "status", ServiceRequest.ASSIGNED),
                AuditEvent.buildState("status", ServiceRequest.NEW));
        push(event);
        return event;
    }

    private void push(AuditEvent event) {
        history.push(event);
        everything.add(event);
    }

    /**
     * Takes back the most recent decision.
     *
     * Hands back the event that was undone, so the caller can say what it
     * put right.
     */
    public AuditEvent undoLast(DynamicArray<ServiceRequest> requests,
                               DynamicArray<Resource> resources) {
        if (history.isEmpty()) {
            throw new StructureException(
                    "There is nothing to undo. No decisions have been made yet.");
        }

        AuditEvent event = history.pop();
        String oldState = event.getUndoPayload();

        if (AuditEvent.PARAM_CHANGE.equals(event.getEventType())) {
            restoreParameter(event.getEntityId(),
                    AuditEvent.readFromState(oldState, "value"));
            return event;
        }

        // Everything else is about a request, so find it and put it back.
        ServiceRequest request = findRequest(requests, event.getEntityId());
        if (request == null) {
            return event;
        }

        String oldStatus = AuditEvent.readFromState(oldState, "status");
        String oldResourceId = AuditEvent.readFromState(oldState, "assigned_resource_id");

        // If a resource was holding this job, hand its capacity back.
        String currentResourceId = request.getAssignedResourceId();
        if (currentResourceId != null && !currentResourceId.equals(oldResourceId)) {
            Resource holder = findResource(resources, currentResourceId);
            if (holder != null) {
                holder.finishJob();
            }
        }

        if (oldStatus != null) {
            request.setStatus(oldStatus);
        }
        request.setAssignedResourceId(oldResourceId);

        return event;
    }

    private void restoreParameter(String code, String value) {
        if (value == null) {
            return;
        }
        if ("P1".equals(code)) {
            Parameters.setHashTableSize(Integer.parseInt(value));
        } else if ("P2".equals(code)) {
            Parameters.setRandomSeed(Long.parseLong(value));
        } else if ("P3".equals(code)) {
            Parameters.setRoughRoadPenalty(Double.parseDouble(value));
        } else if ("P4".equals(code)) {
            Parameters.setPorterMinuteBudget(Integer.parseInt(value));
        } else if ("P5".equals(code)) {
            Parameters.setRedPriorityMultiplier(Integer.parseInt(value));
        }
    }

    private ServiceRequest findRequest(DynamicArray<ServiceRequest> requests, String id) {
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getRequestId().equals(id)) {
                return requests.get(i);
            }
        }
        return null;
    }

    private Resource findResource(DynamicArray<Resource> resources, String id) {
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getResourceId().equals(id)) {
                return resources.get(i);
            }
        }
        return null;
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    public int undoableCount() {
        return history.size();
    }

    /** The last few events, newest first, for menu option M7.1. */
    public DynamicArray<AuditEvent> recent(int howMany) {
        DynamicArray<AuditEvent> out = new DynamicArray<>();
        int start = everything.size() - 1;
        for (int i = start; i >= 0 && out.size() < howMany; i--) {
            out.add(everything.get(i));
        }
        return out;
    }

    public DynamicArray<AuditEvent> all() {
        return everything;
    }

    public String toTraceString() {
        return "undo stack: " + history.size() + " reversible decisions, "
                + everything.size() + " events recorded in total";
    }
}
