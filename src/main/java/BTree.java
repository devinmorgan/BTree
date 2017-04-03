import java.util.*;

/**
 * Created by devinmorgan on 2/9/17.
 */
public class BTree implements SortedSet<Integer>  {
    /*
        Indicates the minimum number of child nodes that
        an internal node can have (excluding the root node).
        There are always minNumberOfChildren - 1 keys in a node
        and a node can have at most 2*minNumberOfChildren - 1
        child nodes before need to split up
     */
    private int minNumberOfChildren;

    /*
        The root node of the B-tree. It can change
        to be a different node during splits and merges
     */
    Node root;

    /*
        Represents the total number of keys in the B-tree
        at any given time.
     */
    private int elementsCount = 0;

    /*-------------------------------BTree Methods-------------------------------*/

    /*
        Basic constructor for B-tree
     */
    private BTree(int minNumberOfChildren) {
        this.minNumberOfChildren = minNumberOfChildren;

        // create an empty root node
        this.root = Node.allocateNode(this.minNumberOfChildren, null);
        this.root.isLeaf = true;

        // write this new node to disk
        Node.writeNodeToDisk(this.root);
    }

    /*
        The public factory method for the B-tree class.
     */
    public static BTree createNewBTree() {
        return BTree.createNewBTreeWithMinNumberOfChildren(1000);
    }

    /*
        The "private" factory method for the B-tree class.
        This method allows you to determine the minimum number
        of children each node in the B-tree can have (and is
        useful for testing)
     */
    static BTree createNewBTreeWithMinNumberOfChildren(int minNumberOfChildren) {
        return new BTree(minNumberOfChildren);
    }

    /*
        Searches the B-tree for the key with value
        key, starting at the node, root. If the key
        is found in the B-tree (i.e. it exists in
        the B-tree), then a NodeIndexPair containing
        the node and the key index, the index in the
         node where key was found, is returned.
     */
    NodeIndexPair search(Node root, int key) {

        // get the index of the first key in root that is larger than key
        int i = 0;
        while (i < root.keys.size() && key > root.keys.get(i))
            i++;

        // if key is actually one of root's keys, then we found it
        if (i < root.keys.size() && key == root.keys.get(i))
            return new NodeIndexPair(root, i);

        // if we didn't find the key and this node
        // is a leaf, then key is not in the B-tree
        else if (root.isLeaf)
            return null;

        // if this node is not a leaf, recurse on
        // the subtree that would contain key
        else {
            String pointer = root.childPointers.get(i);
            Node child = Node.readNodeFromDisk(pointer);
            return search(child, key);
        }
    }

    /*
        Performs the split operation as described in CLRS
        Chapter 18, B-Trees, on page 494. This function is
        used during inserts to ensure that the each node
        always has between minNumberOfChildren - 1 and
        2*minNumberOfChildren -1 keys at any time (excluding
        the root node)
     */
    private void splitChild(Node parent, int index, Node fullNode) {
        // create a new sibling to populate
        Node newSibling = Node.allocateNode(minNumberOfChildren, parent.getPointer());

        // the newSibling is a leaf iff the oldSibling is a leaf
        newSibling.isLeaf = fullNode.isLeaf;

        for (int j = minNumberOfChildren; j < 2* minNumberOfChildren - 1; j++) {
            // remove the upper half of keys from
            // fullNode and add them to newSibling
            Integer val = fullNode.keys.remove(minNumberOfChildren);
            newSibling.keys.add(val);

            // remove the upper half of pointers from
            // fullNode and add them to newSibling
            String pointer = fullNode.childPointers.remove(minNumberOfChildren);
            newSibling.childPointers.add(pointer);
        }

        // there is always 1 more pointer than key
        if (fullNode.isLeaf)
            newSibling.childPointers.add(null);
        else
            newSibling.childPointers.add(fullNode.childPointers.remove(minNumberOfChildren));

        // insert the pointer to newSibling at index + 1 in parent's childPointers
        parent.childPointers.add(index + 1, newSibling.getPointer());

        // remove the median key value from oldSibling and insert it at index in parent's keys
        parent.keys.add(index, fullNode.keys.remove(minNumberOfChildren - 1));

        // set the parent pointer of the child nodes of newSibling
        // to be newSibling and save these changes to disk
        if (! newSibling.isLeaf) {
            for (String pointer : newSibling.childPointers) {
                Node child = Node.readNodeFromDisk(pointer);
                child.parentPointer = newSibling.getPointer();
                Node.writeNodeToDisk(child);
            }
        }

        // write the changes made to newSibling, oldSibling, and parent to disk
        Node.writeNodeToDisk(newSibling);
        Node.writeNodeToDisk(fullNode);
        Node.writeNodeToDisk(parent);
    }

