package gov.nasa.podaac.swodlr.exception;

/*
 * A SwodlrException is a RuntimeException that is safe to display to users.
 * SwodlrExceptions should not contain any internal system state/information and
 * inform the user on how they should further proceed
 */
public class SwodlrException extends RuntimeException {
  public SwodlrException(String message) {
    super(message);
  }
}
