package com.korenevskiy.orderplugin;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.GroupByTypeComparator;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiNamedElement;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OrderTreeStructureProviderTest extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.addClass("package ann; public @interface Order { int value() default Integer.MAX_VALUE; }");
    }

    public void testPackageWithoutAnnotationsKeepsDefaultOrder() {
        myFixture.addClass("package p; public class Charlie {}");
        myFixture.addClass("package p; public class Alpha {}");
        myFixture.addClass("package p; public class Bravo {}");

        assertEquals(List.of("Alpha", "Bravo", "Charlie"), sortedNames("p", false));
        assertFalse(packageChildren("p").stream().anyMatch(n -> n instanceof OrderedNodes.OrderedClassNode));
    }

    public void testAnnotatedClassesFirstSortedByValueThenOthersByName() {
        myFixture.addClass("package p; import ann.Order; @Order(2) public class Alpha {}");
        myFixture.addClass("package p; public class Delta {}");
        myFixture.addClass("package p; import ann.Order; @Order(1) public class Charlie {}");
        myFixture.addClass("package p; public class Bravo {}");
        myFixture.addClass("package p; import ann.Order; @Order(-5) public class Echo {}");

        List<String> expected = List.of("Echo", "Charlie", "Alpha", "Bravo", "Delta");
        assertEquals(expected, sortedNames("p", false));
        assertEquals(expected, sortedNames("p", true));
    }

    public void testConstantValuesAndDefaultValue() {
        myFixture.addClass("package c; public class Consts { public static final int FIRST = -100; }");
        myFixture.addClass("package p; import ann.Order; @Order public class NoValue {}");
        myFixture.addClass("package p; import ann.Order; @Order(c.Consts.FIRST + 1) public class Constant {}");
        myFixture.addClass("package p; import ann.Order; @Order(value = 0) public class Named {}");
        myFixture.addClass("package p; public class Plain {}");

        assertEquals(List.of("Constant", "Named", "NoValue", "Plain"), sortedNames("p", false));
    }

    public void testEqualOrderFallsBackToName() {
        myFixture.addClass("package p; import ann.Order; @Order(1) public class Zulu {}");
        myFixture.addClass("package p; import ann.Order; @Order(1) public class Yankee {}");
        myFixture.addClass("package p; public class Alpha {}");

        assertEquals(List.of("Yankee", "Zulu", "Alpha"), sortedNames("p", false));
    }

    public void testSubpackagesAndNonJavaFilesInAnnotatedPackage() {
        myFixture.addClass("package p; import ann.Order; @Order(1) public class Zulu {}");
        myFixture.addClass("package p; public class Alpha {}");
        myFixture.addClass("package p.sub; public class Inner {}");
        myFixture.addFileToProject("p/notes.txt", "text");

        // Folders stay on top; the plain file is treated like a non-annotated entry.
        assertEquals(List.of("sub", "Zulu", "Alpha", "notes.txt"), sortedNames("p", false));
        assertEquals(List.of("sub", "Zulu", "Alpha", "notes.txt"), sortedNames("p", true));
    }

    private List<String> sortedNames(String packageName, boolean sortByType) {
        List<AbstractTreeNode<?>> children = new ArrayList<>(packageChildren(packageName));
        children.sort(new GroupByTypeComparator(sortByType));
        List<String> names = new ArrayList<>();
        for (AbstractTreeNode<?> child : children) {
            Object value = child.getValue();
            if (value instanceof PsiNamedElement named) names.add(named.getName());
            else if (value instanceof PsiFileSystemItem item) names.add(item.getName());
        }
        return names;
    }

    /** Children of the package node after all registered providers ran, as the Project view does. */
    private Collection<AbstractTreeNode<?>> packageChildren(String packageName) {
        PsiDirectory directory = myFixture.findPackage(packageName).getDirectories()[0];
        ViewSettings settings = ViewSettings.DEFAULT;
        ProjectViewNode<PsiDirectory> parent = new PsiDirectoryNode(getProject(), directory, settings);
        Collection<AbstractTreeNode<?>> children = new ArrayList<>(parent.getChildren());
        for (TreeStructureProvider provider : TreeStructureProvider.EP.getExtensions(getProject())) {
            children = provider.modify(parent, children, settings);
        }
        return children;
    }
}
