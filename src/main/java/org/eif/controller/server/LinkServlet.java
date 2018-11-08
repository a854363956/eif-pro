package org.eif.controller.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eif.controller.annotations.Link;
import org.eif.controller.error.EifRunException;
import org.eif.controller.error.ReplayAttackException;
import org.eif.controller.filters.Filter;
import org.eif.models.utils.Base64;
import org.eif.models.utils.CryptoJS;
import org.eif.models.utils.IOUtils;
import org.eif.models.utils.SpringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 通用的Link统一管理中心
 * 
 * @author zhangj
 * @date 2018年11月6日 下午12:08:22
 * @email zhangjin0908@Hotmail.com
 */
@WebServlet(name = "LinkServlet", urlPatterns = { "link.eif" }, loadOnStartup = 1)
public class LinkServlet extends HttpServlet {
	private static final long serialVersionUID = 5323536051023924826L;
	private static final String ERROR_404 = "No permission to access the current interface.";
	// 公钥
	private static String publicKey = "";
	// 私钥
	private static String privateKey = "";
	// 默认5秒的时间为失效时间 单位/毫秒
	private static final long FAILURE_TIME = 5000;

	// 在请求之前触发拦截器操作
	protected String doFilters(String body, HttpServletRequest req) {
		String[] beanResult = SpringUtils.getApplicationContext().getBeanNamesForType(Filter.class);
		for (String beanName : beanResult) {
			Filter filte = (Filter) SpringUtils.getApplicationContext().getBean(beanName);
			body = filte.doRewrite(body, req);
		}
		return body;
	}

	/**
	 *  请求数据的实体对象
	 * @author zhangj
	 * @date 2018年11月8日 下午9:15:32
	 * @email zhangjin0908@Hotmail.com
	 */
	class RequestCmd {
		private String type;
		private String bean;
		private String method;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getBean() {
			return bean;
		}

		public void setBean(String bean) {
			this.bean = bean;
		}

		public String getMethod() {
			return method;
		}

