package simpledb;

import java.util.*;

/**
 * The Aggregation operator that computes an aggregate (e.g., sum, avg, max,
 * min). Note that we only support aggregates over a single column, grouped by a
 * single column.
 */
public class Aggregate extends Operator {

    private static final long serialVersionUID = 1L;
    
    private DbIterator m_child;
    private int m_afield;
    private int m_gfield;
    private Aggregator.Op m_aop;
    
    private Aggregator m_agg;
    private DbIterator m_result;

    /**
     * Constructor.
     * 
     * Implementation hint: depending on the type of afield, you will want to
     * construct an {@link IntAggregator} or {@link StringAggregator} to help
     * you with your implementation of readNext().
     * 
     * 
     * @param child
     *            The DbIterator that is feeding us tuples.
     * @param afield
     *            The column over which we are computing an aggregate.
     * @param gfield
     *            The column over which we are grouping the result, or -1 if
     *            there is no grouping
     * @param aop
     *            The aggregation operator to use
     */
    public Aggregate(DbIterator child, int afield, int gfield, Aggregator.Op aop) {
	// some code goes here
    	m_child = child;
    	m_afield = afield;
    	m_gfield = gfield;
    	m_aop = aop;
    	
    	Type gfieldtype;
    	
    	if(m_gfield == Aggregator.NO_GROUPING){
    		gfieldtype = null;
    	}
    	else{
    		gfieldtype = m_child.getTupleDesc().getFieldType(m_gfield);
    	}
    	if(m_child.getTupleDesc().getFieldType(m_afield) == Type.INT_TYPE){
    		m_agg = new IntegerAggregator(m_gfield, gfieldtype, m_afield, m_aop);
    	}
    	else if(m_child.getTupleDesc().getFieldType(m_afield) == Type.STRING_TYPE){
    		m_agg = new StringAggregator(m_gfield, gfieldtype, m_afield, m_aop);
    	}
    	else{
    		System.out.println("uh oh");//should never reach here
    	}
    	
    	
    }

    /**
     * @return If this aggregate is accompanied by a groupby, return the groupby
     *         field index in the <b>INPUT</b> tuples. If not, return
     *         {@link simpledb.Aggregator#NO_GROUPING}
     * */
    public int groupField() {
	// some code goes here
    	
    
    	return m_gfield;
    }

    /**
     * @return If this aggregate is accompanied by a group by, return the name
     *         of the groupby field in the <b>OUTPUT</b> tuples If not, return
     *         null;
     * */
    public String groupFieldName() {
	// some code goes here
    	if(m_gfield == Aggregator.NO_GROUPING)
    		return null;
    	return m_child.getTupleDesc().getFieldName(m_gfield);
    }

    /**
     * @return the aggregate field
     * */
    public int aggregateField() {
	// some code goes here
	return m_afield;
    }

    /**
     * @return return the name of the aggregate field in the <b>OUTPUT</b>
     *         tuples
     * */
    public String aggregateFieldName() {
	// some code goes here
	return m_child.getTupleDesc().getFieldName(m_afield);
    }

    /**
     * @return return the aggregate operator
     * */
    public Aggregator.Op aggregateOp() {
	// some code goes here
	return m_aop;
    }

    public static String nameOfAggregatorOp(Aggregator.Op aop) {
    	return aop.toString();
    }

    public void open() throws NoSuchElementException, DbException,
	    TransactionAbortedException {
	// some code goes here
    	
    	m_child.open();
    	
    	while(m_child.hasNext()){
    		m_agg.mergeTupleIntoGroup(m_child.next());
    	}
    	m_result = m_agg.iterator();
    	m_result.open();
    	super.open();
    }

    /**
     * Returns the next tuple. If there is a group by field, then the first
     * field is the field by which we are grouping, and the second field is the
     * result of computing the aggregate, If there is no group by field, then
     * the result tuple should contain one field representing the result of the
     * aggregate. Should return null if there are no more tuples.
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
	// some code goes here
    	if(m_result.hasNext())
    		return m_result.next();
    	return null;
    }

    public void rewind() throws DbException, TransactionAbortedException {
	// some code goes here
    	m_child.rewind();
    	m_result.rewind();
    }

    /**
     * Returns the TupleDesc of this Aggregate. If there is no group by field,
     * this will have one field - the aggregate column. If there is a group by
     * field, the first field will be the group by field, and the second will be
     * the aggregate value column.
     * 
     * The name of an aggregate column should be informative. For example:
     * "aggName(aop) (child_td.getFieldName(afield))" where aop and afield are
     * given in the constructor, and child_td is the TupleDesc of the child
     * iterator.
     */
    public TupleDesc getTupleDesc() {
	// some code goes here
    	TupleDesc td;
    	String[] name;
    	Type[] type;
    	
    	if(m_gfield == Aggregator.NO_GROUPING){
    		name = new String[] {"aggName(" + m_aop.toString() + ") (" + 
    				m_child.getTupleDesc().getFieldName(m_afield) + ")"};
    		type = new Type[] {m_child.getTupleDesc().getFieldType(m_afield)};
    	}
    	else{
    		name = new String[] {m_child.getTupleDesc().getFieldName(m_gfield), 
    				"aggName(" + m_aop.toString() + ") (" + 
    				m_child.getTupleDesc().getFieldName(m_afield) + ")"};
    		type = new Type[] {m_child.getTupleDesc().getFieldType(m_gfield),
    				m_child.getTupleDesc().getFieldType(m_afield)};
    		
    	}
    	td = new TupleDesc(type, name);
    	return td;
    }

    public void close() {
		// some code goes here
    	m_child.close();
    	m_result.close();
    	super.close();
    }

    @Override
    public DbIterator[] getChildren() {
	// some code goes here
	return new DbIterator[] {m_child};
    }

    @Override
    public void setChildren(DbIterator[] children) {
	// some code goes here
    	m_child = children[0];
    }
    
}
