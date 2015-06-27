package simpledb;

import java.util.HashMap;
import java.util.ArrayList;



//import simpledb.IntegerAggregator.AggregateCounter;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    private int m_gbfield;
    private Type m_gbfieldtype;
    private int m_afield;
    //private Op m_op;
    
    private TupleDesc m_td;
    private HashMap<Field,Integer> m_aggmap;
    


    /**
     * Aggregate constructor
     * @param gbfield the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield the 0-based index of the aggregate field in the tuple
     * @param what aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
    	if(what == Op.COUNT){
    		m_gbfield = gbfield;
        	m_gbfieldtype = gbfieldtype;
        	m_afield = afield;
        	//m_op = what;
        	
        	m_aggmap = new HashMap<Field,Integer>();
        	
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
    	else {
    		throw new IllegalArgumentException("Operator must be COUNT!");
    	}
    	
    	
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
    	Field field;
    	if(m_gbfield != Aggregator.NO_GROUPING)
    		field = tup.getField(m_gbfield);
    	else
    		field = null;
    	
    	if(!m_aggmap.containsKey(field)){
    		m_aggmap.put(field, 1);
    	}
    	else{
    		int currentcount = m_aggmap.get(field);
    		currentcount++;
    		m_aggmap.put(field, currentcount);
    	}
    }

    /**
     * Create a DbIterator over group aggregate results.
     *
     * @return a DbIterator whose tuples are the pair (groupVal,
     *   aggregateVal) if using group, or a single (aggregateVal) if no
     *   grouping. The aggregateVal is determined by the type of
     *   aggregate specified in the constructor.
     */
    public DbIterator iterator() {
        // some code goes here
        //throw new UnsupportedOperationException("please implement me for lab2");
    	
    	ArrayList<Tuple> tuplearray = new ArrayList<Tuple>();
    	if(m_gbfield == Aggregator.NO_GROUPING){
        	Tuple newtuple = new Tuple(m_td);
        	int number = m_aggmap.get(null);
        	newtuple.setField(0, new IntField(number));
        	tuplearray.add(newtuple);
        }
    	else{
        	for(Field fieldkey : m_aggmap.keySet()){
        		Tuple newtuple = new Tuple(m_td);
        		newtuple.setField(0, fieldkey);
        		int number = m_aggmap.get(fieldkey);
        		newtuple.setField(1, new IntField(number));
        		tuplearray.add(newtuple);
        	}
        	
        }
        return new TupleIterator(m_td,tuplearray);
    }

}
