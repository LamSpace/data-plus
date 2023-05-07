package io.dataplus.processor;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.lang.model.element.Modifier;
import java.util.Set;
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
     * 判断目标类是否存在无参构造方法.
     *
     * @param jcClass 目标类的 JCClass 实例
     * @return true: 目标类构造方法无任何参数时返回 true, 否则返回 false
     */
    static boolean hasNoArgsConstructor(JCTree.JCClassDecl jcClass) {
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
                    // TODO: 2023/5/7 todo
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