    /*
        Inserts a key into the B-tree according to CLRS
        Chapter 18, B-Trees, on page 495
     */
    private void insert(int key) {
        Node root = this.root;

        // split the tree's root if it is full
        if (root.keys.size() == 2* this.minNumberOfChildren - 1) {

            // initialize a new root for the B-tree
            Node newRoot = Node.allocateNode(this.minNumberOfChildren, null);
            newRoot.isLeaf = false;
            newRoot.childPointers.add(root.getPointer());
            root.parentPointer = newRoot.getPointer();

            // set newRoot as the new root and split the old root
            this.root = newRoot;
            this.splitChild(newRoot, 0, root);

            // continue the search in the non-full root
            this.insertNonFull(newRoot, key);
            int jkk = 7;
        }
        else
            this.insertNonFull(root, key);
    }

    /*
        Inserts a key into the B-tree according to CLRS
        Chapter 18, B-Trees, on page 496
     */
    private void insertNonFull(Node node, int key) {
        if (node.isLeaf) {
            // to insert the key at the correct index in node.keys
            node.keys.add(key);
            Collections.sort(node.keys);

            // ensure that the leaf node has the correct number of pointers
            node.childPointers.clear();
            for (int i = 0; i < node.keys.size() + 1; i++)
                node.childPointers.add(null);

            // save the updated leaf node
            Node.writeNodeToDisk(node);

            // increment the elements count
            this.elementsCount++;
        }
        else {
            // find the index at which the key should be inserted
            int i = node.keys.size() - 1;
            while (i >= 0 && key < node.keys.get(i))
                i--;
            i++;

            // split the eligible next node if it is full
            Node nextNode = Node.readNodeFromDisk(node.childPointers.get(i));
            if (nextNode.keys.size() == 2* minNumberOfChildren - 1) {
                splitChild(node, i, nextNode);

                // check to see if i is still the appropriate index
                // after node gets modified from split()
                if (key > node.keys.get(i)) {
                    nextNode = Node.readNodeFromDisk(node.childPointers.get(i+1));
                }
            }

            // continue the insertion; we only insert keys into leaves
            insertNonFull(nextNode, key);
        }
    }

    /*
        Deletes a key from the B-tree according to CLRS
        Chapter 18, B-Tress, on Pages 499-502
     */
    private boolean delete(Node node, int key) {
        // case 0
        if (node.isLeaf && ! node.keys.contains(new Integer(key)))
            return false;

        // case 1
        else if (node.isLeaf && node.keys.contains(new Integer(key))) {

            // remove the key and null pointer
            node.keys.remove(new Integer(key));
            node.childPointers.remove(null);

            // write the changes to node on disk
            Node.writeNodeToDisk(node);
            this.elementsCount--;
            return true;
        }

        // case 2
        else if (! node.isLeaf && node.keys.contains(new Integer(key))) {
            int i = node.keys.size() - 1;
            while (i >= 0 && key != node.keys.get(i))
                i--;

            // case a
            Node precedingChild = Node.readNodeFromDisk(node.childPointers.get(i));
            if (precedingChild.keys.size() >= minNumberOfChildren) {

                // replace key with next smallest key from precedingChild
                int nextLargestKey = precedingChild.removeLargestKeyFromSubtree();
                node.keys.set(i, nextLargestKey);

                // save the changes to node and preceding child to disk----------------------------
                Node.writeNodeToDisk(precedingChild);
                this.elementsCount--;
                return true;
            }

            // case b
            Node succeedingChild = Node.readNodeFromDisk(node.childPointers.get(i+1));
            if (succeedingChild.keys.size() >= minNumberOfChildren) {

                // replace key with next largest key from succeedingChild
                int nextSmallestKey = succeedingChild.removeSmallestKeyFromSubtree();
                node.keys.set(i, nextSmallestKey);

                // write the changes to succeedingChild on disk
                Node.writeNodeToDisk(succeedingChild);
                this.elementsCount--;
                return true;
            }

            // case c
            // remove key and pointer to succeedingChild from node
            node.keys.remove(i);
            node.childPointers.remove(i+1);

            // merge succeedingChild and key into precedingChild
            Node containingKeyNode = Node.merge(precedingChild, key, succeedingChild, node);

            // if the node was the root and is now empty, set
            // the B-tree's new root to be containingKeyNode
            if (this.root == node && node.keys.isEmpty())
                this.root = containingKeyNode;

            // delete key from newly merged precedingChild
            return this.delete(containingKeyNode, key);
        }

        // case 3
        else {

            // determine the index of the child node that
            // would contain key, if it exits in the B-tree
            int i = node.keys.size() - 1;
            while (i >= 0 && key < node.keys.get(i))
                i--;
            i++;

            // case a | case b
            Node containingKeyNode = Node.readNodeFromDisk(node.childPointers.get(i));
            if (containingKeyNode.keys.size() == minNumberOfChildren - 1) {
                containingKeyNode = containingKeyNode.grabElementFromOrMergeWithSiblingNode(node, i);

                // if the node was the root and is now empty, set
                // the B-tree's new root to be containingKeyNode
                if (this.root == node && node.keys.isEmpty())
                    this.root = containingKeyNode;
            }

            // recursively delete key
            return delete(containingKeyNode, key);
        }
    }

