/**
 * Orders classes in the IntelliJ IDEA Project view by an {@code @Order(n)} annotation.
 *
 * <h2>Ordering rules</h2>
 * Within a single package (directory node):
 * <ol>
 *   <li>If no class in the package carries {@code @Order}, the package is left untouched and the IDE's
 *       default ordering applies.</li>
 *   <li>Otherwise, annotated classes come first, in ascending order of their value; classes with equal
 *       values are ordered by name.</li>
 *   <li>Non-annotated classes and other files follow, ordered by name.</li>
 * </ol>
 * Subpackages are not affected and keep their usual position (on top, by default).
 *
 * <h2>Components</h2>
 * <ul>
 *   <li>{@link com.korenevskiy.orderplugin.OrderTreeStructureProvider}: applies the ordering to Project view nodes.</li>
 *   <li>{@link com.korenevskiy.orderplugin.OrderAnnotations}: reads {@code @Order} values from PSI.</li>
 *   <li>{@link com.korenevskiy.orderplugin.OrderSortKey}: the comparable key that encodes the ordering rules.</li>
 *   <li>{@link com.korenevskiy.orderplugin.OrderAnnotationChangeListener} and
 *       {@link com.korenevskiy.orderplugin.DumbModeRefresher}: keep the view up to date as code and indexes change.</li>
 * </ul>
 */
package com.korenevskiy.orderplugin;
