package io.dataplus.annotation;

import java.lang.annotation.*;

/**
 * 标记相关内容需要生成 {@code hello} 实例方法. 该注解为相关类生成以下实例方法.<br/>
 * <code>
 * public String hello() { return "hello from JSR269"; }
 * </code>
 *
 * @author Lam Tong
 */
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.SOURCE)
@Documented
public @interface Hello {
}
