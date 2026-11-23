package com.korenevskiy.orderplugin;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.ClassTreeNode;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Orders the classes in a package in the Project view by their {@code @Order(n)} annotation.
 * <ul>
 *   <li>If no class in the package is annotated, the children are returned untouched (default ordering).</li>
 *   <li>Otherwise, annotated classes come first, sorted by value, followed by non-annotated classes and other files,
 *       sorted by name. See {@link OrderSortKey} for the exact rules.</li>
 * </ul>
 * The provider doesn't reorder the list itself, because the Project view sorts children again afterwards. Instead,
 * it replaces nodes with {@link OrderedNodes} copies whose sort keys produce the desired order.
 * <p>
 * Only the stock {@link ClassTreeNode} and {@link PsiFileNode} are replaced (exact class match), so nodes created by
 * other plugins keep their behaviour. The provider is {@link DumbAware}: during indexing, values that need
 * constant resolution temporarily fall back to {@link OrderAnnotations#DEFAULT_ORDER}.
 * <p>
 * Registered in {@code plugin.xml} with {@code order="last"}, so it runs after the Java plugin turns files into
 * class nodes.
 */
public final class OrderTreeStructureProvider implements TreeStructureProvider, DumbAware {

    /** Creates the provider. Instantiated by the platform from {@code plugin.xml}. */
    public OrderTreeStructureProvider() {
    }

    /**
     * Applies {@code @Order}-based sort keys to the children of a directory node.
     *
     * @param parent   the node whose children are being built; only {@link PsiDirectoryNode}s are processed
     * @param children the children produced by the platform and the providers that ran earlier
     * @param settings the current Project view settings
     * @return {@code children} unchanged if the package has no annotated class; otherwise a new list in which
     *         class and file nodes carry an {@link OrderSortKey}
     */
    @Override
    public @NotNull Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent,
                                                         @NotNull Collection<AbstractTreeNode<?>> children,
                                                         ViewSettings settings) {
        if (!(parent instanceof PsiDirectoryNode)) return children;

        List<Integer> orders = new ArrayList<>(children.size());
        boolean anyAnnotated = false;
        for (AbstractTreeNode<?> child : children) {
            Integer order = orderOf(child);
            orders.add(order);
            anyAnnotated |= order != null;
        }
        if (!anyAnnotated) return children;

        List<AbstractTreeNode<?>> result = new ArrayList<>(children.size());
        int i = 0;
        for (AbstractTreeNode<?> child : children) {
            result.add(withSortKey(child, orders.get(i++), settings));
        }
        return result;
    }

    /** Returns the {@code @Order} value of a stock class/file node, or {@code null} for other nodes or no annotation. */
    private static @Nullable Integer orderOf(AbstractTreeNode<?> node) {
        if (node.getClass() == ClassTreeNode.class) {
            return OrderAnnotations.orderOf(((ClassTreeNode) node).getPsiClass());
        }
        if (node.getClass() == PsiFileNode.class) {
            return OrderAnnotations.orderOf(((PsiFileNode) node).getValue());
        }
        return null;
    }

    /** Replaces a stock class/file node with an equivalent node sorted by {@link OrderSortKey}; returns other nodes as is. */
    private static AbstractTreeNode<?> withSortKey(AbstractTreeNode<?> node, @Nullable Integer order, ViewSettings settings) {
        if (node.getClass() == ClassTreeNode.class) {
            ClassTreeNode classNode = (ClassTreeNode) node;
            PsiClass psiClass = classNode.getPsiClass();
            if (psiClass == null) return node;
            OrderSortKey key = new OrderSortKey(order, String.valueOf(psiClass.getName()));
            return new OrderedNodes.OrderedClassNode(node.getProject(), psiClass, settings, classNode.getNestedFileNodes(), key);
        }
        if (node.getClass() == PsiFileNode.class) {
            PsiFile file = ((PsiFileNode) node).getValue();
            if (file == null) return node;
            OrderSortKey key = new OrderSortKey(order, file.getName());
            return new OrderedNodes.OrderedFileNode(node.getProject(), file, settings, key);
        }
        return node;
    }
}
