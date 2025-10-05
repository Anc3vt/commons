/*
 * Copyright (C) 2025 Ancevt.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ancevt.util.tree;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A generic, mutable, and thread-safe tree node representation.
 * <p>
 * Each {@code TreeNode<T>} holds a single value of type {@code T}, an optional set of key-value properties,
 * a reference to its parent node, and a list of child nodes. The tree structure is fully navigable
 * (upwards and downwards) and supports both sequential and parallel traversal.
 * </p>
 *
 * <h2>Features</h2>
 * <ul>
 *     <li>Thread-safe read/write access using {@link ReadWriteLock}</li>
 *     <li>Recursive traversal with {@link #walk()} and {@link #parallelWalk(Consumer)}</li>
 *     <li>Tree search with {@link #find(Predicate)}</li>
 *     <li>Automatic parent-child relationship management</li>
 *     <li>Pretty-print representation with {@link #toTreeString()}</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * All read and write operations are protected by a {@link ReentrantReadWriteLock}.
 * Multiple readers are allowed concurrently, but write operations (adding/removing children or modifying values)
 * are exclusive.
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * TreeNode<String> root = TreeNode.of("root");
 * TreeNode<String> a = TreeNode.of("A");
 * TreeNode<String> b = TreeNode.of("B");
 * TreeNode<String> c = TreeNode.of("C");
 *
 * root.add(a);
 * root.add(b);
 * a.add(c);
 *
 * System.out.println(root.toTreeString());
 *
 * // Output:
 * // root
 * // ├─ A
 * // │  └─ C
 * // └─ B
 * }</pre>
 *
 * @param <T> the type of the node's value
 */
public class TreeNode<T> implements Iterable<TreeNode<T>> {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<String, Object> properties;
    private final List<TreeNode<T>> children = new ArrayList<>();
    private TreeNode<T> parent;
    private T value;

    private TreeNode(T value, Map<String, Object> properties) {
        this.value = value;
        this.properties = new HashMap<>(properties);
    }

    /**
     * Creates a new {@code TreeNode} containing the specified value.
     * <p>
     * The node is created with no parent and an empty property map.
     * </p>
     *
     * @param value the value to store in this node
     * @param <T>   the type of the node's value
     * @return a new {@code TreeNode<T>} instance
     */
    public static <T> TreeNode<T> of(T value) {
        return new TreeNode<>(value, Collections.emptyMap());
    }

    // --- Core API ---