    /*
        Find the node in the B-tree that contains the
        smallest key values by repeatedly traversing
        the left-most child node
     */
    Node getSmallestNode() {
        Node node = this.root;
        while (! node.isLeaf)
            node = Node.readNodeFromDisk(node.childPointers.get(0));
        return node;
    }

    /*
        Does an in-order traversal of the B-Tree starting
        at the first key with value greater than or equal
        to from and continuing until finding a value greater
        than or equal to until
     */
    private BTree walkOverTree(Integer from, Integer until) {
        BTree sortedSubset = BTree.createNewBTreeWithMinNumberOfChildren(this.minNumberOfChildren);
        BTreeIterator iterator = new BTreeIterator(this, from);
        while (iterator.hasNext()) {
            Integer i = iterator.next();
            if (i >= until)
                return sortedSubset;
            sortedSubset.add(i);
        }
        return sortedSubset;
    }

    /*-------------------------------SortedSet Methods----------------------------*/

    /*
        Adds an integer to the B-Tree as a new key
        value if the key does not already exist in
        the tree. Returns true if the integer was
        successfully inserted into the B-tree and
        false otherwise
     */
    public boolean add(Integer integer) {
        if (this.contains(integer))
            return false;
        this.insert(integer);
        return true;
    }

    /*
        Adds all of the integer objects within the collection
        that do not already existing within the B-tree. Returns
        true if at least one object was successfully added to
        the B-tree
     */
    public boolean addAll(Collection<? extends Integer> c)  {
        boolean hasBeenModified = false;
        for (Object o : c) {
            if (o instanceof Integer)
                hasBeenModified = this.add((Integer) o) || hasBeenModified;
        }
        return hasBeenModified;
    }

    /*
        Removes the object from the B-tree if the object is
        an Integer that exists within the B-tree. Returns
        true if the object was successfully removed and
        false otherwise.
     */
    public boolean remove(Object o) {
        if (o instanceof Integer) {
            Integer i = (Integer) o;
            return this.delete(this.root, i);
        }
        return false;
    }

    /*
        Removes all of the objects in the collection that
        are Integers and that exist within the B-tree. Returns
        true if at least one value was removed and false otherwise.
     */
    public boolean removeAll(Collection<?> c) {
        boolean hasBeenModified = false;
        for (Object o : c)
            hasBeenModified = this.remove(o) || hasBeenModified;
        return hasBeenModified;
    }

