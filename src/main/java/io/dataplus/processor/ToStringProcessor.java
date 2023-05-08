package io.dataplus.processor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.dataplus.annotation.ToString;

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
 * 自定义注解 {@code @ToString} 处理器.
 *
 * @author Lam Tong
 * @see ToString
 */
@SupportedAnnotationTypes(value = {"io.dataplus.annotation.ToString"})
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
public class ToStringProcessor extends BaseAnnotationProcessor {

    private static final Logger logger = Logger.getLogger(ToStringProcessor.class.getName());

    private List<JCTree.JCVariableDecl> fields;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ToString.class);
        for (Element element : elements) {
            JCTree jcTree = super.trees.getTree(element);
            super.treeMaker.pos = jcTree.pos;
            jcTree.accept(new TreeTranslator() {

                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    String className = jcClassDecl.name.toString();
                    messager.printMessage(Diagnostic.Kind.NOTE, "Starts process @ToString with class [" + className + "].");

                    fields = Helper.getJCVariableDecls(jcClassDecl);
                    if (!Helper.hasToString(jcClassDecl)) {
                        jcClassDecl.defs = jcClassDecl.defs.append(buildToString(className));
                        logger.log(Level.INFO, "构建 [" + className + "] toString 实例方法完毕.");
                    }
                    fields = null;

                    messager.printMessage(Diagnostic.Kind.NOTE, "Ends process @ToString with class [" + className + "].");
                }

            });
        }
        return true;
    }

    /**
     * 构建 {@code toString} 实例方法.
     *
     * @param className 类名
     * @return toString 实例方法定义
     */
    @SuppressWarnings(value = {"AlibabaMethodTooLong"})
    private JCTree.JCMethodDecl buildToString(String className) {
        logger.log(Level.INFO, "构建 [" + className + "] toString 实例方法.");
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();

        JCTree.JCVariableDecl previous = null;
        JCTree.JCExpression expression = super.treeMaker.Literal(className.concat("{"));
        for (int i = 0; i < fields.size(); i++) {
            JCTree.JCVariableDecl variableDecl = fields.get(i);
            String fieldName = variableDecl.name.toString().concat("=");
            if (Constants.STRING_CLASS_NAME.equals(variableDecl.vartype.type.toString())) {
                fieldName = fieldName.concat("'");
            }
            if (i > 0) {
                if (Constants.STRING_CLASS_NAME.equals(previous.vartype.type.toString())) {
                    fieldName = "', ".concat(fieldName);
                } else {
                    fieldName = ", ".concat(fieldName);
                }
            }
            expression = super.treeMaker.Binary(
                    JCTree.Tag.PLUS,
                    expression,
                    super.treeMaker.Literal(fieldName)
            );
            expression = super.treeMaker.Binary(
                    JCTree.Tag.PLUS,
                    expression,
                    super.treeMaker.Ident(super.names.fromString(variableDecl.name.toString()))
            );
            previous = variableDecl;
        }
        String endWith = "} => Created By Data-Plus.";
        if (previous != null && Constants.STRING_CLASS_NAME.equals(previous.vartype.type.toString())) {
            endWith = "'".concat(endWith);
        }
        expression = super.treeMaker.Binary(
                JCTree.Tag.PLUS,
                expression,
                super.treeMaker.Literal(endWith)
        );

        statements.append(
                super.treeMaker.Return(
                        expression
                )
        );

        JCTree.JCBlock block = super.treeMaker.Block(0, statements.toList());
        return super.treeMaker.MethodDef(super.treeMaker.Modifiers(Flags.PUBLIC),
                super.names.fromString(Constants.TO_STRING),
                super.treeMaker.Ident(super.names.fromString(Constants.STRING_NAME)),
                List.nil(),
                List.nil(),
                List.nil(),
                block,
                null);
    }

}
