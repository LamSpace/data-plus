package io.dataplus.processor;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;

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
     * @return true 或者 false
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

}
