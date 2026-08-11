package gh.ug.hospital.exception;

/**
 * Owner: 22040255
 *
 * Thrown when a data structure is used wrongly, for example popping an
 * empty stack. This means a programmer made a mistake, not that the
 * hospital refused something.
 */
public class StructureException extends HospitalSystemException {

    public StructureException(String message) {
        super(message);
    }
}
