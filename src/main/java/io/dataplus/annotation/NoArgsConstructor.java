package io.dataplus.annotation;

import java.lang.annotation.*;

/**
 * 标记相关内容需要生成无参构造函数.
 *
 * @author Lam Tong
 */
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.SOURCE)
@Documented
public @interface NoArgsConstructor {
}
