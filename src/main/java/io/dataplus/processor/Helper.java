package io.dataplus.processor;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.lang.model.element.Modifier;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 自定义注解处理器工具类.
 *
 * @author Lam Tong
 */
final class Helper {

    @SuppressWarnings(value = {"unused"})
    private static final Logger logger = Logger.getLogger(Helper.class.getName());


    /**
     * 判断目标类是否存在无参构造方法. 判断条件如下:
     * <ol>
     *     <li>方法名为 {@code <init>}</li>
     *     <li>方法参数个数为 0</li>
     *     <li>目标类中不存在修饰符包含 final 的属性</li>
     * </ol>
     * 三个条件缺一不可.
     *
     * @param jcClass 目标类的 JCClass 实例
     * @return true: 目标类构造方法无任何参数时返回 true, 否则返回 false
     */
    static boolean hasNoArgsConstructor(JCTree.JCClassDecl jcClass) {
        for (JCTree jcTree : jcClass.defs) {
            if (jcTree.getKind().equals(Tree.Kind.VARIABLE)) {
                JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) jcTree;
                Set<Modifier> flags = variableDecl.mods.getFlags();
                if (!flags.contains(Modifier.STATIC) && flags.contains(Modifier.FINAL)) {
                    logger.log(Level.INFO, "[" + jcClass.name.toString() + "] 包含 final 属性 " + variableDecl.name.toString() + ", 无法添加无参构造方法.");
                    return true;
                }
            }
        }
        for (JCTree jcTree : jcClass.defs) {
            if (jcTree.getKind().equals(Tree.Kind.METHOD)) {
                JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
                if (Constants.CONSTRUCTOR_NAME.equals(jcMethodDecl.name.toString())) {
                    return jcMethodDecl.params.size() == 0;
                }
            }
        }
        return false;
    }

    /**
     * 判断目标类是否存在全参数构造方法.
     *
     * @param jcClass 目标类的 JCClass 实例
     * @return true: 目标类的构造方法数量等于目标类的属性数量且每个属性均赋值, 否则返回 false
     */
    static boolean hasAllArgsConstructor(JCTree.JCClassDecl jcClass, List<JCTree.JCVariableDecl> variableDecls) {
        // 全参构造方法判断条件: 方法名为 <init> 且参数个数与
        for (JCTree jcTree : jcClass.defs) {
            if (jcTree.getKind().equals(Tree.Kind.METHOD)) {
                JCTree.JCMethodDecl method = (JCTree.JCMethodDecl) jcTree;
                if (Constants.CONSTRUCTOR_NAME.equals(method.name.toString()) && variableDecls.size() == method.params.size()) {
                    for (JCTree.JCVariableDecl variableDecl : variableDecls) {
                        if (!method.params.contains(variableDecl)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取目标类的所有属性集合.
     *
     * @param jcClassDecl 目标类的 JCClass 实例
     * @return 目标类的所有属性集合.
     */
    @SuppressWarnings(value = {"AlibabaLowerCamelCaseVariableNaming"})
    static List<JCTree.JCVariableDecl> getJCVariableDecls(JCTree.JCClassDecl jcClassDecl) {
        ListBuffer<JCTree.JCVariableDecl> variableDecls = new ListBuffer<>();
        for (JCTree jcTree : jcClassDecl.defs) {
            if (isValidField(jcTree)) {
                variableDecls.append((JCTree.JCVariableDecl) jcTree);
            }
        }
        return variableDecls.toList();
    }

    /**
     * 判断目标类是否存在 hello() 实例方法.
     *
     * @param jcClass 目标类的 JCClass 实例
     * @return true 或者 false
     */
    static boolean hasHelloMethod(JCTree.JCClassDecl jcClass) {
        for (JCTree jcTree : jcClass.defs) {
            if (jcTree.getKind().equals(Tree.Kind.METHOD)) {
                JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) jcTree;
                if (Constants.HELLO_METHOD.equals(methodDecl.name.toString())) {

                    return methodDecl.params.size() == 0;
                }
            }
        }
        return false;
    }


    /**
     * 判断目标类是否存在 toString 实例方法.
     *
     * @param jcClass 目标类的 JCClass 实例
     * @return true 或者 false
     */
    static boolean hasToString(JCTree.JCClassDecl jcClass) {
        for (JCTree jcTree : jcClass.defs) {
            if (jcTree.getKind().equals(Tree.Kind.METHOD)) {
                JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
                if (Constants.TO_STRING.equals(jcMethodDecl.name.toString())) {
                    // TODO: 2023/5/7 toString 实例方法两要素: 方法名为 toString、参数个数为 0 以及 返回类型为 String
                    return jcMethodDecl.params.size() == 0;
                }
            }
        }
        return false;
    }

    /**
     * 克隆根据给定的目标类属性集合.
     *
     * @param treeMaker TreeMaker 实例
     * @param variables 目标类属性集合
     * @return 克隆的属性集合
     */
    static List<JCTree.JCVariableDecl> cloneFromVariables(TreeMaker treeMaker, List<JCTree.JCVariableDecl> variables) {
        ListBuffer<JCTree.JCVariableDecl> variableList = new ListBuffer<>();
        for (JCTree.JCVariableDecl variable : variables) {
            variableList.append(cloneFromVariable(treeMaker, variable));
        }
        return variableList.toList();
    }

    /**
     * 克隆给定的目标类属性.
     *
     * @param maker        TreeMaker 实例
     * @param variableDecl 目标类属性
     * @return 克隆的属性
     */
    static JCTree.JCVariableDecl cloneFromVariable(TreeMaker maker, JCTree.JCVariableDecl variableDecl) {
        return maker.VarDef(
                maker.Modifiers(Flags.PARAMETER),
                variableDecl.name,
                variableDecl.vartype,
                null
        );
    }

    /**
     * 判断给定的目标类中是否存在属性的 {@code Getter} 实例方法
     *
     * @param classDecl    目标类实例
     * @param variableDecl 目标类属性实例
     * @return true 或者 false
     */
    static boolean hasGetterMethod(JCTree.JCClassDecl classDecl, JCTree.JCVariableDecl variableDecl) {
        String getterMethodName = convertFieldNameToGetterMethodName(variableDecl.name.toString());
        for (JCTree jcTree : classDecl.defs) {
            if (jcTree.getKind().equals(Tree.Kind.METHOD)) {
                JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
                if (getterMethodName.equals(jcMethodDecl.name.toString())
                        && jcMethodDecl.params.size() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断给定的目标类中是否存在属性的 {@code Setter} 实例方法
     *
     * @param classDecl    目标类实例
     * @param variableDecl 目标类属性实例
     * @return true 或者 false
     */
    static boolean hasSetterMethod(JCTree.JCClassDecl classDecl, JCTree.JCVariableDecl variableDecl) {
        String setterMethodName = convertFieldNameToSetterMethodName(variableDecl.name.toString());
        for (JCTree jcTree : classDecl.defs) {
            if (jcTree.getKind().equals(Tree.Kind.METHOD)) {
                JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) jcTree;
                if (setterMethodName.equals(methodDecl.name.toString())
                        && methodDecl.params.size() == 1
                        && methodDecl.params.get(0).vartype.type.equals(variableDecl.vartype.type)
                        && methodDecl.getReturnType().type.equals(new Type.JCVoidType())) {
                    // setter 实例方法三要素: setter 方法名、参数个数为 1 且参数类型为属性类型以及方法返回值为 void
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 将属性名转换为对应的 {@code Getter} 实例方法名.
     *
     * @param fieldName 属性名
     * @return Getter 实例方法名
     */
    static String convertFieldNameToGetterMethodName(String fieldName) {
        // TODO: 2023/5/7 布尔类型的变量另算
        return Constants.GET + StringUtils.capitalize(fieldName);
    }

    /**
     * 将属性名转换为对应的 {@code Setter} 实例方法名.
     *
     * @param fieldName 属性名
     * @return Setter 实例方法名
     */
    static String convertFieldNameToSetterMethodName(String fieldName) {
        // TODO: 2023/5/7 布尔类型的变量另算
        return Constants.SET + StringUtils.capitalize(fieldName);
    }

    /**
     * 判断目标字段是否是合法的类属性, 即实例属性.
     *
     * @param tree 目标类属性的 JCTree 实例
     * @return true: 实例属性, 否则返回 false
     */
    private static boolean isValidField(JCTree tree) {
        if (tree.getKind().equals(Tree.Kind.VARIABLE)) {
            JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) tree;
            Set<Modifier> flags = variableDecl.mods.getFlags();
            return !flags.contains(Modifier.STATIC) && !flags.contains(Modifier.FINAL);
        }
        return false;
    }

}
