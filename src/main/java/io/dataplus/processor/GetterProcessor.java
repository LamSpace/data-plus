package io.dataplus.processor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.dataplus.annotation.Getter;

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
 * 自定义注解 {@code @Getter} 处理器.
 *
 * @author Lam Tong
 * @see Getter
 */
@SupportedAnnotationTypes(value = {"io.dataplus.annotation.Getter"})
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
public class GetterProcessor extends BaseAnnotationProcessor {

    private static final Logger logger = Logger.getLogger(GetterProcessor.class.getName());

    private List<JCTree.JCVariableDecl> fields;

    private JCTree.JCClassDecl classDecl;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Getter.class);
        for (Element element : elements) {
            JCTree jcTree = super.trees.getTree(element);
            super.treeMaker.pos = jcTree.pos;
            jcTree.accept(new TreeTranslator() {

                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    String className = jcClassDecl.name.toString();
                    messager.printMessage(Diagnostic.Kind.NOTE, "Starts process @Getter with class [" + className + "].");

                    fields = Helper.getJCVariableDecls(jcClassDecl);
                    classDecl = jcClassDecl;
                    jcClassDecl.defs = jcClassDecl.defs.appendList(buildGetterMethods(className));
                    logger.log(Level.INFO, "构建 [" + className + "] 属性 getter 实例方法完毕.");
                    fields = null;
                    classDecl = null;

                    messager.printMessage(Diagnostic.Kind.NOTE, "Ends process @Getter with class [" + className + "].");
                }

            });
        }
        return true;
    }

    /**
     * 构建目标类属性 {@code Getter} 实例方法集合.
     *
     * @param className 目标类名
     * @return 属性 {@code Getter} 实例方法集合
     */
    private List<JCTree> buildGetterMethods(String className) {
        logger.log(Level.INFO, "构建 [" + className + "] 属性 getter 实例方法.");
        ListBuffer<JCTree> methods = new ListBuffer<>();
        for (JCTree.JCVariableDecl variableDecl : fields) {
            if (!Helper.hasGetterMethod(classDecl, variableDecl)) {
                methods.append(this.buildGetterMethod(className, variableDecl));
                logger.log(Level.INFO, "构建 [" + className + "] 属性 " + variableDecl.name.toString() + " getter 实例方法完毕.");
            }
        }
        return methods.toList();
    }

    /**
     * 构建目标属性 {@code Getter} 实例方法.
     *
     * @param variableDecl 目标属性
     * @return 目标属性 {@code Getter} 实例方法
     */
    private JCTree.JCMethodDecl buildGetterMethod(String className, JCTree.JCVariableDecl variableDecl) {
        logger.log(Level.INFO, "构建 [" + className + "] 属性 " + variableDecl.name.toString() + " getter 实例方法.");
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        statements.append(
                super.treeMaker.Return(
                        super.treeMaker.Select(
                                super.treeMaker.Ident(super.names.fromString(Constants.THIS)),
                                variableDecl.name
                        )
                )
        );
        JCTree.JCBlock block = super.treeMaker.Block(0, statements.toList());
        return super.treeMaker.MethodDef(
                super.treeMaker.Modifiers(Flags.PUBLIC),
                super.names.fromString(Helper.convertFieldNameToGetterMethodName(variableDecl.name.toString())),
                variableDecl.vartype,
                List.nil(),
                List.nil(),
                List.nil(),
                block,
                null
        );
    }

}
