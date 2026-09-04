package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Prediction.Abstract;



import java.util.Collection;

public interface PredictionStrategy<T> {


    /**
     * Determines whether all elements in the provided collection are the same.
     *
     * @param data the collection of elements to be checked
     * @return true if all elements in the collection are identical, otherwise false
     */
    boolean isAllSame(Collection<T> data);


    /**
     * Calculates the average value of the elements in the provided collection.
     *
     * @param data the collection of elements for which the average value needs to be computed
     * @return the calculated average value as a double
     */
    double calculateAverageValue(Collection<T> data);

    /**
     * Retrieves the numeric value associated with the specified input data.
     *
     * @param data the input data from which the numeric value is derived
     * @return the numeric value as a double
     */
    double getValue(T data);


    /**
     * Calculates the variance of the numeric values derived from the elements in the provided collection.
     * Variance measures the average squared difference from the mean value of the collection.
     *
     * @param data the collection of elements from which the variance is computed
     *             Must contain at least two elements; otherwise, the method returns 0.
     * @return the calculated variance as a double; returns 0 if the collection size is less than 2
     */
    default double calculateVariance(Collection<T> data) {

        if(data.size() < 2) return 0;

        //we ge the mean value
        double mean = calculateAverageValue(data);

        //we calculate the variance by summing the squared differences
        var sum = 0.0;
        for(var item : data) {
            var difference = getValue(item) - mean;
            sum += difference * difference;
        }

        //we average the squared differences
        return sum / data.size();
    }




}
