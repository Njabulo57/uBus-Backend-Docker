package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Tree;

import lombok.Builder;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.SplitResult;


public abstract class AbstractTree<T> implements ITree<T> {


    protected int depth;
    protected final int MAX_DEPTH = 20;

    protected Node<T> root;


    @Builder
    protected record Node<T>(SplitResult<T> splitResult, Node<T> left,
                             Node<T> right, double value, int valuesUsed,
                             boolean isLeaf) { }


    public double predict(T data) {
        if (root == null)
            throw new IllegalStateException("Tree not built! Call build() first.");


        Node<T> current = root;
        while (!current.isLeaf)
            if (current.splitResult.shouldGoLeft(data))
                current = current.left;
             else
                current = current.right;

        return current.value;
    }
}
