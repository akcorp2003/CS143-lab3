package simpledb;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Knows how to compute some aggregate over a set of IntFields.
 */
public class IntegerAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;

    private int m_gbfield;
    private Type m_gbfieldtype;
    private int m_afield;
    private Op m_op;
    
    private TupleDesc m_td;
    private HashMap<Field, AggregateCounter> m_aggmap;
    
    
    //simple class that contains both a value and a count
    private class AggregateCounter{
		public int value;
		public int count = 0;
		public AggregateCounter(int val, int c){
			value = val;
			count = c;
		}
	}
    
    /**
     * Aggregate constructor
     * 
     * @param gbfield
     *            the 0-based index of the group-by field in the tuple, or
     *            NO_GROUPING if there is no grouping
     * @param gbfieldtype
     *            the type of the group by field (e.g., Type.INT_TYPE), or null
     *            if there is no grouping
     * @param afield
     *            the 0-based index of the aggregate field in the tuple
     * @param what
     *            the aggregation operator
     */

    public IntegerAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
    	m_gbfield = gbfield;
    	m_gbfieldtype = gbfieldtype;
    	m_afield = afield;
    	m_op = what;
    	m_aggmap = new HashMap<Field, AggregateCounter>();
    	
    	String[] name;
    	Type[] type;
    	if(m_gbfield == Aggregator.NO_GROUPING){
    		name = new String[] {"aggregateValue"};
    		type = new Type[] {Type.INT_TYPE};
    	}
    	else{
    		name = new String[] {"groupValue", "aggregateValue"};
    		type = new Type[] {m_gbfieldtype, Type.INT_TYPE};
    	}
    	m_td = new TupleDesc(type, name);
    	
    	
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the
     * constructor
     * 
     * @param tup
     *            the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
    	Field field;
    	if(m_gbfield != Aggregator.NO_GROUPING)
    		field = tup.getField(m_gbfield);
    	else
    		field = null;
    	
    	int value = ((IntField) tup.getField(m_afield)).getValue();
    	if(!m_aggmap.containsKey(field)){
    		m_aggmap.put(field, new AggregateCounter(value,1));
    	}
    	else{
    		AggregateCounter currentvalue = m_aggmap.get(field);
    		currentvalue.count += 1;
    		switch(m_op){
    		case MIN:
    			if(currentvalue.value > value)
    				currentvalue.value = value;
    			break;
    		case MAX:
    			if(currentvalue.value < value)
    				currentvalue.value = value;
    			break;
    		
    		case SUM: case AVG: //we will calculate average at the end
    			currentvalue.value += value;
    			break;
    		//case AVG:
    			//currentvalue.value = (value+currentvalue.value)/(currentvalue.count);
    		default:
    			break;
    		}
    		
    		m_aggmap.put(field, currentvalue);
    	}
    	
    }

    /**
     * Create a DbIterator over group aggregate results.
     * 
     * @return a DbIterator whose tuples are the pair (groupVal, aggregateVal)
     *         if using group, or a single (aggregateVal) if no grouping. The
     *         aggregateVal is determined by the type of aggregate specified in
     *         the constructor.
     */
    public DbIterator iterator() {
        // some code goes here
        //throw new
        //UnsupportedOperationException("please implement me for lab2");
        
        ArrayList<Tuple> tuplearray = new ArrayList<Tuple>();
        
        if(m_gbfield == Aggregator.NO_GROUPING){
        	Tuple newtuple = new Tuple(m_td);
        	
        	if(m_op == Op.AVG){
    			newtuple.setField(0, new IntField(m_aggmap.get(null).value
    					/m_aggmap.get(null).count));
    		}
    		else if(m_op == Op.COUNT){
    			newtuple.setField(0, new IntField(m_aggmap.get(null).count));
    		}
    		else{
    			newtuple.setField(0, new IntField(m_aggmap.get(null).value));
    		}
        	tuplearray.add(newtuple);
        }
        else{
        	for(Field fieldkey : m_aggmap.keySet()){
        		Tuple newtuple = new Tuple(m_td);
        		newtuple.setField(0, fieldkey);
        		
        		//calculate average now
        		if(m_op == Op.AVG){
        			newtuple.setField(1, new IntField(m_aggmap.get(fieldkey).value
        					/m_aggmap.get(fieldkey).count));
        		}
        		else if(m_op == Op.COUNT){
        			newtuple.setField(1, new IntField(m_aggmap.get(fieldkey).count));
        		}
        		else
        			newtuple.setField(1, new IntField(m_aggmap.get(fieldkey).value));
        		tuplearray.add(newtuple);
        	}
        }
        return new TupleIterator(m_td,tuplearray);
    }

}
