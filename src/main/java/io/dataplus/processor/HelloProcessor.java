package io.dataplus.processor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.dataplus.annotation.Hello;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 自定义注解 {@code @Hello} 处理器.
 *
 * @author Lam Tong
 * @see Hello
 */
@SupportedAnnotationTypes(value = {"io.dataplus.annotation.Hello"})
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
public class HelloProcessor extends BaseAnnotationProcessor {

    private static final Logger logger = Logger.getLogger(HelloProcessor.class.getName());

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Hello.class);
        for (Element element : elements) {
            JCTree jcTree = super.trees.getTree(element);
            super.treeMaker.pos = jcTree.pos;
            jcTree.accept(new TreeTranslator() {

                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    String className = jcClassDecl.name.toString();
                    messager.printMessage(Diagnostic.Kind.NOTE, "Starts process @Hello with class [" + className + "].");

                    if (!Helper.hasHelloMethod(jcClassDecl)) {
                        jcClassDecl.defs = jcClassDecl.defs.append(buildHelloMethod(className));
                        logger.log(Level.INFO, "构建 [" + className + "] hello 实例方法完毕.");
                    }

                    messager.printMessage(Diagnostic.Kind.NOTE, "Ends process @Hello with class [" + className + "].");
                }

            });
        }
        return true;
    }

    /**
     * 构建实例方法 hello.
     *
     * @param className 类名
     * @return hello 实例方法定义
     */
    private JCTree.JCMethodDecl buildHelloMethod(String className) {
        logger.log(Level.INFO, "构建 [" + className + "] hello 实例方法.");
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();

        // 直接返回字符串常量
        statements.append(
                super.treeMaker.Return(
                        super.treeMaker.Literal("hello from JSR269")
                )
        );

        JCTree.JCBlock block = super.treeMaker.Block(0, statements.toList());
        return super.treeMaker.MethodDef(
                super.treeMaker.Modifiers(Flags.PUBLIC),
                super.names.fromString(Constants.HELLO_METHOD),
                super.treeMaker.Ident(super.names.fromString("String")),
                List.nil(),
                List.nil(),
                List.nil(),
                block,
                null
        );
    }

}
