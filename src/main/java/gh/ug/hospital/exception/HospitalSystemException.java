package gh.ug.hospital.exception;

/**
 * Owner: 22040255
 *
 * The parent of every error in this project.
 *
 * We extend RuntimeException so the compiler does not force a try/catch
 * on every single call. The menu catches these in one place instead.
 */
public class HospitalSystemException extends RuntimeException {

    public HospitalSystemException(String message) {
        super(message);
    }
}
