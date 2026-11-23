package com.korenevskiy.orderplugin;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * Sort key for a class or file node in a package that contains at least one {@code @Order}-annotated class.
 * <p>
 * Keys compare as follows:
 * <ol>
 *   <li>annotated nodes ({@code order != null}) come before non-annotated ones;</li>
 *   <li>annotated nodes are ordered by ascending {@code order};</li>
 *   <li>ties, and all non-annotated nodes, are ordered by name using natural ordering
 *       ({@code Item2} before {@code Item10}), the same way the Project view sorts by default.</li>
 * </ol>
 *
 * @param order the {@code @Order} value, or {@code null} if the node is not annotated
 * @param name  the class or file name used as a tie-breaker
 */
public record OrderSortKey(@Nullable Integer order, @NotNull String name) implements Comparable<OrderSortKey> {

    private static final Comparator<OrderSortKey> COMPARATOR = Comparator
            .comparing((OrderSortKey key) -> key.order() == null)
            .thenComparing(OrderSortKey::order, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(OrderSortKey::name, StringUtil::naturalCompare);

    @Override
    public int compareTo(@NotNull OrderSortKey other) {
        return COMPARATOR.compare(this, other);
    }
}
