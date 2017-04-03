import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * Created by devinmorgan on 2/12/17.
 */
class BTreeIterator implements Iterator<Integer> {

    /*
        The B-tree that this Iterator will iterate
        over
     */
    private final BTree bTree;

    /*
        The node that contains the key that would
        be returned by next()
     */
    private Node currentNode;

    /*
        The index of the key in the current node
        that would be returned by next()
     */
    private int currentChildIndex;

    /*
        Creates an iterator that starts at the
        smallest key in the B-tree and iterates
        through the B-tree in ascending order
     */
    BTreeIterator(BTree bTree) {
        this.bTree = bTree;
        this.currentNode = bTree.getSmallestNode();
        this.currentChildIndex = 0;
    }

    /*
        Creates an iterator that starts at the
        first key value that is greater than or
        equal to the value startingFrom. If
        startingFrom is bigger than the largest
        key in the B-tree, then this will throw
        a NoSuchElementException
     */
    BTreeIterator(BTree bTree, Integer startingFrom) throws NoSuchElementException {
        this.bTree = bTree;

        if (this.bTree.size() == 0)
            throw new NoSuchElementException();

        // check to see if startingFrom is greater than the largest value in the B-tree
        if (startingFrom > bTree.last()) {
            this.currentNode = null;
            this.currentChildIndex = -1;
            return;
        }

        Node node = bTree.root;
        int minValidStartingKey = Integer.MIN_VALUE;
        while (! node.isLeaf) {
            for (int i = 0; i < node.keys.size(); i++) {

                // found a larger value, recurse on corresponding node
                if (node.keys.get(i) > startingFrom) {
                    minValidStartingKey = node.keys.get(i);
                    node = Node.readNodeFromDisk(node.childPointers.get(i));
                    continue;
                }

                // found the exact value, we're done!
                else if (node.keys.get(i) == startingFrom) {
                    this.currentNode = node;
                    this.currentChildIndex = i;
                    return;
                }
            }

            // all values are less than startingFrom, explore the largest child node
            node = Node.readNodeFromDisk(node.childPointers.get(node.keys.size()));
        }

        // explore the keys of the leaf to find first value >= startingFrom
        for (int i = 0; i < node.keys.size(); i++) {
            if (node.keys.get(i) >= startingFrom) {
                this.currentNode = node;
                this.currentChildIndex = i;
                return;
            }
        }

        // couldn't find a viable key in the leaf so go with the best viable one we found so far
        NodeIndexPair nodeIndexPair = this.bTree.search(this.bTree.root, minValidStartingKey);
        this.currentNode = nodeIndexPair.getNode();
        this.currentChildIndex = nodeIndexPair.getIndex();
    }

    /*
        Advances the iterator to point to the next
        key in the parent node of the current node
     */
    private void moveToNextKeyInParent() {
        // load the current node's parent
        Node parent = Node.readNodeFromDisk(this.currentNode.parentPointer);

        // find the index of the pointer, to the current
        // node, in the parent's childPointers
        int childIndex = 0;
        while (! parent.childPointers.get(childIndex).equals(this.currentNode.getPointer()))
            childIndex++;
        this.currentNode = parent;

        // determine whether or not should keep iterating
        // through the parent's children or whether we
        // should explore the next parent
        if (childIndex == parent.keys.size() && parent.parentPointer != null)
            this.moveToNextKeyInParent();
        else
            this.currentChildIndex = childIndex;
    }

    /*
        Advances the iterator to point to the next
        key in the next child node of the current
        node (assumes the current node is not a leaf node)
     */
    private void moveToSmallestKeyInSubtree() {
        // start looking in the child's subtree
        Node child = Node.readNodeFromDisk(this.currentNode.childPointers.get(this.currentChildIndex + 1));

        // find the left most node in child's subtree
        while (! child.isLeaf)
            child = Node.readNodeFromDisk(child.childPointers.get(0));
        this.currentNode = child;

        // go to the beginning of this node
        this.currentChildIndex = 0;
    }

    /*
        Advances the iterator to the next key in the
        B-tree in ascending order. Throws a NoSuchElementException
        if the iteration does not contain anymore elements
     */
    public Integer next() throws NoSuchElementException {
        // check that there is a next element
        if (! this.hasNext())
            throw new NoSuchElementException();

        // if we have not explored all of the keys in a leaf node
        if (this.currentChildIndex < this.currentNode.keys.size() && this.currentNode.isLeaf) {
            return this.currentNode.keys.get(this.currentChildIndex++);
        }

        // if we have explored all of the keys in a leaf node
        if (this.currentChildIndex == this.currentNode.keys.size() && this.currentNode.isLeaf) {
            this.moveToNextKeyInParent();
            return this.currentNode.keys.get(this.currentChildIndex);
        }

        // if we have not explored all of the keys in a non-leaf node
        if (this.currentChildIndex < this.currentNode.keys.size() && ! this.currentNode.isLeaf) {
            this.moveToSmallestKeyInSubtree();
            return this.currentNode.keys.get(this.currentChildIndex++);
        }

        // if we explored all of the keys but not all of the children in a non-leaf node
        if (this.currentChildIndex == this.currentNode.keys.size() && ! this.currentNode.isLeaf) {
            this.moveToSmallestKeyInSubtree();
            return this.currentNode.keys.get(this.currentChildIndex);
        }

        // if we explored all of the keys and all of the children in a non-leaf node
        if (this.currentChildIndex > this.currentNode.keys.size() && ! this.currentNode.isLeaf) {
            this.moveToNextKeyInParent();
            return this.currentNode.keys.get(this.currentChildIndex);
        }

        throw new RuntimeException("something went wrong!");
    }

    /*
        Indicates whether or not the Iterator has finished
        iterating through the B-tree. Returns true if
        there is at least one more element to iterate over.
        Returns false otherwise
     */
    public boolean hasNext() {
        if (this.bTree.size() == 0)
            return false;
        if (this.currentNode.equals(this.bTree.root)) {
            if (this.bTree.root.isLeaf) {
                return this.currentChildIndex < this.bTree.root.keys.size();
            }
            else {
                return this.currentChildIndex < this.bTree.root.keys.size();
            }
        }
        else if (this.currentNode.isLeaf) {
            if (this.currentChildIndex < this.currentNode.keys.size())
                return true;
            else
                return this.currentNode.keys.get(this.currentNode.keys.size() - 1) != this.bTree.last();
        }
        else {
            if (this.currentChildIndex < this.currentNode.keys.size() + 1)
                return true;
            else
                return this.currentNode.keys.get(this.currentNode.keys.size() - 1) != this.bTree.last();
        }
    }

    /*
        Removes the current element from the B-tree and
        advances the iterator over to the next element.
        This is the only safe way to remove an element
        form the B-tree while iterating over it
     */
    public void remove() {
        // don't remove anything from an empty tree
        if (this.bTree.size() == 0)
            return;

        // determine the next key the Iterator should
        // point at after deleting the current key
        Integer nextKey;
        if (this.hasNext()) {
            nextKey = this.next();
        }
        else {
            nextKey = this.bTree.last();
        }

        // remove the current key from the B-tree
        Integer removedValue = this.currentNode.keys.get(this.currentChildIndex);
        this.bTree.remove(removedValue);

        // update the iterator to point to the next key
        BTreeIterator temp = new BTreeIterator(this.bTree, nextKey);
        this.currentChildIndex = temp.currentChildIndex;
        this.currentNode = temp.currentNode;
    }
}
