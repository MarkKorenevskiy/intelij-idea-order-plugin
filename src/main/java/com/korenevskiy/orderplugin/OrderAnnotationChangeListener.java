package com.korenevskiy.orderplugin;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Re-sorts a package in the Project view when the annotations of one of its classes change.
 * <p>
 * The Project view's own PSI listener only refreshes the node of the edited file, not its siblings. Without this
 * listener, adding, removing or editing {@code @Order} would not move the class until the package is reloaded.
 * <p>
 * To keep typing cheap, only changes inside a modifier list (where annotations live) or whole-file reparses
 * trigger a refresh, and the refresh is limited to the containing directory.
 * <p>
 * Registered as a {@code psi.treeChangeListener} extension in {@code plugin.xml}.
 */
public final class OrderAnnotationChangeListener extends PsiTreeChangeAdapter {

    private final Project project;

    /**
     * Creates the listener. Instantiated by the platform for each open project.
     *
     * @param project the project whose Project view is updated
     */
    public OrderAnnotationChangeListener(Project project) {
        this.project = project;
    }

    @Override
    public void childAdded(@NotNull PsiTreeChangeEvent event) {
        handle(event);
    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {
        handle(event);
    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {
        handle(event);
    }

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        handle(event);
    }

    /**
     * Schedules a refresh of the directory containing the changed file if the change may affect an annotation.
     * The refresh runs later on the EDT and is skipped if the project is disposed in the meantime.
     */
    private void handle(PsiTreeChangeEvent event) {
        PsiFile file = event.getFile();
        if (!(file instanceof PsiClassOwner)) return;
        if (!affectsModifiers(event.getParent(), file)) return;
        PsiDirectory directory = file.getContainingDirectory();
        if (directory == null) return;

        ApplicationManager.getApplication().invokeLater(() -> {
            AbstractProjectViewPane pane = ProjectView.getInstance(project).getCurrentProjectViewPane();
            if (pane != null && directory.isValid()) {
                pane.updateFrom(directory, true, true);
            }
        }, project.getDisposed());
    }

    /**
     * Returns whether a change under {@code parent} can affect class annotations. Annotation edits land inside a
     * {@link PsiModifierList}; a whole-file reparse reports the file itself as the parent.
     */
    private static boolean affectsModifiers(PsiElement parent, PsiFile file) {
        return parent == file || PsiTreeUtil.getParentOfType(parent, PsiModifierList.class, false) != null;
    }
}
