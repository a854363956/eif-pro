package org.eif.controller.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 *    用来标明当前方法是否允许公布
 * @author zhangj
 * @date 2018年11月6日 下午3:56:08
 * @email zhangjin0908@Hotmail.com
   */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Link {
}	
