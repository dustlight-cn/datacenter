package cn.dustlight.datacenter.core;

public class DatacenterException extends RuntimeException {

    private ErrorDetails errorDetails;

    public DatacenterException() {
        super();
        this.errorDetails = new ErrorDetails(0, null);
    }

    public DatacenterException(String message) {
        super(message);
        this.errorDetails = new ErrorDetails(0, message);
    }

    public DatacenterException(String message, Throwable throwable) {
        super(message, throwable);
        this.errorDetails = new ErrorDetails(0, message);
    }

    public DatacenterException(ErrorDetails errorDetails) {
        super(errorDetails.getMessage());
        this.errorDetails = errorDetails;
    }

    public DatacenterException(ErrorDetails errorDetails, Throwable throwable) {
        super(errorDetails.getMessage(), throwable);
        this.errorDetails = errorDetails;
    }

    public ErrorDetails getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(ErrorDetails errorDetails) {
        this.errorDetails = errorDetails;
    }
}
