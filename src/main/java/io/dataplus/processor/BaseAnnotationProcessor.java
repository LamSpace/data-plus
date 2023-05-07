package io.dataplus.processor;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 注解处理器公共类.
 *
 * @author Lam Tong
 */
public abstract class BaseAnnotationProcessor extends AbstractProcessor {

    private static final Logger logger = Logger.getLogger(BaseAnnotationProcessor.class.getName());

    Messager messager;

    JavacTrees trees;

    TreeMaker treeMaker;

    Names names;

    @SuppressWarnings(value = {"SameParameterValue"})
    private static <T> T jbUnwrap(Class<? extends T> iface, T wrapper) {
        T unwrapped = null;
        try {
            final Class<?> apiWrappers = wrapper.getClass().getClassLoader().loadClass("org.jetbrains.jps.javac.APIWrappers");
            final Method unwrapMethod = apiWrappers.getDeclaredMethod("unwrap", Class.class, Object.class);
            unwrapped = iface.cast(unwrapMethod.invoke(null, iface, wrapper));
        } catch (Throwable ignored) {
        }
        return unwrapped != null ? unwrapped : wrapper;
    }

    @Override
    public final synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        if (!(processingEnv instanceof JavacProcessingEnvironment)) {
            processingEnv = jbUnwrap(ProcessingEnvironment.class, processingEnv);
        }
        this.trees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        logger.log(Level.INFO, "注解处理器公共类初始化完成.");
    }

}
