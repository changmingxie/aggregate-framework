package org.aggregateframework.alert;

/**
 * @Author huabao.fang
 * @Date 2022/6/7 00:28
 * 命名需要调整 TODO
 **/
public class AlertException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

    public AlertException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public AlertException(ResponseCodeEnum responseCodeEnum) {
        this(responseCodeEnum.getCode(), responseCodeEnum.getMessage());
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getMessage() {
        return this.errorCode + "-" + this.errorMessage;
    }
}
