//package io.dataplus.processor;
//
//import com.sun.tools.javac.code.Flags;
//import com.sun.tools.javac.code.TypeTag;
//import com.sun.tools.javac.tree.JCTree;
//import com.sun.tools.javac.tree.TreeTranslator;
//import com.sun.tools.javac.util.List;
//import io.dataplus.annotation.ToString;
//
//import javax.annotation.processing.RoundEnvironment;
//import javax.annotation.processing.SupportedAnnotationTypes;
//import javax.annotation.processing.SupportedSourceVersion;
//import javax.lang.model.SourceVersion;
//import javax.lang.model.element.Element;
//import javax.lang.model.element.TypeElement;
//import javax.tools.Diagnostic;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// * 自定义注解 {@code @ToString} 处理器.
// *
// * @author Lam Tong
// * @see ToString
// */
//@SupportedAnnotationTypes(value = {"io.dataplus.annotation.ToString"})
//@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
//public class ToStringProcessor extends BaseAnnotationProcessor {
//
//    private static final Logger logger = Logger.getLogger(ToStringProcessor.class.getName());
//
//    @Override
//    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ToString.class);
//        for (Element element : elements) {
//            JCTree jcTree = super.trees.getTree(element);
//            jcTree.accept(new TreeTranslator() {
//                @Override
//                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
//                    messager.printMessage(Diagnostic.Kind.NOTE, "Starts process @ToString with class [" + jcClassDecl.name.toString() + "].");
//                    if (!Helper.hasToString(jcClassDecl)) {
//
//                    }
//                    messager.printMessage(Diagnostic.Kind.NOTE, "Ends process @ToString with class [" + jcClassDecl.name.toString() + "].");
//                }
//            });
//        }
//        return true;
//    }
//
//    /**
//     * 构建 {@code toString} 实例方法.
//     *
//     * @param className 类名
//     * @return toString 实例方法定义
//     */
//    private JCTree.JCMethodDecl buildToString(String className) {
//        logger.log(Level.INFO, "构建 [" + className + "] toString() 实例方法.");
//        return super.treeMaker.MethodDef(super.treeMaker.Modifiers(Flags.PUBLIC),
//                super.names.fromString(Constants.TO_STRING),
//                super.treeMaker.TypeIdent(TypeTag.VOID),
//                List.nil(),
//                List.nil(),
//                List.nil(),
//                null,
//                null);
//    }
//
//}
