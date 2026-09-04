package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Tree;

import lombok.*;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Prediction.Abstract.PredictionStrategy;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Split.Abstract.SplitStrategy;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.SplitResult;

import java.util.Collection;


@RequiredArgsConstructor
public class DecisionTree<T> extends AbstractTree<T> {

    //finds the best way to split the data
    private final SplitStrategy<T> splitStrategy; //this is different for each feauture

    // strategy to predict the value of the leaf nodes
    private final PredictionStrategy<T> predictionStrategy; //this is different for each feauture


    public void build(Collection<T> trainingData) {
        // we recursively build the tree
        this.root = buildTree(trainingData, 0);
    }


    private Node<T> buildTree(Collection<T> data, int depth) {

        ///stopping conditions

        //if the data is small, we build a leaf node
        //continuing would add noise to the tree
        if(data.size() <= 10) {
            System.err.println("data is small, building leaf node " + data.size());
            return buildLeafNode(data);
        }

        //if the tree is deep, we build a leaf node
        //if its too deep it'll start memorizing instead of learning
        if(depth  > MAX_DEPTH) {
            System.err.println("tree is deep, building leaf node " + depth);
            return buildLeafNode(data);
        }

        //if the values are all the same theres no benefit to splitting
        if(predictionStrategy.isAllSame(data)) {

            System.err.println("tree elements are the saame.making leaf node");
            return buildLeafNode(data);
        }
        ///best splitting conditions

        //we find the best split
        var splitResult = splitStrategy.findBestSplit(data);

        //if there was nothing or no visible gain we leave it
        if(splitResult == null || splitResult.gain() <= 0)
            return buildLeafNode(data);



        var leftData = splitResult.leftData();
        var leftSubTree = buildTree(leftData, depth + 1);

        var rightData = splitResult.rightData();
        var rightSubTree = buildTree(rightData, depth + 1);

        //this will always be a internal node
        return buildNode(splitResult, leftSubTree, rightSubTree);
    }

    private Node<T> buildLeafNode(Collection<T> data) {

        double value = predictionStrategy.calculateAverageValue(data);

        return buildNode(value, data.size());
    }

    private Node<T> buildNode(SplitResult<T> splitResult, Node<T> left, Node<T> right) {
        return Node.<T>builder()
                .left(left)
                .right(right)
                .isLeaf(false)
                .splitResult(splitResult)
                .build();
    }

    private Node<T> buildNode(double value, int valuesUsed) {
        return Node.<T>builder()
                .value(value)
                .valuesUsed(valuesUsed)
                .isLeaf(true)
                .build();
    }

}
