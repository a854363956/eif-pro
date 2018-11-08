package org.eif.models.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
/**
 *  用来处理IO的工具类
 * @author zhangj
 * @date 2018年11月6日 下午8:30:38
 * @email zhangjin0908@Hotmail.com
 */
public class IOUtils {
	/**
	 * 将Input流转换为字符串
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static String readInputStream(java.io.InputStream in ) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = in.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString("UTF-8");
	}
}
