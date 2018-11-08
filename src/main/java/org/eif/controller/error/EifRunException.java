package org.eif.controller.error;

public class EifRunException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private int code;
	public EifRunException(int code) {
		super();
		this.code = code;
	}

	public EifRunException(int code,String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.code = code;
	}

	public EifRunException(int code,String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public EifRunException(int code,String message) {
		super(message);
		this.code = code;
	}

	public EifRunException(int code,Throwable cause) {
		super(cause);
		this.code = code;
	}

	public int getCode() {
		return code;
	}
	public String getErrorMsg() {
		return "";
	}
}
