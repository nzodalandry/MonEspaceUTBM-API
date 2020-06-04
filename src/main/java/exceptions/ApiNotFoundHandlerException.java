package exceptions;

import javax.servlet.ServletException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import dtos.ApiErrorMessageResponse;
import helpers.ErrorHandlerHelper;
import helpers.LogHelper;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ApiNotFoundHandlerException extends ServletException {

	private static final long serialVersionUID = -4931623900956989558L;
	LogHelper logger = LogHelper.getLogger();

	public ApiNotFoundHandlerException() {
		super("Error " + HttpStatus.NOT_FOUND.value() + ": Resource not found");
	}

	public ApiNotFoundHandlerException(String message) {
		super(message);
		logger.logDebug("in ApiNotFoundException " + message);
	}

	public ApiNotFoundHandlerException(String message, Throwable exc) {
		super(message, exc);
		logger.logDebug("in ApiNotFoundException " + message);
	}

	public ApiErrorMessageResponse getResponseDto() {

		ApiErrorMessageResponse errorInfo = new ApiErrorMessageResponse();
		String description = "";
		if (getRootCause() != null) {
			description = ErrorHandlerHelper.getStackTrace(getRootCause());
		} else if (getMessage() != null && !getMessage().isEmpty()) {
			description = getMessage();
		} else {
			description = "Resource not found in this server";
		}

		errorInfo.setExeption(getRootCause());

		errorInfo.setData(null);
		errorInfo.setDescription(description);
		errorInfo.setError("API_RESOURCE_NOT_FOUND");
		errorInfo.setId(System.currentTimeMillis());
		errorInfo.setStatus(HttpStatus.NOT_FOUND.value());

		return errorInfo;
	}
}
