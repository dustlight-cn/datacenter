package plus.datacenter.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ErrorDetails {

    private String message;
    private int code;
    private String details;

    @JsonIgnore
    private transient DatacenterException exception;

    public ErrorDetails(int code, String message) {
        this.code = code;
        this.message = message;
        exception = new DatacenterException(this);
    }

    public ErrorDetails(int code, String message, Throwable throwable) {
        this.code = code;
        this.message = message;
        if (throwable != null)
            this.details = throwable.getMessage();
        exception = new DatacenterException(this, throwable);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setDetails(Throwable throwable) {
        if (throwable == null)
            return;
        this.details = throwable.getMessage();
        this.exception.setStackTrace(throwable.getStackTrace());
    }

    public ErrorDetails message(String message) {
        this.message = message;
        return this;
    }

    public ErrorDetails code(int code) {
        this.code = code;
        return this;
    }

    public ErrorDetails details(String details) {
        this.details = details;
        return this;
    }

    public void setException(DatacenterException storageException) {
        this.exception = exception;
    }

    public DatacenterException getException() {
        return exception;
    }

    public void throwException() throws DatacenterException {
        throw exception;
    }

    @Override
    public String toString() {
        return "ErrorDetails{" +
                "message='" + message + '\'' +
                ", code=" + code +
                ", details='" + details + '\'' +
                '}';
    }
}
