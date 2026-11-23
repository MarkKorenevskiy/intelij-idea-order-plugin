package com.korenevskiy.orderplugin;

import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiModifierList;
import org.jetbrains.annotations.Nullable;

/**
 * Reads the value of an {@code @Order(n)} annotation from PSI.
 * <p>
 * The annotation is matched by its simple name {@code Order}, so any annotation with that name works: Spring's
 * {@code org.springframework.core.annotation.Order} or a project-specific one. Matching by simple name avoids
 * reference resolution, which is unavailable while the IDE is indexing.
 * <p>
 * Supported value forms:
 * <ul>
 *   <li>literals: {@code @Order(1)}, {@code @Order(-5)}, {@code @Order(value = 2)};</li>
 *   <li>compile-time constant expressions: {@code @Order(Priorities.HIGHEST + 1)} (evaluated once indexing is done);</li>
 *   <li>no value: {@code @Order}, treated as {@link #DEFAULT_ORDER}, the same default Spring uses.</li>
 * </ul>
 * Every method must be called inside a read action.
 */
public final class OrderAnnotations {

    /** Simple name of the recognized annotation. */
    static final String ANNOTATION_NAME = "Order";

    /**
     * Value used for {@code @Order} without a value or with a value that can't be computed.
     * Equals {@link Integer#MAX_VALUE}, matching Spring's {@code Ordered.LOWEST_PRECEDENCE}.
     */
    static final int DEFAULT_ORDER = Integer.MAX_VALUE;

    private OrderAnnotations() {
    }

    /**
     * Returns the {@code @Order} value declared directly on a class. Inherited annotations are ignored.
     *
     * @param psiClass the class to inspect; may be {@code null}
     * @return the order value, {@link #DEFAULT_ORDER} if the value can't be computed,
     *         or {@code null} if the class is not annotated
     */
    public static @Nullable Integer orderOf(@Nullable PsiClass psiClass) {
        if (psiClass == null) return null;
        PsiModifierList modifierList = psiClass.getModifierList();
        if (modifierList == null) return null;
        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
            if (isOrderAnnotation(annotation)) {
                return valueOf(annotation);
            }
        }
        return null;
    }

    /**
     * Returns the smallest {@code @Order} value among the top-level classes of a file. Used for file nodes, which
     * the Project view shows instead of class nodes when a file has several top-level classes or
     * "Show members" is off.
     *
     * @param file the file to inspect; may be {@code null} or a file without classes
     * @return the smallest order value, or {@code null} if no top-level class in the file is annotated
     */
    public static @Nullable Integer orderOf(@Nullable PsiFile file) {
        if (!(file instanceof PsiClassOwner owner)) return null;
        Integer result = null;
        for (PsiClass psiClass : owner.getClasses()) {
            Integer order = orderOf(psiClass);
            if (order != null && (result == null || order < result)) {
                result = order;
            }
        }
        return result;
    }

    /** Returns whether the annotation's simple name is {@value #ANNOTATION_NAME}, without resolving it. */
    static boolean isOrderAnnotation(PsiAnnotation annotation) {
        PsiJavaCodeReferenceElement ref = annotation.getNameReferenceElement();
        String name = ref != null ? ref.getReferenceName() : StringUtil.getShortName(StringUtil.notNullize(annotation.getQualifiedName()));
        return ANNOTATION_NAME.equals(name);
    }

    /**
     * Evaluates the annotation's {@code value} attribute as a constant expression.
     * Falls back to {@link #DEFAULT_ORDER} if the value is missing, not numeric, or needs indexes that aren't ready.
     */
    private static int valueOf(PsiAnnotation annotation) {
        PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue("value");
        if (!(value instanceof PsiExpression expression)) return DEFAULT_ORDER;
        try {
            Object constant = JavaPsiFacade.getInstance(annotation.getProject())
                    .getConstantEvaluationHelper()
                    .computeConstantExpression(expression);
            return constant instanceof Number number ? number.intValue() : DEFAULT_ORDER;
        } catch (IndexNotReadyException e) {
            // A constant reference (e.g. Ordered.HIGHEST_PRECEDENCE) can't be resolved while indexing;
            // the view is refreshed when indexing finishes (see DumbModeRefresher).
            return DEFAULT_ORDER;
        }
    }
}
