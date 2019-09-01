package net.lessqq.amidstforge;

public class RemoteCommunicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RemoteCommunicationException() {
    }

    public RemoteCommunicationException(String message) {
        super(message);
    }

    public RemoteCommunicationException(Throwable cause) {
        super(cause);
    }

    public RemoteCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteCommunicationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
