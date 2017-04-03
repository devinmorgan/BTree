import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by devinmorgan on 2/18/17.
 */
public class BTreeTest {
//    @Test
//    public void add() throws Exception {
//
//        // add elements in ascending order
//        BTree t1 = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        t1.add(-2);
//        t1.add(-1);
//        t1.add(0);
//        t1.add(1);
//        t1.add(2);
//        assertEquals(t1.toString(), "|-1,|\n|-2,||0,1,2,|\n");
//
//        // add elements in descending order
//        BTree t2 = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        t2.add(2);
//        t2.add(1);
//        t2.add(0);
//        t2.add(-1);
//        t2.add(-2);
//        assertEquals(t2.toString(), "|1,|\n|-2,-1,0,||2,|\n");
//
//        // add elements in mixed order
//        BTree t3 = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        t3.add(0);
//        t3.add(-1);
//        t3.add(1);
//        t3.add(-2);
//        t3.add(2);
//        assertEquals(t3.toString(), "|0,|\n|-2,-1,||1,2,|\n");
//
//        // add duplicate elements
//        BTree t4 = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        t4.add(0);
//        t4.add(-1);
//        t4.add(1);
//        t4.add(-2);
//        t4.add(2);
//        t4.add(0);
//        t4.add(-1);
//        t4.add(1);
//        t4.add(-2);
//        t4.add(2);
//        assertEquals(t3.toString(), "|0,|\n|-2,-1,||1,2,|\n");
//    }
//
//    @Test
//    public void addAll() throws Exception {
//        // add elements in ascending order
//        BTree t1 = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        ArrayList<Integer> l1 = new ArrayList<Integer>();
//        l1.add(-2);
//        l1.add(-1);
//        l1.add(0);
//        l1.add(1);
//        l1.add(2);
//        t1.addAll(l1);
//        assertEquals(t1.toString(), "|-1,|\n|-2,||0,1,2,|\n");
//
//        // add elements in descending order
//        BTree t2 = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        ArrayList<Integer> l2 = new ArrayList<Integer>();
//        l2.add(2);
//        l2.add(1);
//        l2.add(0);
//        l2.add(-1);
//        l2.add(-2);
//        t2.addAll(l2);
//        assertEquals(t2.toString(), "|1,|\n|-2,-1,0,||2,|\n");
//
//        // add elements in mixed order
//        BTree t3 = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        ArrayList<Integer> l3 = new ArrayList<Integer>();
//        l3.add(0);
//        l3.add(-1);
//        l3.add(1);
//        l3.add(-2);
//        l3.add(2);
//        t3.addAll(l3);
//        assertEquals(t3.toString(), "|0,|\n|-2,-1,||1,2,|\n");
//    }
//
//    @Test
//    public void remove() throws Exception {
//        BTree t1 = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        t1.add(-2);
//        t1.add(-1);
//        t1.add(0);
//        t1.add(1);
//        t1.add(-3);
//
//        // case 2.a), y has >= t keys
//        t1.remove(-1);
//        assertEquals(t1.toString(), "|-2,|\n|-3,||0,1,|\n");
//
//        // case 2.b), z has >= t keys, and y has < t keys
//        t1.remove(-2);
//        assertEquals(t1.toString(), "|0,|\n|-3,||1,|\n");
//
//        // case 2.c) both y and z have < t keys
//        t1.remove(0);
//        assertEquals(t1.toString(), "|-3,1,|\n");
//
//        // case 1) x is a leaf node and contains the key to be deleted
//        t1.remove(-3);
//        assertEquals(t1.toString(), "|1,|\n");
//
//        // refreshing the list
//        t1.add(-2);
//        t1.add(-1);
//        t1.add(0);
//        t1.add(1);
//
//        // case 3.a.i) left child contains key and has t-1 keys. Right child has >=t keys
//        t1.remove(-2);
//        assertEquals(t1.toString(), "|0,|\n|-1,||1,|\n");
//
//        // case 3.a.i) right child contains key and has t-1 keys. Left child has >=t keys
//        t1.add(-2);
//        t1.remove(1);
//        assertEquals(t1.toString(), "|-1,|\n|-2,||0,|\n");
//
//        // create a new tree
//        BTree t2 = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        t2.add(0);
//        t2.add(10);
//        t2.add(15);
//        t2.add(-5);
//        t2.add(14);
//        t2.add(13);
//        t2.add(11);
//        t2.remove(11);
//        t2.remove(0);
//
//        // case 3.b) middle child contains key and left, middle, and right children have t-1 keys
//        t2.remove(13);
//        assertEquals(t2.toString(), "|10,|\n|-5,||14,15,|\n");
//    }
//
//    @Test
//    public void removeAll() throws Exception {
//        // all the elements to remove are in the B-tree
//        BTree t1 = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        ArrayList<Integer> l1 = new ArrayList<Integer>();
//        l1.add(-2);
//        l1.add(-1);
//        l1.add(0);
//        l1.add(1);
//        l1.add(2);
//        t1.addAll(l1);
//        l1.remove(new Integer(0));
//        t1.removeAll(l1);
//        assertEquals(t1.toString(), "|0,|\n");
//
//        // add some arbitrary elements to l1
//        l1.add(6);
//        l1.add(-3);
//
//        // subset of the elements to remove are in the B-tree
//        BTree t2 = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        ArrayList<Integer> l2 = new ArrayList<Integer>();
//        l2.add(-1);
//        l2.add(0);
//        l2.add(1);
//        l2.add(2);
//        l2.add(7);
//        t2.addAll(l2);
//        t2.removeAll(l1);
//        assertEquals(t2.toString(), "|0,7,|\n");
//
//        // give l2 some new values
//        l2.clear();
//        l2.add(3);
//        l2.add(4);
//        l2.add(5);
//
//        // none of the elements to be removed are in the B-tree
//        BTree t3 = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        ArrayList<Integer> l3 = new ArrayList<Integer>();
//        l3.add(0);
//        l3.add(-1);
//        l3.add(1);
//        l3.add(-2);
//        l3.add(2);
//        t3.addAll(l3);
//        t3.removeAll(l2);
//        assertEquals(t3.toString(), "|0,|\n|-2,-1,||1,2,|\n");
//    }
//
//    @Test
//    public void contains() throws Exception {
//        // tree contains the key
//        BTree t1 = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        t1.add(-10);
//        t1.add(-5);
//        t1.add(0);
//        t1.add(5);
//        t1.add(10);
//        assertTrue(t1.contains(-10));
//        assertTrue(t1.contains(-5));
//        assertTrue(t1.contains(0));
//        assertTrue(t1.contains(5));
//        assertTrue(t1.contains(10));
//
//        // tree does not contain the key
//        assertFalse(t1.contains(-7));
//        assertFalse(t1.contains(-3));
//        assertFalse(t1.contains(3));
//        assertFalse(t1.contains(7));
//    }
//
//    @Test
//    public void containsAll() throws Exception {
//        BTree t = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        t.add(-5);
//        t.add(0);
//        t.add(5);
//
//        // list is a superset of t
//        ArrayList<Integer> l = new ArrayList<Integer>();
//        l.add(-6);
//        l.add(-5);
//        l.add(0);
//        l.add(5);
//        assertFalse(t.containsAll(l));
//        l.clear();
//
//        // list overlaps with t but also contains different elements
//        l.add(-5);
//        l.add(0);
//        l.add(7);
//        assertFalse(t.containsAll(l));
//        l.clear();
//
//        // list is a subset of t
//        l.add(-5);
//        l.add(0);
//        assertTrue(t.containsAll(l));
//
//        // list is the same set as t
//        l.add(5);
//        assertTrue(t.containsAll(l));
//        l.clear();
//
//        // list is disjoint from t
//        l.add(7);
//        l.add(9);
//        assertFalse(t.containsAll(l));
//        l.clear();
//
//        // l5 is the empty set
//        assertTrue(t.containsAll(l));
//    }
//
//    @Test
//    public void size() throws Exception {
//        BTree t = BTree.createNewBTreeWithMinNumberOfChildren(2);
//
//        // before adding any elements
//        assertEquals(t.size(), 0);
//
//        // after adding some elements
//        t.add(1);
//        t.add(-1);
//        assertEquals(t.size(), 2);
//
//        // after adding some elements
//        ArrayList<Integer> l = new ArrayList<Integer>();
//        l.add(0);
//        l.add(2);
//        l.add(-3);
//        t.addAll(l);
//        assertEquals(t.size(), 5);
//
//        // after removing some elements
//        t.remove(0);
//        t.remove(-1);
//        assertEquals(t.size(), 3);
//
//        // after removing some elements
//        l.clear();
//        l.add(1);
//        l.add(2);
//        l.add(18);
//        t.removeAll(l);
//        assertEquals(t.size(), 1);
//
//        // after adding back some elements
//        l.clear();
//        l.add(8);
//        l.add(-8);
//        t.addAll(l);
//        assertEquals(t.size(), 3);
//
//        // after adding back some elements
//        t.add(9);
//        assertEquals(t.size(), 4);
//
//        // after removing some elements
//        t.remove(8);
//        assertEquals(t.size(), 3);
//
//        // after removing the rest of the elements
//        l.clear();
//        l.add(-3);
//        l.add(-8);
//        l.add(9);
//        t.removeAll(l);
//        assertEquals(t.size(), 0);
//    }
//
//    @Test
//    public void isEmpty() throws Exception {
//        BTree t = BTree.createNewBTreeWithMinNumberOfChildren(2);
//
//        // before adding any elements
//        assertTrue(t.isEmpty());
//
//        // after adding some elements
//        t.add(1);
//        t.add(-1);
//        assertFalse(t.isEmpty());
//
//        // after adding some elements
//        ArrayList<Integer> l = new ArrayList<Integer>();
//        l.add(0);
//        l.add(2);
//        l.add(-3);
//        t.addAll(l);
//        assertFalse(t.isEmpty());
//
//        // after removing some elements
//        t.remove(0);
//        t.remove(-1);
//        assertFalse(t.isEmpty());
//
//        // after removing some elements
//        l.clear();
//        l.add(1);
//        l.add(2);
//        l.add(18);
//        t.removeAll(l);
//        assertFalse(t.isEmpty());
//
//        // after adding back some elements
//        l.clear();
//        l.add(8);
//        l.add(-8);
//        t.addAll(l);
//        assertFalse(t.isEmpty());
//
//        // after adding back some elements
//        t.add(9);
//        assertFalse(t.isEmpty());
//
//        // after removing some elements
//        t.remove(8);
//        assertFalse(t.isEmpty());
//
//        // after removing the rest of the elements
//        l.clear();
//        l.add(-3);
//        l.add(-8);
//        l.add(9);
//        t.removeAll(l);
//        assertTrue(t.isEmpty());
//    }
//
//    @Test
//    public void clear() throws Exception {
//        BTree t = BTree.createNewBTreeWithMinNumberOfChildren(2);
//        List<Integer> l = Arrays.asList(new Integer[] {0,1,2});
//
//        // 0 levels of node (0 keys)
//        t.clear();
//        assertEquals(t.size(), 0);
//        assertEquals(t.toString(), "||\n");
//
//        // 1 level of nodes
//        t.addAll(l);
//        t.clear();
//        assertEquals(t.size(), 0);
//        assertEquals(t.toString(), "||\n");
//
//        // 2 levels of nodes
//        List<Integer> l2 = Arrays.asList(new Integer[] {-1,-2,-3,-4});
//        t.addAll(l2);
//        t.clear();
//        assertEquals(t.size(), 0);
//        assertEquals(t.toString(), "||\n");
//
//        // 2+ levels of nodes
//        ArrayList<Integer> l3 = new ArrayList<Integer>();
//        for (int i = -1; i < 100; i+=7)
//            l3.add(i);
//        t.addAll(l3);
//        t.clear();
//        assertEquals(t.size(), 0);
//        assertEquals(t.toString(), "||\n");
//    }
//
//    @Test
//    public void first() throws Exception {
//        BTree t = BTree.createNewBTreeWithMinNumberOfChildren(2);
//
//        // 0 keys
//        boolean threwException = false;
//        try {
//            Integer first = t.first();
//        } catch (NoSuchElementException e) {
//            threwException = true;
//        }
//        assertTrue(threwException);
//
//        // 1 level of nodes
//        for (int i = -5; i <5; i+=7)
//            t.add(i);
//        assertEquals(t.first(), new Integer(-5));
//
//        // 2 levels of nodes
//        for (int i = 6; i <27; i+=5)
//            t.add(i);
//        assertEquals(t.first(), new Integer(-5));
//
//        // 2+ level of nodes
//        for (int i = -50; i < 100; i+=3)
//            t.add(i);
//        assertEquals(t.first(), new Integer(-50));
//    }
//
//    @Test
//    public void last() throws Exception {
//        BTree t = BTree.createNewBTreeWithMinNumberOfChildren(2);
//
//        // 0 keys
//        boolean threwException = false;
//        try {
//            Integer last = t.last();
//        } catch (NoSuchElementException e) {
//            threwException = true;
//        }
//        assertTrue(threwException);
//
//        // 1 level of nodes
//        for (int i = -5; i <5; i+=7)
//            t.add(i);
//        assertEquals(t.last(), new Integer(2));
//
//        // 2 levels of nodes
//        for (int i = 6; i <27; i+=5)
//            t.add(i);
//        assertEquals(t.last(), new Integer(26));
//
//        // 2+ level of nodes
//        for (int i = -50; i < 100; i+=3)
//            t.add(i);
//        assertEquals(t.last(), new Integer(97));
//    }
//
//    @Test
//    public void iterator() {
//        BTree t = BTree.createNewBTreeWithMinNumberOfChildren(2);
//
//        // 0 keys
//        for (Integer i : t)
//            assertFalse("Should never hit this statement when BTree has no keys", true);
//
//        // 1 level of nodes
//        t.addAll(Arrays.asList(new Integer[] {2,0,1}));
//        int key1 = 0;
//        for (Integer i : t)
//            assertEquals(new Integer(i), new Integer(key1++));
//
//        // 2 levels of nodes
//        for (int i = 6; i < 8; i++) {
//            t.add(i);
//        }
//        for (int i = 3; i < 6; i++) {
//            t.add(i);
//        }
//
//        int key2 = 0;
//        Iterator<Integer> iterator = new BTreeIterator(t);
//        while (iterator.hasNext()) {
//            Integer integer = iterator.next();
//            assertEquals(integer, new Integer(key2++));
//        }
//        // 2+ levels of nodes
//        Node contains4and5 = null;
//        for (int i = -1; i >= -5; i--)
//            t.add(i);
//        int key3 = -5;
//        for (Integer i : t)
//            assertEquals(new Integer(i), new Integer(key3++));
//        t.clear();
//
//        // a lot of random values against a sorted comparison
//        ArrayList<Integer> comparison = new ArrayList<Integer>();
//        for (int i = 0; i < 100; i++) {
//            Random r = new Random();
//            Integer value = r.nextInt(100) - 50;
//
//            // add values to B-tree
//            t.add(value);
//
//            // only add unique values to list
//            if (!comparison.contains(value)) {
//                comparison.add(value);
//            }
//        }
//        Collections.sort(comparison);
//
//        int index = 0;
//        BTreeIterator iteratorNew = new BTreeIterator(t);
//        while (iteratorNew.hasNext()) {
//            Integer actual = iteratorNew.next();
//            Integer expected = comparison.get(index++);
//            assertEquals(expected, actual);
//        }
//    }

    @Test
    public void testMaxCapacity() {
        int capcity = 10000000;
//        BTree t = BTree.createNewBTreeWithMinNumberOfChildren(capcity);
//        int i = 0;
//        for (; i < capcity/1; i++)
//            t.add(i);
//        System.out.println(t.toString());
        TreeSet<Integer> a = new TreeSet<Integer>();
        Random r = new Random();
        for (int i = 0; i < capcity; i++) {
            a.add(r.nextInt(i + 1 ));
//            a.contains(i/2);
        }
    }

}