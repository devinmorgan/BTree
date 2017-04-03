import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by devinmorgan on 2/12/17.
 */
class Node {
    /*
        A file that contains all of information about a
        Node so that it the node can be stored on Disk
        in case the data is larger than main memory
     */
    final File diskLocation;

    /*
        The minimum number of child nodes that a given
        node can have. A node always has minimumChildCount - 1
        keys (except if the node is a root node).
     */
    private final int minimumChildCount;

    /*
        Indicates whether the current node is a leaf node
        or an internal node. It is true if the node is a
        leaf node and false if it is an internal node
     */
    boolean isLeaf;

    /*
        An ArrayList<Integers> that contains all of this
        node's keys in sorted ascending order
     */
    final ArrayList<Integer> keys;

    /*
        An ArrayList<String> that contains the pointers to
        all of this nodes child nodes. The pointers are
        represented as Strings of the absolute file paths to
        the location of the child node's data (files) on disk.
        All of the pointers are null if the node is a leaf node.
     */
    final ArrayList<String> childPointers;

    /*
        A pointer to the parent of this node. The pointer is
        a String of the absolute file path to the data (file)
        on the disk. The pointer is "null" if current node is
         a root node
     */
    String parentPointer;

    /*
        A private constructor for the Node class
     */
    private Node(File diskLocation,
                 boolean isLeaf,
                 int minimumChildCount,
                 ArrayList<Integer> keys,
                 ArrayList<String> childPointers,
                 String parentPointer) {
        this.diskLocation = diskLocation;
        this.minimumChildCount = minimumChildCount;
        this.isLeaf = isLeaf;
        this.keys = keys;
        this.childPointers = childPointers;
        this.parentPointer = parentPointer;
    }

