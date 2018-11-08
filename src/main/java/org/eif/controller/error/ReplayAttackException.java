package org.eif.controller.error;
/**
 *   如果发现重放攻击则抛出异常
 * @author zhangj
 * @date 2018年11月7日 下午2:53:32
 * @email zhangjin0908@Hotmail.com
 */
public class ReplayAttackException extends RuntimeException{
	private static final long serialVersionUID = -2778472862645813531L;

	public ReplayAttackException() {
		super();
	}

	public ReplayAttackException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ReplayAttackException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReplayAttackException(String message) {
		super(message);
	}

	public ReplayAttackException(Throwable cause) {
		super(cause);
	}

}
