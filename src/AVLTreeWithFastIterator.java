import java.util.NoSuchElementException;

public class AVLTreeWithFastIterator<E extends Comparable<E>> extends BST<E> {
    /** Create an empty AVL tree */
    public AVLTreeWithFastIterator() {
    }

    /** Create an AVL tree from an array of objects */
    public AVLTreeWithFastIterator(E[] objects) {
        super(objects);
    }

    @Override /** Override createNewNode to create an AVLTreeNode */
    protected AVLTreeNode<E> createNewNode(E e) {
        return new AVLTreeNode<E>(e);
    }

    @Override
    public java.util.Iterator<E> iterator() { return new InorderIterator(); }

    public java.util.Iterator<E> iterator(int index) { return new InorderIterator(index); }

    private class InorderIterator implements java.util.Iterator<E> {
        private TreeNode<E> current = root;
        private java.util.Stack<TreeNode<E>> stack = new java.util.Stack<>();

        public InorderIterator() { }

        public InorderIterator(int numToSkip) {
            if (numToSkip < 0 || numToSkip > size)
                throw new IndexOutOfBoundsException();

            //Binary search to position our iterator
            while (numToSkip > 0) {
                //If it's a tiny tree
                if (current.left == null) {
                    if (numToSkip == 1) {
                        current = current.right;
                        numToSkip = 0;
                    }
                    else {
                        current = null;
                        numToSkip = 0;
                    }
                }
                else if (numToSkip == ((AVLTreeNode<E>)current.left).size) {
                    //Skip the left subtree only
                    stack.push(current);
                    current = null;
                    numToSkip = 0;
                }
                else if (numToSkip < ((AVLTreeNode<E>)current.left).size) {
                    //Go into the left subtree
                    stack.push(current);
                    current = current.left;
                }
                else {
                    //Go into the right subtree
                    numToSkip -= ((AVLTreeNode<E>)current.left).size+1; //Will have already skipped  left subtree and current node so take that off
                    current = current.right;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return (current != null || !stack.empty());
        }

        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();
            while (current != null) {
                stack.push(current);
                current = current.left;
            }
            TreeNode<E> node = stack.pop();
            current = node.right;
            return node.element;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override /** Insert an element and rebalance if necessary */
    public boolean insert(E e) {
        boolean successful = super.insert(e);
        if (!successful)
            return false; // e is already in the tree
        else {
            balancePath(e); // Balance from e to the root if necessary
        }

        return true; // e is inserted
    }

    /** Update the height of a specified node */
    private void updateHeightAndSize(AVLTreeNode<E> node) {
        if (node.left == null && node.right == null) // node is a leaf
            node.height = 0;
        else if (node.left == null) // node has no left subtree
            node.height = 1 + ((AVLTreeNode<E>)(node.right)).height;
        else if (node.right == null) // node has no right subtree
            node.height = 1 + ((AVLTreeNode<E>)(node.left)).height;
        else
            node.height = 1 +
                    Math.max(((AVLTreeNode<E>)(node.right)).height,
                            ((AVLTreeNode<E>)(node.left)).height);
        node.size = 1;
        if (node.left != null)
            node.size += ((AVLTreeNode<E>)node.left).size;
        if (node.right != null)
            node.size += ((AVLTreeNode<E>)node.right).size;
    }

    /** Balance the nodes in the path from the specified
     * node to the root if necessary
     */
    private void balancePath(E e) {
        java.util.ArrayList<TreeNode<E>> path = path(e);
        for (int i = path.size() - 1; i >= 0; i--) {
            AVLTreeNode<E> A = (AVLTreeNode<E>)(path.get(i));
            updateHeightAndSize(A);
            AVLTreeNode<E> parentOfA = (A == root) ? null :
                    (AVLTreeNode<E>)(path.get(i - 1));

            switch (balanceFactor(A)) {
                case -2:
                    if (balanceFactor((AVLTreeNode<E>)A.left) <= 0) {
                        balanceLL(A, parentOfA); // Perform LL rotation
                    }
                    else {
                        balanceLR(A, parentOfA); // Perform LR rotation
                    }
                    break;
                case +2:
                    if (balanceFactor((AVLTreeNode<E>)A.right) >= 0) {
                        balanceRR(A, parentOfA); // Perform RR rotation
                    }
                    else {
                        balanceRL(A, parentOfA); // Perform RL rotation
                    }
            }
        }
    }

    /** Return the balance factor of the node */
    private int balanceFactor(AVLTreeNode<E> node) {
        if (node.right == null) // node has no right subtree
            return -node.height;
        else if (node.left == null) // node has no left subtree
            return +node.height;
        else
            return ((AVLTreeNode<E>)node.right).height -
                    ((AVLTreeNode<E>)node.left).height;
    }

    /** Balance LL (see Figure 27.1) */
    private void balanceLL(TreeNode<E> A, TreeNode<E> parentOfA) {
        TreeNode<E> B = A.left; // A is left-heavy and B is left-heavy

        if (A == root) {
            root = B;
        }
        else {
            if (parentOfA.left == A) {
                parentOfA.left = B;
            }
            else {
                parentOfA.right = B;
            }
        }

        A.left = B.right; // Make T2 the left subtree of A
        B.right = A; // Make A the left child of B
        updateHeightAndSize((AVLTreeNode<E>)A);
        updateHeightAndSize((AVLTreeNode<E>)B);
    }

    /** Balance LR (see Figure 27.1c) */
    private void balanceLR(TreeNode<E> A, TreeNode<E> parentOfA) {
        TreeNode<E> B = A.left; // A is left-heavy
        TreeNode<E> C = B.right; // B is right-heavy

        if (A == root) {
            root = C;
        }
        else {
            if (parentOfA.left == A) {
                parentOfA.left = C;
            }
            else {
                parentOfA.right = C;
            }
        }

        A.left = C.right; // Make T3 the left subtree of A
        B.right = C.left; // Make T2 the right subtree of B
        C.left = B;
        C.right = A;

        // Adjust heights
        updateHeightAndSize((AVLTreeNode<E>)A);
        updateHeightAndSize((AVLTreeNode<E>)B);
        updateHeightAndSize((AVLTreeNode<E>)C);
    }

    /** Balance RR (see Figure 27.1b) */
    private void balanceRR(TreeNode<E> A, TreeNode<E> parentOfA) {
        TreeNode<E> B = A.right; // A is right-heavy and B is right-heavy

        if (A == root) {
            root = B;
        }
        else {
            if (parentOfA.left == A) {
                parentOfA.left = B;
            }
            else {
                parentOfA.right = B;
            }
        }

        A.right = B.left; // Make T2 the right subtree of A
        B.left = A;
        updateHeightAndSize((AVLTreeNode<E>)A);
        updateHeightAndSize((AVLTreeNode<E>)B);
    }

    /** Balance RL (see Figure 27.1d) */
    private void balanceRL(TreeNode<E> A, TreeNode<E> parentOfA) {
        TreeNode<E> B = A.right; // A is right-heavy
        TreeNode<E> C = B.left; // B is left-heavy

        if (A == root) {
            root = C;
        }
        else {
            if (parentOfA.left == A) {
                parentOfA.left = C;
            }
            else {
                parentOfA.right = C;
            }
        }

        A.right = C.left; // Make T2 the right subtree of A
        B.left = C.right; // Make T3 the left subtree of B
        C.left = A;
        C.right = B;

        // Adjust heights
        updateHeightAndSize((AVLTreeNode<E>)A);
        updateHeightAndSize((AVLTreeNode<E>)B);
        updateHeightAndSize((AVLTreeNode<E>)C);
    }

    @Override /** Delete an element from the binary tree.
     * Return true if the element is deleted successfully
     * Return false if the element is not in the tree */
    public boolean delete(E element) {
        if (root == null)
            return false; // Element is not in the tree

        // Locate the node to be deleted and also locate its parent node
        TreeNode<E> parent = null;
        TreeNode<E> current = root;
        while (current != null) {
            if (element.compareTo(current.element) < 0) {
                parent = current;
                current = current.left;
            }
            else if (element.compareTo(current.element) > 0) {
                parent = current;
                current = current.right;
            }
            else
                break; // Element is in the tree pointed by current
        }

        if (current == null)
            return false; // Element is not in the tree

        // Case 1: current has no left children (See Figure 23.6)
        if (current.left == null) {
            // Connect the parent with the right child of the current node
            if (parent == null) {
                root = current.right;
            }
            else {
                if (element.compareTo(parent.element) < 0)
                    parent.left = current.right;
                else
                    parent.right = current.right;

                // Balance the tree if necessary
                balancePath(parent.element);
            }
        }
        else {
            // Case 2: The current node has a left child
            // Locate the rightmost node in the left subtree of
            // the current node and also its parent
            TreeNode<E> parentOfRightMost = current;
            TreeNode<E> rightMost = current.left;

            while (rightMost.right != null) {
                parentOfRightMost = rightMost;
                rightMost = rightMost.right; // Keep going to the right
            }

            // Replace the element in current by the element in rightMost
            current.element = rightMost.element;

            // Eliminate rightmost node
            if (parentOfRightMost.right == rightMost)
                parentOfRightMost.right = rightMost.left;
            else
                // Special case: parentOfRightMost is current
                parentOfRightMost.left = rightMost.left;

            // Balance the tree if necessary
            balancePath(parentOfRightMost.element);
        }

        size--;
        return true; // Element inserted
    }

    /** AVLTreeNode is TreeNode plus height */
    protected static class AVLTreeNode<E> extends BST.TreeNode<E> {
        protected int height = 0; // New data field
        public int size = 0;

        public AVLTreeNode(E o) {
            super(o);
        }
    }
}
