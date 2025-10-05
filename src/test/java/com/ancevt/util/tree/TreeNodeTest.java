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

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TreeNodeTest {

    @Test
    void testAddAndChildren() {
        TreeNode<String> root = TreeNode.of("root");
        TreeNode<String> child = TreeNode.of("child");

        root.add(child);

        assertTrue(root.hasChildren());
        assertEquals(1, root.children().size());
        assertEquals("child", root.children().get(0).getValue());
        assertEquals(root, child.getParent().orElseThrow(RuntimeException::new));
    }

    @Test
    void testRemove() {
        TreeNode<String> root = TreeNode.of("root");
        TreeNode<String> child = TreeNode.of("child");

        root.add(child);
        root.remove(child);

        assertFalse(root.hasChildren());
        assertFalse(child.getParent().isPresent());
    }

    @Test
    void testDetachAndReattach() {
        TreeNode<String> a = TreeNode.of("a");
        TreeNode<String> b = TreeNode.of("b");
        TreeNode<String> c = TreeNode.of("c");

        a.add(b);
        b.add(c);
        a.remove(b);

        assertFalse(a.hasChildren());
        assertFalse(b.getParent().isPresent());

        c.getRoot(); // should not blow up
        assertEquals(b, c.getRoot());
    }

    @Test
    void testWalkCollectsAllNodes() {
        TreeNode<Integer> root = TreeNode.of(1);
        TreeNode<Integer> a = TreeNode.of(2);
        TreeNode<Integer> b = TreeNode.of(3);
        root.add(a);
        root.add(b);
        a.add(TreeNode.of(4));

        List<Integer> values = root.walk().map(TreeNode::getValue).collect(Collectors.toList());

        assertEquals(Arrays.asList(1, 2, 4, 3), values);
    }

    @Test
    void testParallelWalkExecutesAction() {
        TreeNode<Integer> root = TreeNode.of(1);
        for (int i = 2; i <= 8; i++) root.add(TreeNode.of(i));

        AtomicInteger counter = new AtomicInteger();
        root.parallelWalk(node -> counter.incrementAndGet());

        assertEquals(8, counter.get()); // root + 7 children
    }

    @Test
    void testFindWorks() {
        TreeNode<String> root = TreeNode.of("root");
        TreeNode<String> api = TreeNode.of("api");
        TreeNode<String> user = TreeNode.of("user");
        api.add(user);
        root.add(api);

        Optional<TreeNode<String>> found = root.find(n -> "user".equals(n.getValue()));
        assertTrue(found.isPresent());
        assertEquals("user", found.get().getValue());
    }

    @Test
    void testCountAllNodes() {
        TreeNode<Integer> root = TreeNode.of(0);
        for (int i = 1; i <= 3; i++) {
            TreeNode<Integer> child = TreeNode.of(i);
            child.add(TreeNode.of(i * 10));
            root.add(child);
        }
        assertEquals(7, root.countAllNodes());
    }

    @Test
    void testToTreeStringProducesHierarchy() {
        TreeNode<String> root = TreeNode.of("root");
        TreeNode<String> a = TreeNode.of("a");
        TreeNode<String> b = TreeNode.of("b");
        root.add(a);
        root.add(b);
        a.add(TreeNode.of("a1"));

        String s = root.toTreeString();
        assertTrue(s.contains("root"));
        assertTrue(s.contains("└─") || s.contains("├─"));
        assertTrue(s.contains("a1"));
    }

    @Test
    void testEqualsAndHashCode() {
        TreeNode<String> t1 = TreeNode.of("x");
        TreeNode<String> t2 = TreeNode.of("x");
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void testPropertiesUnmodifiable() {
        TreeNode<String> n = TreeNode.of("p");
        Map<String, Object> props = n.getProperties();
        assertThrows(UnsupportedOperationException.class, () -> props.put("a", 1));
    }

    @Test
    void testThreadSafetyBasics() throws InterruptedException {
        TreeNode<Integer> root = TreeNode.of(1);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int val = i;
            threads.add(new Thread(() -> root.add(TreeNode.of(val))));
        }
        threads.forEach(Thread::start);
        for (Thread t : threads) t.join();

        assertEquals(10, root.children().size());
    }
}