    /*
        A package private factor method for the Node class.
        The node must have a minimum child count and must
        have a pointer to its parent node (can be null if
        the node doesn't have a parent)
     */
    static Node allocateNode(int minimumChildCount, String parentPointer) {
        try{
            File file = File.createTempFile("node", ".txt");
            return new Node(file, true, minimumChildCount, new ArrayList<Integer>(), new ArrayList<String>(), parentPointer);
        }
        catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    /*
        Mutates the rightSibling, this, and the parent node
        according to case 2.a) in CLRS Chapter 18, B-Trees
        on page 501
     */
    private Node grabElementFromRightSiblingNode(Node rightSibling, Node parent, int currentNodeIndex) {
        // append the parent's key that is to the right
        // of this node's pointer
        this.keys.add(parent.keys.get(currentNodeIndex));

        // remove rightSiblings left-most key and swap it
        // with the parent's key that is to the right of
        // this node's pointer
        parent.keys.set(currentNodeIndex, rightSibling.keys.remove(0));

        // remove rightSibling's left-most pointer
        // and append it to this.childPointers
        this.childPointers.add(rightSibling.childPointers.remove(0));

        // save the changes to rightSibling, this, and parent node
        Node.writeNodeToDisk(rightSibling);
        Node.writeNodeToDisk(this);
        Node.writeNodeToDisk(parent);
        return this;
    }

    /*
        Mutates the rightSibling, this, and the parent node
        according to case 2.b) in CLRS Chapter 18, B-Trees
        on page 501
     */
    private Node grabElementFromLeftSiblingNode(Node leftSibling, Node parent, int currentNodeIndex) {
        // append the parent's key that is to the left
        // of this node's pointer
        this.keys.add(parent.keys.get(currentNodeIndex - 1));

        // remove leftSiblings right-most key and swap it
        // with the parent's key that is to the left of
        // this node's pointer
        parent.keys.set(currentNodeIndex - 1, leftSibling.keys.remove(leftSibling.keys.size() - 1));

        // remove leftSibling's right-most pointer
        // and append it to this.childPointers
        this.childPointers.add(leftSibling.childPointers.remove(leftSibling.keys.size() - 1));

        // save the changes to leftSibling, this, and parent
        Node.writeNodeToDisk(leftSibling);
        Node.writeNodeToDisk(this);
        Node.writeNodeToDisk(parent);
        return this;
    }

    /*
        Merges mergingNode into lastingNode by copying over
        mergingNode's keys and pointers (to its children)
        and by adding the adjacent key from parent. This node
        will delete mergingNode once the merge is finished
        and will also delete parent if it would not contain any
        keys as a result of the merge
     */
    static Node merge(Node lastingNode, int key, Node mergingNode, Node parent) {
        // add key to lastingNode.keys first
        lastingNode.keys.add(key);

        // remove all of the keys and pointers from mergingNode
        // and add them to lastingNode
        for (int i = 0; i < mergingNode.keys.size(); i++) {
            lastingNode.keys.add(mergingNode.keys.remove(0));
            lastingNode.childPointers.add(mergingNode.childPointers.remove(0));
        }

        // there is always 1 more pointer than key
        lastingNode.childPointers.add(mergingNode.childPointers.remove(0));

        // delete the parent node if it's empty
        if (parent.keys.isEmpty()) {

            // set the lastingNode parentPointer to the parentPointer of the old parent
            lastingNode.parentPointer = parent.parentPointer;

            // set the childPointer to the old parent in the old parent's parent to point to the lastingNode
            if (parent.parentPointer != null) {
                Node parentOfOldParent = Node.readNodeFromDisk(parent.parentPointer);
                int index = parentOfOldParent.childPointers.indexOf(parent.getPointer());
                parentOfOldParent.childPointers.set(index, lastingNode.getPointer());

                // save the changes to parentOfOldParent to disk
                Node.writeNodeToDisk(parentOfOldParent);
            }

            // delete the parents data
            parent.childPointers.clear();
            File parentNodeDiskContents = new File(parent.getPointer());
            parentNodeDiskContents.delete();
        }
        else {
            // remove the pointer to mergingNode from the parent
            parent.childPointers.remove(mergingNode.getPointer());

            // save the changes to parent node on disk
            Node.writeNodeToDisk(parent);
        }

        // delete mergingNode's content from disk memory
        File mergingNodeDiskContents = new File(mergingNode.getPointer());
        mergingNodeDiskContents.delete();

        // save changes to lastingNode (mergingNode get's deleted and parent node got saved already)
        Node.writeNodeToDisk(lastingNode);
        return lastingNode;
    }

    /*
        Combines cases 2.a) - 3.b) from CLRS Chapter 18, B-Trees
        on pages 500-502 by evaluating the number of keys in
        the current node and its sibling nodes
     */
    Node grabElementFromOrMergeWithSiblingNode(Node parent, int index) {
        if (index == 0) {
            Node rightSibling = Node.readNodeFromDisk(parent.childPointers.get(index+1));

            // merge this node with right sibling if right
            // sibling has the minimum number of keys
            if (rightSibling.keys.size() == this.minimumChildCount - 1) {
                int key = parent.keys.remove(index);
                return Node.merge(this, key, rightSibling, parent);
            }

            // otherwise grab an element from the right sibling
            return this.grabElementFromRightSiblingNode(rightSibling, parent, index);
        }
        else if (index == parent.keys.size()) {
            Node leftSibling = Node.readNodeFromDisk(parent.childPointers.get(index -1 ));

            // merge this node with left sibling if left
            // sibling has the minimum number of keys
            if (leftSibling.keys.size() == this.minimumChildCount - 1) {
                int key = parent.keys.remove(index - 1);
                return Node.merge(leftSibling, key, this, parent);
            }

            // otherwise grab an element from the left sibling
            return this.grabElementFromLeftSiblingNode(leftSibling, parent, index);
        }
        else {
            Node leftSibling = Node.readNodeFromDisk(parent.childPointers.get(index -1 ));
            Node rightSibling = Node.readNodeFromDisk(parent.childPointers.get(index+1));

            // merge this node with a right sibling if both left
            // and right siblings have the minimum number of keys
            if (leftSibling.keys.size() == this.minimumChildCount - 1
                    && rightSibling.keys.size() == this.minimumChildCount - 1) {
                int key = parent.keys.remove(index);
                return Node.merge(this, key, rightSibling, parent);
            }

            // otherwise grab an element for the appropriate sibling
            else if (leftSibling.keys.size() == this.minimumChildCount - 1)
                return this.grabElementFromRightSiblingNode(rightSibling, parent, index);
            else
                return this.grabElementFromLeftSiblingNode(leftSibling, parent, index);
        }
    }

    /*
        Returns the absolute path to the file that contains this node's data on disk
     */
    String getPointer() {
        return this.diskLocation.getAbsolutePath();
    }

    /*
        Reconstructs the node from the data stored on disk
        at the absolute file path identified by pointer.
     */
    static Node readNodeFromDisk(String pointer) {
        try {
            File diskLocation = new File(pointer);
            Scanner sc = new Scanner(diskLocation);

            // content of line 0 is minimumChildCount
            int minimumChildCount = sc.nextInt();
            sc.nextLine();

            // content of line 1 is isLeaf
            boolean isLeaf = sc.nextBoolean();
            sc.nextLine();

            // contents of line 2 are values of keys as space separated Integers
            ArrayList<Integer> keys = new ArrayList<Integer>();
            while (sc.hasNextInt()) {
                keys.add(sc.nextInt());
            }
            sc.nextLine();

            // contents of line 3 are values of pointers as space separated Strings
            String[] pointers = sc.nextLine().split(" ");
            ArrayList<String> childPointers = new ArrayList<String>(Arrays.asList(pointers));

            // if is a leaf, then convert "null" to null
            for (int i = 0; i < childPointers.size(); i++) {
                if (childPointers.get(i).equals("null"))
                    childPointers.set(i, null);
                else
                    break;
            }

            // contents of line 4 is the parent pointer
            String parentPointer = sc.nextLine();

            return new Node(diskLocation, isLeaf, minimumChildCount, keys, childPointers, parentPointer);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
        Saves the nodes data to disk. It will either create
        a new file on disk if the node has not previously
        been saved or it will update the existing file if
        the node has already been created.
     */
    static void writeNodeToDisk(Node n) {
        try {
            StringBuilder fileContent = new StringBuilder();

            // line 0 contains minimumChildCount
            fileContent.append(n.minimumChildCount + "\n");

            // line 1 contains n.isLeaf
            fileContent.append(n.isLeaf + "\n");

            // line 2 contains n.keys as a sequence of space separated integers
            for (Integer i : n.keys)
                fileContent.append(i + " ");
            fileContent.append("\n");

            // line 3 contains n.childPointers as a sequence of space separated strings
            for (String s : n.childPointers)
                fileContent.append(s + " ");
            fileContent.append("\n");

            // line 4 contains n.parentPointer as a String
            fileContent.append(n.parentPointer + "\n");

            // write the fileContents to disk
            PrintWriter writer = new PrintWriter(n.diskLocation);
            writer.println(fileContent);
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Removes the element with the smallest key value
        form the subtree whose root is this node
     */
    int removeSmallestKeyFromSubtree() {
        Node node = this;
        while (! node.isLeaf)
            node = Node.readNodeFromDisk(node.childPointers.get(0));

        // remove the key and the pointer
        node.childPointers.remove(0);
        int smallestKey = node.keys.remove(0);

        // save the changes to the node on the disk
        Node.writeNodeToDisk(node);
        return smallestKey;
    }

    /*
        Removes the element with the largest key value
        from the subtree whose root is this node
     */
    int removeLargestKeyFromSubtree() {
        Node node = this;
        while (! node.isLeaf)
            node = Node.readNodeFromDisk(node.childPointers.get(0));

        // remove the key and pointer
        node.childPointers.remove(node.keys.size());
        int largestKey = node.keys.remove(node.keys.size() - 1);

        // save the changes to the node on the disk
        Node.writeNodeToDisk(node);
        return largestKey;
    }

    /*
        Deletes all of the Data (from disk and from main
        memory) for each of the nodes in the subtree whose
        root is node.
     */
    static void deleteSubtree(Node node) {
        // recursively delete all of the data from children first
        for (String childPointer : node.childPointers) {
            if (childPointer != null) {
                Node child = Node.readNodeFromDisk(childPointer);
                Node.deleteSubtree(child);
            }
        }

        // clear this node's data
        node.keys.clear();
        node.parentPointer = null;
        node.childPointers.clear();
        File diskContent = new File(node.getPointer());
        diskContent.delete();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        for (Integer k : this.keys) {
            sb.append(k).append(",");
        }
        sb.append("|");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            Node that = (Node) obj;
            return this.diskLocation.equals(that.diskLocation);
        }
        return false;
    }
}

