package io.dataplus.annotation;

import java.lang.annotation.*;

/**
 * 表示相关内容需要生成 {@code Getter} 实例方法.
 *
 * @author Lam Tong
 */
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.SOURCE)
@Documented
public @interface Getter {
}