    /*
        Returns true if the object is an integer whose value
        is a key in the B-tree. Returns false otherwise.
     */
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            Integer i = (Integer) o;
            return this.search(this.root, i) != null;
        }
        return false;
    }

    /*
        Returns true if and only iff all of the values in the
        collection are Integers and if and only if each of
        their values is a key in the B-tree. Returns false otherwise.
     */
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (! this.contains(o))
                return false;
        }
        return true;
    }

    /*
        Returns an int equivalent to the number of keys currently
        stored in the B-tree
     */
    public int size() {
        return this.elementsCount;
    }

    /*
        Returns true if there are no keys in the B-tree and
        false if there is at least one key in the B-tree
     */
    public boolean isEmpty() {
        return this.root.keys.isEmpty();
    }

    /*
        Removes all of the keys in the B-tree and all of the
        data associated with them. The B-tree is essentially
        mutated to become an empty B-tree again.
     */
    public void clear() {
        // delete all data for each node in the root's subtree
        Node.deleteSubtree(this.root);

        // initialize an empty root node for the B-tree
        Node newRoot = Node.allocateNode(minNumberOfChildren, null);
        newRoot.isLeaf = true;
        this.root = newRoot;
        this.elementsCount = 0;

        // write this new node to disk
        Node.writeNodeToDisk(newRoot);
    }

    /*
        Returns the value of the smallest key inside the
        B-tree. If the tree is empty, then this method
        throws a NoSuchElementException.
     */
    public Integer first() throws NoSuchElementException {
        // handle the empty B-tree case
        if (this.elementsCount == 0)
            throw new NoSuchElementException();

        // for all trees with >= 1 elements
        Node node = this.root;
        while (! node.isLeaf)
            node = Node.readNodeFromDisk(node.childPointers.get(0));
        return node.keys.get(0);
    }

    /*
        Returns the value of the laregest key inside the
        B-tree. If the tree is empty, then this method
        throws a NoSuchElementException
     */
    public Integer last() throws NoSuchElementException {
        // handle the empty B-tree case
        if (this.elementsCount == 0)
            throw new NoSuchElementException();

        // for all trees with >=1 elements
        Node node = this.root;
        while (! node.isLeaf)
            node = Node.readNodeFromDisk(node.childPointers.get(node.keys.size()));
        return node.keys.get(node.keys.size() - 1);
    }

    /*
        Returns an iterator for the B-tree that will traverse
        each the entire B-tree in sorted ascending order.
     */
    public Iterator<Integer> iterator() {
        return new BTreeIterator(this);
    }

    /*
        Returns a new SortedSet that contains all of the
        keys strictly less than the value toElement in
        the current B-tree
     */
    public SortedSet<Integer> headSet(Integer toElement) {
        Integer smallestElement = this.first();
        if (toElement < smallestElement)
            return BTree.createNewBTreeWithMinNumberOfChildren(this.minNumberOfChildren);
        else
            return walkOverTree(smallestElement, toElement);
    }

    /*
        Returns a new SortedSet that contains all of the
        keys greater than or equal to the value
        fromElement in the current B-tree
     */
    public SortedSet<Integer> tailSet(Integer fromElement) {
        Integer largestElement = this.last();
        if (fromElement > largestElement)
            return BTree.createNewBTreeWithMinNumberOfChildren(this.minNumberOfChildren);
        else
            return walkOverTree(fromElement, largestElement + 1);
    }

    /*
        Returns a new SortedSet that contains all of the
        keys strictly greater than the value FromElement
        and less than or equal to the value toElement
     */
    public SortedSet<Integer> subSet(Integer fromElement, Integer toElement) {
        // fromElement must be less than toElement
        if (fromElement > toElement) {
            return BTree.createNewBTreeWithMinNumberOfChildren(this.minNumberOfChildren);
        }

        return walkOverTree(fromElement, toElement);
    }

    /*
        Modifies the B-tree so that it only contains
        the keys that are also elements in the passed
        collection. That is, the element takes the
        intersection of the passed collection and the
        current B-tree
     */
    public boolean retainAll(Collection<?> c) {
        ArrayList<Integer> integers = new ArrayList<Integer>();
        for (Object o : c) {
            if (this.contains(o))
                integers.add((Integer) o);
        }
        this.clear();
        this.addAll(integers);

        // since retainAll can only reduce the size of B-tree,
        // then if B-tree's size never changes, then no
        // elements were removed
        return this.size() == integers.size();
    }

    /*
        See the Java Documentation for public
        Object[] toArray() for the Set<E> interface
     */
    public Object[] toArray() {
        Object[] array = new Object[this.size()];
        int index = 0;
        for (Integer integer : this)
            array[index++] = integer;
        return array;
    }

    /*
        See the Java Documentation for public
        <T> T[] toArray(T[] a) for the Set<E> interface
     */
    public <T> T[] toArray(T[] a) {
        if (a.length >= this.size()) {
            int index = 0;
            for (Integer integer : this)
                a[index++] = (T) integer;
            return a;
        }
        else {
            T[] biggerArray = (T[]) (new Object[this.size()]);
            int index = 0;
            for (Integer integer : this)
                biggerArray[index++] = (T) integer;
            return biggerArray;
        }
    }

    /*
        Returns null because B-trees use the natural ordering of Integer objects
     */
    public Comparator<? super Integer> comparator() {
        return null;
    }

    /*
        Builds up a String representation of the current B-tree.
     */
    private void buildToString(Node node, int level, ArrayList<StringBuilder> stringObject ) {
        // add another StringBuilder for each new level of the tree
        if (level == stringObject.size()) {
            stringObject.add(new StringBuilder());
        }

        // append the toString for the node to stringBuilder for the level
        StringBuilder currentSB = stringObject.get(level);
        currentSB.append(node.toString());

        // recurse on all of the children of the node
        if (! node.isLeaf) {
            for (String pointer : node.childPointers) {
                Node child = Node.readNodeFromDisk(pointer);
                buildToString(child, level+1, stringObject);
            }
        }
    }

    @Override
    public String toString() {
        // build up the toString content for each level of the tree
        ArrayList<StringBuilder> stringObject = new ArrayList<StringBuilder>();
        buildToString(this.root, 0, stringObject);

        // concatenate the tree level content together separated by "\n"
        StringBuilder mainContent = new StringBuilder();
        for (StringBuilder treeLevel : stringObject) {
            mainContent.append(treeLevel).append("\n");
        }

        return mainContent.toString();
    }
}
