/**
 * Created by devinmorgan on 2/18/17.
 */
class NodeIndexPair {
    /*
        Represents a node in B-tree that contains a particular key value
     */
    private final Node node;
    /*
        The index of the particular key in node.keys
     */
    private final int index;

    public NodeIndexPair(Node node, int index) {
        this.node = node;
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public Node getNode() {
        return this.node;
    }
}
