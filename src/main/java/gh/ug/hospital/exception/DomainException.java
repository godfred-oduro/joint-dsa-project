package gh.ug.hospital.exception;

/**
 * Owner: 22040255
 *
 * Thrown when a hospital rule refuses an operation, for example sending a
 * patient to a clinic that is closed. This is normal during running.
 * The menu catches it, prints the message, and carries on.
 */
public class DomainException extends HospitalSystemException {

    public DomainException(String message) {
        super(message);
    }
}
