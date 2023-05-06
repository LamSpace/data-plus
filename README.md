## Data-Plus

> `Data-Plus` 是 Pluggable Annotation Processing API 示例程序.

---

### Pluggable Annotation Processing API 简介

> 该 API 是 JSR 269 提供的一台标准 API (Pluggable Annotation Processing) 来处理 Annotations，该 API 在编译期间而不是
> 运行期间处理注解, 相当于是编译器的一个插件, 因此称为插件式注解处理. 若注解处理器处理注解时产生了新的 Java 代码,
> 编译器会再调用一次注解处理器, 直到没有产生新代码为止. 每执行一次处理称为一个 `round`. 整个注解处理过程可以看做一个
> `round` 序列. JSR 269 主要被设计称为针对 Tools 或者容器的 API.<br/>

---

### 使用步骤

1. 自定义 Annotation Processor, 继承 javax.annotation.processing.AbstractProcessor.
2. 自定义注解, 需要指定 RetentionPolicy.SOURCE.
3. 自定义 Annotation Processor 中使用 javax.annotation.processing.SupportedAnnotationTypes 指定自定义注解.
4. 自定义 Annotation Processor 使用 javax.annotation.processing.SupportedSourceVersion 指定编译版本以及编译参数.

> 【注意】: 该 API 只能用于生成新文件, 不能用于修改现有文件.

---
