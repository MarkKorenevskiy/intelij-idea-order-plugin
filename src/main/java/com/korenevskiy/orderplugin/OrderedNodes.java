package com.korenevskiy.orderplugin;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.ClassTreeNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;

import java.util.Collection;

/**
 * Versions of the standard Project view nodes that sort by an {@link OrderSortKey}.
 * <p>
 * The Project view sorts children with {@code GroupByTypeComparator} after all {@code TreeStructureProvider}s
 * have run, so reordering the list in a provider has no effect. The comparator consults {@code getSortKey()}
 * ("Sort by Name") and {@code getTypeSortKey()} ("Sort by Type"), so these nodes override them. Apart from that,
 * they look and behave exactly like the nodes they replace.
 */
final class OrderedNodes {

    private OrderedNodes() {
    }

    /** A {@link ClassTreeNode} (a file shown as its single top-level class) that sorts by {@link OrderSortKey}. */
    static final class OrderedClassNode extends ClassTreeNode {
        private final OrderSortKey key;

        /**
         * @param project         the project
         * @param psiClass        the class shown by the node
         * @param settings        the view settings of the original node
         * @param nestedFileNodes the nested file nodes of the original node (for file nesting rules)
         * @param key             the sort key to use for both name and type sorting
         */
        OrderedClassNode(Project project, PsiClass psiClass, ViewSettings settings,
                         Collection<? extends AbstractTreeNode<?>> nestedFileNodes, OrderSortKey key) {
            super(project, psiClass, settings, nestedFileNodes);
            this.key = key;
        }

        @Override
        public Comparable<?> getSortKey() {
            return key;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Comparable getTypeSortKey() {
            return key;
        }
    }

    /** A {@link PsiFileNode} that sorts by {@link OrderSortKey}. */
    static final class OrderedFileNode extends PsiFileNode {
        private final OrderSortKey key;

        /**
         * @param project  the project
         * @param file     the file shown by the node
         * @param settings the view settings of the original node
         * @param key      the sort key to use
         */
        OrderedFileNode(Project project, PsiFile file, ViewSettings settings, OrderSortKey key) {
            super(project, file, settings);
            this.key = key;
        }

        @Override
        public Comparable<?> getSortKey() {
            return key;
        }
    }
}