		public void setMethod(String method) {
			this.method = method;
		}
	}
	/**
	 *  采用Link协议来进行传输数据
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	protected void doLink(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		Date currenDate = new Date();
		try (OutputStream os = resp.getOutputStream(); InputStream is = req.getInputStream()) {
			try {
				// 当前读取的JSON数据
				String body = IOUtils.readInputStream(is);

				// 触发拦截器 对所有在Spring容器中，并且会调用org.eif.controller.filters.Filter#doRewrite 方法
				// 所有实现了org.eif.controller.filters.Filter的接口的方法
				// body = doFilters(body, req);

				// 转换成为JSON对象
				JSONObject jBody = JSON.parseObject(body);

				// 进行解密内容
				String ak = jBody.getString("AK");
				String ab = jBody.getString("AB");
				byte[] aesKey = CryptoJS.RSA.decrypt(CryptoJS.RSA.getPrivateKey(privateKey), Base64.decode(ak));
				jBody = JSON.parseObject(CryptoJS.AES.decrypt(ab, new String(aesKey, "UTF-8")));

				// 当前请求的路由 格式如下 spring::beanName#method
				String route = jBody.getString("route");
				// 当前请求的口令
				// String token = jBody.getString("token");
				// 当前客户端请求的时间
				Date time = jBody.getDate("time");
				// 请求封装的数据
				// String data = jBody.getString("data");

				// 如果用户请求的时间大于5秒的时间，就认为请求被拦截进行重放攻击导致的异常数据，对于这种数据不需要进行解析操作
				if (time.getTime() + FAILURE_TIME < currenDate.getTime()) {
					throw new ReplayAttackException(String.format("Discover replay attack, Route=[ %s ].", route));
				} else {
					// 如果用户没有传入进来route参数，则抛出异常表示无法访问
					if ("".equals(route) || route == null) {
						throw new EifRunException(1000);
					} else {
						// 解析成为对应的请求实体对象
						RequestCmd rc = conversionRequestCmd(route);
						// 如果是采用Spring的方式进行路由
						if("spring".equalsIgnoreCase(rc.getType())) {
							//    要调用的bean的名称
							String beanName = rc.getBean();
							//    要调用的方法名称
							String method   = rc.getMethod();
							Object beanObject = SpringUtils.getApplicationContext(getServletContext()).getBean(beanName);
						  Method met = beanObject.getClass().getDeclaredMethod(method, JSONObject.class);
						  if(met == null) {
							  throw new EifRunException(1002);
						  }else {
							  Link link = met.getAnnotation(Link.class);
							  if(link == null) {
								  throw new EifRunException(1003);
							  }else {
								  Object resultData = met.invoke(beanObject, jBody);
								  JSONObject result = new JSONObject();
								  result.put("data", resultData);
								  result.put("status", 0);
								  os.write(JSON.toJSONString(result).getBytes("UTF-8"));
								  return;
							     }
						    }
						}else {
							// 暂时不支持其他的路由方式，目前仅仅只是支持Spring的方式的路由
							JSONArray parame= new JSONArray();
							parame.add(route);
							throw new EifRunException(1001,JSON.toJSONString(parame));
						}
					}

				}
			} catch (EifRunException e) {
				JSONObject result = new JSONObject();
				result.put("data", e.getErrorMsg());
				result.put("status", -1); // -1 表示失败 0 表示成功
				os.write(JSON.toJSONString(result).getBytes("UTF-8"));
			} catch (Exception e) {
				JSONObject result = new JSONObject();
				result.put("data", e.getMessage());
				result.put("status", -1); // -1 表示失败 0 表示成功
				os.write(JSON.toJSONString(result).getBytes("UTF-8"));
			}
		}
	}
	
	private RequestCmd  conversionRequestCmd(String cmd) {
		RequestCmd rc = new RequestCmd();
		String [] cmds = cmd.split("::");
		rc.setType(cmds[0]);
		rc.setBean(cmds[1].split("#")[0]);
		rc.setMethod(cmds[1].split("#")[1]);
		return rc;
	}

	protected void doRsa(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try (OutputStream os = resp.getOutputStream()) {
			os.write(publicKey.getBytes("UTF-8"));
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			privateKey = IOUtils.readInputStream(
					config.getServletContext().getResourceAsStream("/org/eif/resources/cert/private.key"));
			publicKey = IOUtils.readInputStream(
					config.getServletContext().getResourceAsStream("/org/eif/resources/cert/public.key"));
			java.security.Security.addProvider(new BouncyCastleProvider());
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.init(config);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, ERROR_404);
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, ERROR_404);
	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, ERROR_404);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, ERROR_404);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, ERROR_404);
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, ERROR_404);
	}

	@Override
	protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, ERROR_404);
	}

	private static final String METHOD_DELETE = "DELETE";
	private static final String METHOD_HEAD = "HEAD";
	private static final String METHOD_GET = "GET";
	private static final String METHOD_OPTIONS = "OPTIONS";
	private static final String METHOD_POST = "POST";
	private static final String METHOD_PUT = "PUT";
	private static final String METHOD_TRACE = "TRACE";
	private static final String METHOD_RSA = "RSA";
	private static final String METHOD_LINK= "LINK";

	private static final String HEADER_IFMODSINCE = "If-Modified-Since";
	private static final String HEADER_LASTMOD = "Last-Modified";

	private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";
	private static ResourceBundle lStrings = ResourceBundle.getBundle(LSTRING_FILE);

	private void maybeSetLastModified(HttpServletResponse resp, long lastModified) {
		if (resp.containsHeader(HEADER_LASTMOD))
			return;
		if (lastModified >= 0)
			resp.setDateHeader(HEADER_LASTMOD, lastModified);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String method = req.getMethod();

		if (method.equals(METHOD_GET)) {
			long lastModified = getLastModified(req);
			if (lastModified == -1) {
				// servlet doesn't support if-modified-since, no reason
				// to go through further expensive logic
				doGet(req, resp);
			} else {
				long ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE);
				if (ifModifiedSince < lastModified) {
					// If the servlet mod time is later, call doGet()
					// Round down to the nearest second for a proper compare
					// A ifModifiedSince of -1 will always be less
					maybeSetLastModified(resp, lastModified);
					doGet(req, resp);
				} else {
					resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				}
			}

		} else if (method.equals(METHOD_HEAD)) {
			long lastModified = getLastModified(req);
			maybeSetLastModified(resp, lastModified);
			doHead(req, resp);

		} else if (method.equals(METHOD_POST)) {
			doPost(req, resp);

		} else if (method.equals(METHOD_PUT)) {
			doPut(req, resp);

		} else if (method.equals(METHOD_DELETE)) {
			doDelete(req, resp);

		} else if (method.equals(METHOD_OPTIONS)) {
			doOptions(req, resp);

		} else if (method.equals(METHOD_TRACE)) {
			doTrace(req, resp);

		} else if (method.equals(METHOD_RSA)) {
			doRsa(req, resp);
			
		} else if (method.equals(METHOD_LINK)){
			doLink(req, resp);
			
		}else {
			//
			// Note that this means NO servlet supports whatever
			// method was requested, anywhere on this server.
			//

			String errMsg = lStrings.getString("http.method_not_implemented");
			Object[] errArgs = new Object[1];
			errArgs[0] = method;
			errMsg = MessageFormat.format(errMsg, errArgs);

			resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, errMsg);
		}

	}

}
