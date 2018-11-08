package org.eif.controller.filters;

import javax.servlet.http.HttpServletRequest;

/**
 *    所有接口的拦截器需要实现的接口
 * @author zhangj
 * @date 2018年11月7日 下午3:30:10
 * @email zhangjin0908@Hotmail.com
 */
public interface Filter {
	String doRewrite(String body,HttpServletRequest req);
	/**
	 *    级别越小，则优先级越高
	 * @return 默认优先级为0
	 */
	default int getLevel() {
		return 0;
	}
}
