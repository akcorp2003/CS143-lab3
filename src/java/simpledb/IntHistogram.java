package simpledb;


/** A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram {

	private int m_buckets;
	private int m_min;
	private int m_max;
	private int m_bucketwidth;
	private int m_total;
	
	private int m_histogram[];
	
    /**
     * Create a new IntHistogram.
     * 
     * This IntHistogram should maintain a histogram of integer values that it receives.
     * It should split the histogram into "buckets" buckets.
     * 
     * The values that are being histogrammed will be provided one-at-a-time through the "addValue()" function.
     * 
     * Your implementation should use space and have execution time that are both
     * constant with respect to the number of values being histogrammed.  For example, you shouldn't 
     * simply store every value that you see in a sorted list.
     * 
     * @param buckets The number of buckets to split the input value into.
     * @param min The minimum integer value that will ever be passed to this class for histogramming
     * @param max The maximum integer value that will ever be passed to this class for histogramming
     */
    public IntHistogram(int buckets, int min, int max) {
    	// some code goes here
    	m_buckets = buckets;
    	m_min = min;
    	m_max = max;
    	m_bucketwidth = (int)Math.ceil((double)(max - min + 1)/buckets);
    	m_histogram = new int[m_buckets];
    	m_total = 0;
    }
    /**
     * Returns the bucket number in the histogram of a value
     * @param v The value
     * @return The bucket number
     */
    public int getBucketNo(int v){
    	int bucketno = (v - m_min)/m_bucketwidth;
    	if(bucketno > m_buckets)
    		bucketno = m_buckets - 1;
    	return bucketno;
    }

    /**
     * Add a value to the set of values that you are keeping a histogram of.
     * @param v Value to add to the histogram
     */
    public void addValue(int v) {
    	// some code goes here
    	
    	int bucketno = (v - m_min)/m_bucketwidth;
    	m_histogram[bucketno]++;
    	m_total++;
    	
    }

    /**
     * Estimate the selectivity of a particular predicate and operand on this table.
     * 
     * For example, if "op" is "GREATER_THAN" and "v" is 5, 
     * return your estimate of the fraction of elements that are greater than 5.
     * 
     * @param op Operator
     * @param v Value
     * @return Predicted selectivity of this particular operator and value
     */
    public double estimateSelectivity(Predicate.Op op, int v) {

    	// some code goes here
    	double result = 0.0;
    	int bucketno = getBucketNo(v);
    	switch(op){
    	case EQUALS:
    		if(v > m_max || v < m_min)
    			return 0.0;
    		result = ((double)m_histogram[bucketno]/m_bucketwidth) /m_total;
    		break;
    	case NOT_EQUALS:
    		result = 1.0 - estimateSelectivity(Predicate.Op.EQUALS, v);
    		break;
    	case GREATER_THAN_OR_EQ:
    		result = estimateSelectivity(Predicate.Op.EQUALS, v);
    	case GREATER_THAN:
    		if(v < m_min)
    			return 1.0;
    		if(v > m_max)
    			return 0.0;
    		
    		int h_b = m_histogram[bucketno];
    		double b_f = (double)h_b/m_total;
    		int b_right = m_min + ((bucketno+1) * m_bucketwidth)-1;
    		double b_part = (double)(b_right - v) / m_bucketwidth;
    		result += b_f * b_part;
    		
    		for(int x = bucketno+1; x < m_buckets; x++){
    			result += (double) m_histogram[x]/m_total;
    		}
    		break;
    	case LESS_THAN_OR_EQ:
    		result = estimateSelectivity(Predicate.Op.EQUALS,v);
    	case LESS_THAN:
    		if(v < m_min)
    			return 0.0;
    		if(v > m_max)
    			return 1.0;
    		
    		int h_b2 = m_histogram[bucketno];
    		double b_f2 = (double)h_b2/m_total;
    		int b_left = m_min + (bucketno * m_bucketwidth);
    		double b_part2 = (double)(v - b_left) / m_bucketwidth;
    		result += b_f2 * b_part2;
    		
    		for(int x = bucketno-1; x >= 0; x--){
    			result += (double) m_histogram[x]/m_total;
    		}
    		break;
    	default: return -1.0;
    	}
    	
    	
    	
        return result;
    }
    
    /**
     * @return
     *     the average selectivity of this histogram.
     *     
     *     This is not an indispensable method to implement the basic
     *     join optimization. It may be needed if you want to
     *     implement a more efficient optimization
     * */
    public double avgSelectivity()
    {
        // some code goes here
        return 1.0;
    }
    
    /**
     * @return A string describing this histogram, for debugging purposes
     */
    public String toString() {

        // some code goes here
        return null;
    }
}
