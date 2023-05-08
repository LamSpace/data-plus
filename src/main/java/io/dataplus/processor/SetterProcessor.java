package io.dataplus.processor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import io.dataplus.annotation.Setter;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 自定义注解 {@code Setter} 注解处理器实现.
 *
 * @author Lam Tong
 * @see Setter
 */
@SupportedAnnotationTypes(value = {"io.dataplus.annotation.Setter"})
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
public class SetterProcessor extends BaseAnnotationProcessor {

    private static final Logger logger = Logger.getLogger(SetterProcessor.class.getName());

    private List<JCTree.JCVariableDecl> fields;

    private JCTree.JCClassDecl classDecl;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Setter.class);
        for (Element element : elements) {
            JCTree jcTree = super.trees.getTree(element);
            super.treeMaker.pos = jcTree.pos;
            jcTree.accept(new TreeTranslator() {

                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    String className = jcClassDecl.name.toString();
                    messager.printMessage(Diagnostic.Kind.NOTE, "Starts process @Setter with class [" + className + "].");

                    fields = Helper.getJCVariableDecls(jcClassDecl);
                    classDecl = jcClassDecl;
                    jcClassDecl.defs = jcClassDecl.defs.appendList(buildSetterMethods(className));
                    logger.log(Level.INFO, "构建 [" + className + "] 属性 setter 实例方法完毕.");
                    fields = null;
                    classDecl = null;

                    messager.printMessage(Diagnostic.Kind.NOTE, "Ends process @Setter with class [" + className + "].");
                }

            });
        }
        return true;
    }

    /**
     * 构建目标类属性 {@code setter} 实例方法集合.
     *
     * @param className 目标类名
     * @return setter 实例方法集合
     */
    private List<JCTree> buildSetterMethods(String className) {
        logger.log(Level.INFO, "构建 [" + className + "] 属性 setter 实例方法.");
        ListBuffer<JCTree> methods = new ListBuffer<>();
        for (JCTree.JCVariableDecl variableDecl : fields) {
            // 只有不包含 final 修饰符的属性才能生成 setter 实例方法
            if (!Helper.hasSetterMethod(classDecl, variableDecl)) {
                if (variableDecl.mods.getFlags().contains(Modifier.FINAL)) {
                    logger.log(Level.INFO, "[" + className + "] 包含 final 属性 " + variableDecl.name.toString() + ", 无法生成 setter 实例方法.");
                } else {
                    methods.append(this.buildSetterMethod(className, variableDecl));
                }
            }
        }
        return methods.toList();
    }

    /**
     * 构建目标属性 {@code Setter} 实例方法.
     *
     * @param className    目标类名
     * @param variableDecl 目标属性
     * @return 目标属性 setter 实例方法
     */
    private JCTree.JCMethodDecl buildSetterMethod(String className, JCTree.JCVariableDecl variableDecl) {
        logger.log(Level.INFO, "构建 [" + className + "] 属性 " + variableDecl.name.toString() + " setter 实例方法.");
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        statements.append(
                super.treeMaker.Exec(
                        super.treeMaker.Assign(
                                super.treeMaker.Select(
                                        super.treeMaker.Ident(super.names.fromString(Constants.THIS)),
                                        variableDecl.name
                                ),
                                super.treeMaker.Ident(variableDecl.name)
                        )
                )
        );
        JCTree.JCBlock block = super.treeMaker.Block(0, statements.toList());
        return super.treeMaker.MethodDef(
                super.treeMaker.Modifiers(Flags.PUBLIC),
                super.names.fromString(Helper.convertFieldNameToSetterMethodName(variableDecl)),
                super.treeMaker.TypeIdent(TypeTag.VOID),
                List.nil(),
                List.of(Helper.cloneFromVariable(super.treeMaker, variableDecl)),
                List.nil(),
                block,
                null
        );
    }

}