    /**
     * Returns the value stored in this node.
     * <p>
     * The method acquires a read lock for thread safety.
     * </p>
     *
     * @return the node value
     */
    public T getValue() {
        lock.readLock().lock();
        try {
            return value;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Updates the value of this node.
     *
     * @param value the new value to set
     */
    public void setValue(T value) {
        lock.writeLock().lock();
        try {
            this.value = value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns an {@link Optional} containing this node’s parent, or empty if it is the root.
     *
     * @return the parent node, or {@link Optional#empty()} if none
     */
    public Optional<TreeNode<T>> getParent() {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(parent);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns an unmodifiable view of the node’s property map.
     * <p>
     * Properties can be used to store arbitrary metadata associated with this node.
     * </p>
     *
     * @return an unmodifiable map of properties
     */
    public Map<String, Object> getProperties() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableMap(properties);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns an unmodifiable snapshot of this node’s direct children.
     *
     * @return an unmodifiable list of direct child nodes
     */
    public List<TreeNode<T>> children() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(children));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Adds a child node to this node.
     * <p>
     * If the specified child already has a parent, it is detached first.
     * </p>
     *
     * @param child the child node to add (must not be {@code null})
     * @throws NullPointerException if {@code child} is {@code null}
     */
    public void add(TreeNode<T> child) {
        Objects.requireNonNull(child, "child cannot be null");
        lock.writeLock().lock();
        try {
            child.detach();
            child.parent = this;
            children.add(child);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes a specific child from this node.
     * <p>
     * If the child is successfully removed, its parent reference becomes {@code null}.
     * </p>
     *
     * @param child the child node to remove
     */
    public void remove(TreeNode<T> child) {
        lock.writeLock().lock();
        try {
            if (children.remove(child)) {
                child.parent = null;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns the root node of this tree (traversing up until there is no parent).
     *
     * @return the top-most ancestor of this node
     */
    public TreeNode<T> getRoot() {
        TreeNode<T> node = this;
        while (node.parent != null) {
            node = node.parent;
        }
        return node;
    }

    /**
     * Checks whether this node has any children.
     *
     * @return {@code true} if this node has at least one child, otherwise {@code false}
     */
    public boolean hasChildren() {
        lock.readLock().lock();
        try {
            return !children.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    private void detach() {
        lock.writeLock().lock();
        try {
            if (parent != null) {
                parent.children.remove(this);
                parent = null;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    // ---- Traversal --------------------------------------------------------

    /**
     * Returns a sequential {@link Stream} over all nodes reachable from this one (including itself).
     * <p>
     * This stream is non-recursive and traverses using the {@link Iterable} implementation.
     * </p>
     *
     * @return a sequential stream of {@code TreeNode<T>} elements
     */
    public Stream<TreeNode<T>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns a recursive depth-first stream of this node and all its descendants.
     * <p>
     * The traversal order is pre-order (parent before children).
     * </p>
     *
     * @return a {@link Stream} containing this node and all descendants
     */
    public Stream<TreeNode<T>> walk() {
        return Stream.concat(
                Stream.of(this),
                children.stream().flatMap(TreeNode::walk)
        );
    }

    /**
     * Performs a parallel traversal over this node and all its descendants,
     * executing the provided action for each node using the {@link ForkJoinPool#commonPool()}.
     *
     * @param action the operation to perform on each node
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public void parallelWalk(Consumer<TreeNode<T>> action) {
        Objects.requireNonNull(action, "action");
        lock.readLock().lock();
        try {
            ForkJoinPool.commonPool().invoke(new WalkTask(this, action));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds the first node in the subtree (including this one) that matches the given predicate.
     *
     * @param predicate the predicate to test each node
     * @return an {@link Optional} containing the first matching node, or empty if none
     */
    public Optional<TreeNode<T>> find(Predicate<TreeNode<T>> predicate) {
        return walk().filter(predicate).findFirst();
    }

    private class WalkTask extends RecursiveAction {
        private final TreeNode<T> node;
        private final Consumer<TreeNode<T>> action;

        WalkTask(TreeNode<T> node, Consumer<TreeNode<T>> action) {
            this.node = node;
            this.action = action;
        }

        @Override
        protected void compute() {
            action.accept(node);
            List<WalkTask> subTasks = new ArrayList<>();
            lock.readLock().lock();
            try {
                for (TreeNode<T> child : node.children) {
                    subTasks.add(new WalkTask(child, action));
                }
            } finally {
                lock.readLock().unlock();
            }
            invokeAll(subTasks);
        }
    }

    /**
     * Counts the total number of nodes in this subtree (including this node).
     *
     * @return the total node count
     */
    public int countAllNodes() {
        return (int) walk().count();
    }

    // ---- Pretty printing --------------------------------------------------
    /**
     * Returns a human-readable ASCII representation of this tree,
     * where each level is indented with lines and branches.
     * <p>
     * Example:
     * </p>
     * <pre>
     * root
     * ├─ child1
     * │  └─ subchild
     * └─ child2
     * </pre>
     *
     * @return a formatted tree string
     */
    public String toTreeString() {
        return toTreeString(node -> String.valueOf(node.value));
    }
    /**
     * Returns a formatted tree string using a custom rendering function for each node.
     *
     * @param render a function that converts a node to its textual representation
     * @return a formatted string representing the tree
     */
    public String toTreeString(Function<TreeNode<T>, String> render) {
        StringBuilder sb = new StringBuilder();
        sb.append(render.apply(this)).append(System.lineSeparator());
        buildTreeString(this, "", true, render, sb);
        return sb.toString();
    }

    private void buildTreeString(TreeNode<T> node, String prefix, boolean isTail,
                                 Function<TreeNode<T>, String> render, StringBuilder sb) {
        for (Iterator<TreeNode<T>> it = node.children.iterator(); it.hasNext(); ) {
            TreeNode<T> child = it.next();
            boolean last = !it.hasNext();
            sb.append(prefix)
                    .append(last ? "└─ " : "├─ ")
                    .append(render.apply(child))
                    .append(System.lineSeparator());
            if (child.hasChildren()) {
                buildTreeString(child, prefix + (last ? "   " : "│  "), last, render, sb);
            }
        }
    }

    // ---- Iterable ---------------------------------------------------------
    /**
     * Returns an iterator over this node and all its descendants (depth-first order).
     *
     * @return an iterator traversing this node’s subtree
     */
    @Override
    public Iterator<TreeNode<T>> iterator() {
        return walk().iterator();
    }

    // ---- Object overrides -------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TreeNode)) return false;
        TreeNode<?> other = (TreeNode<?>) o;
        return Objects.equals(value, other.value)
                && Objects.equals(properties, other.properties)
                && Objects.equals(children, other.children);
    }


    @Override
    public int hashCode() {
        return Objects.hash(value, properties, children);
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "value=" + value +
                ", properties=" + properties +
                ", children=" + children.size() +
                '}';
    }
}
