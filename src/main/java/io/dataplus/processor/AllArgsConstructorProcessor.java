package io.dataplus.processor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.dataplus.annotation.AllArgsConstructor;

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
 * 自定义注解 {@code AllArgsConstructor} 处理器.
 *
 * @author Lam Tong
 * @see AllArgsConstructor
 */
@SupportedAnnotationTypes(value = {"io.dataplus.annotation.AllArgsConstructor"})
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
public class AllArgsConstructorProcessor extends BaseAnnotationProcessor {

    private static final Logger logger = Logger.getLogger(AllArgsConstructorProcessor.class.getName());

    private List<JCTree.JCVariableDecl> variables;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(AllArgsConstructor.class);
        for (Element element : elements) {
            JCTree jcTree = super.trees.getTree(element);
            super.treeMaker.pos = jcTree.pos;
            jcTree.accept(new TreeTranslator() {

                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    String className = jcClassDecl.name.toString();
                    messager.printMessage(Diagnostic.Kind.NOTE, "Starts process @AllArgsConstructor with class [" + className + "].");

                    variables = Helper.getJCVariableDecls(jcClassDecl);
                    if (!Helper.hasAllArgsConstructor(jcClassDecl, variables)) {
                        jcClassDecl.defs = jcClassDecl.defs.append(buildAllArgsConstructor(className));
                        logger.log(Level.INFO, "构建 [" + className + "] 全参数构造方法完毕.");
                    }
                    variables = null;

                    messager.printMessage(Diagnostic.Kind.NOTE, "Ends process @AllArgsConstructor with class [" + className + "].");
                }

            });
        }
        return true;
    }

    /**
     * 构建目标类的全参构造方法.
     *
     * @return 目标类的全参构造方法
     */
    private JCTree.JCMethodDecl buildAllArgsConstructor(String className) {
        logger.log(Level.INFO, "构建 [" + className + "] 全参数构造方法.");
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        for (JCTree.JCVariableDecl variable : variables) {
            // 构建全参数构造方法中的 this.xx = xx;
            statements.append(
                    super.treeMaker.Exec(
                            super.treeMaker.Assign(
                                    super.treeMaker.Select(
                                            super.treeMaker.Ident(super.names.fromString(Constants.THIS)),
                                            super.names.fromString(variable.name.toString())
                                    ),
                                    super.treeMaker.Ident(super.names.fromString(variable.name.toString()))
                            )
                    )
            );
        }
        JCTree.JCBlock block = super.treeMaker.Block(0, statements.toList());
        return super.treeMaker.MethodDef(
                super.treeMaker.Modifiers(Flags.PUBLIC),
                super.names.fromString(Constants.CONSTRUCTOR_NAME),
                super.treeMaker.TypeIdent(TypeTag.VOID),
                List.nil(),
                Helper.cloneFromVariables(super.treeMaker, variables),
                List.nil(),
                block,
                null
        );
    }

}
