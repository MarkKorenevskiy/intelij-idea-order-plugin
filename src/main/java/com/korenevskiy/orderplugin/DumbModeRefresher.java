package com.korenevskiy.orderplugin;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;

/**
 * Refreshes the Project view when indexing finishes.
 * <p>
 * Values that reference constants, such as {@code @Order(Priorities.HIGHEST)}, can't be evaluated while the IDE is
 * indexing (dumb mode), so {@link OrderAnnotations} temporarily treats them as {@link OrderAnnotations#DEFAULT_ORDER}.
 * Once indexes are ready, this listener rebuilds the view so those classes move to their correct positions.
 * <p>
 * Registered as a project-level listener for {@link DumbService.DumbModeListener#DUMB_MODE} in {@code plugin.xml}.
 */
public final class DumbModeRefresher implements DumbService.DumbModeListener {

    private final Project project;

    /**
     * Creates the listener. Instantiated by the platform for each open project.
     *
     * @param project the project whose Project view is refreshed
     */
    public DumbModeRefresher(Project project) {
        this.project = project;
    }

    /** Refreshes the Project view so annotation values that depend on indexes are re-evaluated. */
    @Override
    public void exitDumbMode() {
        if (!project.isDisposed()) {
            ProjectView.getInstance(project).refresh();
        }
    }
}
