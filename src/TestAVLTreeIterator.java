// This program tests and times the iterator from the AVLTreeWithFastIterator class,
// comparing the results to those from AVLTree.java.
// The methods tested are the iterator() and iterator(int index) methods from the two AVLTree classes,

// - Jeff Ward

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class TestAVLTreeIterator {
    static final int TREE_SIZE = 10000000; // Size of the trees used in the timing tests.
    public static void main(String[] args) {
        // Data for small tests
        Integer[] array = new Integer[]{ 50, 30, 60, 20, 40, 55, 70, 10, 25, 57, 65, 80};

        // Data for timing tests
        Random rand = new Random(50);
        AVLTree<Integer> tree = new AVLTree<>();
        AVLTreeWithFastIterator<Integer> treeWithFastIter = new AVLTreeWithFastIterator<>();
        long startTime = System.currentTimeMillis();
        System.out.printf("Adding %,d random values to AVLTree and to AVLTreeWithFastIterator.\n", TREE_SIZE);
        while (tree.size() < TREE_SIZE) {
            int value = rand.nextInt();
            tree.add(value);
            treeWithFastIter.add(value);
        }
        long endTime = System.currentTimeMillis();
        double seconds = (endTime - startTime) / 1000.0;
        System.out.printf("Time to create trees:  %1.3f seconds\n", seconds);
        if (tree.size() != treeWithFastIter.size())
            throw new RuntimeException("Tree sizes do not match: " + tree.size()
                    + " versus " + treeWithFastIter.size());

        System.out.println("Testing Part I:  iterator(), hasNext(), and next()");
        smallTestPartI(array);
        timingTestPartI(tree, treeWithFastIter);
        System.out.println("End of Part I tests");

        // Uncomment the following lines when you are ready to test your code for Part II of the assignment:
        System.out.println("Testing Part II:  iterator(int index)");
        smallTestPartII(array);
        timingTestPartII(tree, treeWithFastIter);
        System.out.println("End of Part II tests");
    }

    // Test the iterator(), hasNext() and next() methods.  Specifically:
    // Create an AVLTreeWithFastIterator from the array.
    // Create an iterator from it using the iterator() method.
    // Then use hasNext() and next() to step through the tree,
    // checking that the correct sequence of values is returned.
    public static void smallTestPartI(Integer[] array) {
        AVLTreeWithFastIterator<Integer> tree = new AVLTreeWithFastIterator<>(array);
        Integer[] arraySorted = Arrays.copyOf(array, array.length);
        Arrays.sort(arraySorted);
        int count = 0;
        for (Iterator<Integer> iter = tree.iterator(); iter.hasNext(); ) {
            int element = iter.next();
            if (element != arraySorted[count])
                throw new RuntimeException("AVLTree iterator returned incorrect value at index: " + count
                        + " Returned: " + element + " Correct value is: " + arraySorted[count]);
            count++;
        }
        if (count != array.length)
            throw new RuntimeException("AVLTree iterator returned incorrect number of elements."
                    + " Returned " + count + " elements.  Correct number is " + array.length + "." );
    }

    // Tests the iterator(index) method.  Specifically:
    // Create an AVLTreeWithFastIterator from the array.
    // Test that iterator(int) throws an exception if the index is out of range.
    // Then, for each valid index i, call iterator(int) on i
    // and test that the resulting iterator returns the correct sequence.
    public static void smallTestPartII(Integer[] array) {
        AVLTreeWithFastIterator<Integer> tree = new AVLTreeWithFastIterator<>(array);
        Integer[] arraySorted = Arrays.copyOf(array, array.length);
        Arrays.sort(arraySorted);
        for (int i : new int[] {-1, array.length + 1})
            try {
                tree.iterator(-1);
                throw new RuntimeException("iterator(int) did not throw IndexOutOfBoundsException on bad index"
                    + i);
            }
            catch(IndexOutOfBoundsException ex) {
                // Caught IndexOutOfBoundsException:  Good!
            }
        for (int i = 0; i < tree.size(); i++) {
            int count = 0;
            for (Iterator<Integer> iter = tree.iterator(i); iter.hasNext(); ) {
                int element = iter.next();
                if (element != arraySorted[i + count])
                    throw new RuntimeException("AVLTree iterator returned incorrect value at index: " + (i + count)
                            + " Returned: " + element + " Correct value is: " + arraySorted[i + count]);
                count++;
            }
            if (count != array.length - i)
                throw new RuntimeException("AVLTree iterator returned incorrect number of elements."
                        + " Returned " + count + " elements.  Correct number is " + (array.length - i) + "." );
        }
    }

    // Run timing tests on the iterators from the two trees, and verify that that return the same sequence.
    // The iterator() method is used to create the iterators.
    // (Testing and timing the iterator(int) method is done in Part II.)
    public static void timingTestPartI(AVLTree<Integer> tree, AVLTreeWithFastIterator<Integer> treeWithFastIter) {
        // Time the iterators and check that they return the same sequences
        System.out.println("Using plain AVLTree:");
        ArrayList<ArrayList<Integer>> list1 = timeIterator(tree, -1);
        System.out.println("Using AVLTreeWithFastIterator:");
        ArrayList<ArrayList<Integer>> list2 = timeIterator(treeWithFastIter, -1);
        if (!list1.equals(list2))
            throw new RuntimeException("Results do not match.");
    }

    // Run timing tests on the iterators from the two trees, and verify that that return the same sequence.
    // The iterator(int) method is used to create the iterators.
    // Five iterators are created from each tree:
    //    one iterator at the beginning of the collection,
    //    one iterator that starts a quarter of the way into the collection,
    //    one iterator that starts halfway into the collection,
    //    one iterator that starts three quarters of the way into the collection, and
    //    one iterator that starts at the end of the collection.
    public static void timingTestPartII(AVLTree<Integer> tree, AVLTreeWithFastIterator<Integer> treeWithFastIter) {
        for (int startIndex : new int[] {0, tree.size() / 4, tree.size() / 2, 3 * tree.size() / 4, tree.size() }) {
            System.out.println("Using plain AVLTree:");
            ArrayList<ArrayList<Integer>> list1 = timeIterator(tree, startIndex);
            System.out.println("Using AVLTreeWithFastIterator:");
            ArrayList<ArrayList<Integer>> list2 = timeIterator(treeWithFastIter, startIndex);
            if (!list1.equals(list2))
                throw new RuntimeException("Results do not match.");
        }
    }

    // Times and displays:
    // (1) how long it takes to create an iterator from the bst,
    // (2) how long it takes to use the iterator to iterate through the first few (e.g. 10) elements, and
    // (3) how long it takes to use the iterator to iterate through the remaining elements.
    // If iteratorIndex is negative then we use iterator() to create the iterator.
    // If iteratorIndex is non-negative then we use iterator(iteratorIndex) to create the iterator.
    // The return value is a two-dimensional ArrayList:
    // Row 0 is the sequence returned by iterating through the first several elements.
    // Row 1 is the sequence returned by iterating through the remaining elements.
    // This return value can be used by the caller to check that the correct sequences were returned.
    public static ArrayList<ArrayList<Integer>> timeIterator(BST<Integer> bst, int iteratorIndex) {
        ArrayList<ArrayList<Integer>> returnValue = new ArrayList<>(2);

        // Create iterator
        long startTime = System.currentTimeMillis();
        Iterator<Integer> iter;
        if (iteratorIndex < 0) {
            System.out.print("\tTime required for iterator() call: ");
            iter = bst.iterator();
        }
        else {
            System.out.print("\tTime required for iterator(" + iteratorIndex + ") call: ");
            iter = bst.iterator(iteratorIndex);
        }
        long endTime = System.currentTimeMillis();
        double seconds = (endTime - startTime) / 1000.0;
        System.out.printf("%1.3f seconds\n", seconds);

        // Iterate through first few elements only
        final int NUM_VALUES_TAKEN_AT_FIRST = 10;
        if (iteratorIndex + NUM_VALUES_TAKEN_AT_FIRST < bst.size()) {
            System.out.print("\tTime to iterate through " + NUM_VALUES_TAKEN_AT_FIRST + " elements:  ");
            startTime = System.currentTimeMillis();
            ArrayList<Integer> valuesFromIter = new ArrayList<>(NUM_VALUES_TAKEN_AT_FIRST);
            for (int i = 0; i < NUM_VALUES_TAKEN_AT_FIRST; i++)
                valuesFromIter.add(iter.next());
            endTime = System.currentTimeMillis();
            seconds = (endTime - startTime) / 1000.0;
            System.out.printf("%1.3f seconds\n", seconds);
            returnValue.add(valuesFromIter);

            // Iterate through the remaining elements
            System.out.print("\tTime to iterate through remaining elements:  ");
            valuesFromIter = new ArrayList<>(TREE_SIZE);
            startTime = System.currentTimeMillis();
            while (iter.hasNext())
                valuesFromIter.add(iter.next());
            endTime = System.currentTimeMillis();
            seconds = (endTime - startTime) / 1000.0;
            System.out.printf("%1.3f seconds\n", seconds);
            returnValue.add(valuesFromIter);
        }

        return returnValue;
    }
}
