package simpledb;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Inserts tuples read from the child operator into the tableid specified in the
 * constructor
 */
public class Insert extends Operator {

    private static final long serialVersionUID = 1L;

    private TransactionId m_tid;
    private DbIterator m_child;
    private int m_tableid;
    
    private TupleDesc m_td;
    private boolean inserted = false;
    
    /**
     * Constructor.
     * 
     * @param t
     *            The transaction running the insert.
     * @param child
     *            The child operator from which to read tuples to be inserted.
     * @param tableid
     *            The table in which to insert tuples.
     * @throws DbException
     *             if TupleDesc of child differs from table into which we are to
     *             insert.
     */
    public Insert(TransactionId t,DbIterator child, int tableid)
            throws DbException {
        // some code goes here
    	m_tid = t;
    	m_child = child;
    	m_tableid = tableid;

    	
    	Type [] type = new Type[] {Type.INT_TYPE};
    	String [] string = new String[] {"No. of inserted tuples"};
    	TupleDesc newTd = new TupleDesc(type,string);
    	m_td = newTd;
    }

    public TupleDesc getTupleDesc() {
        // some code goes here
        return m_td;
    }

    public void open() throws DbException, TransactionAbortedException {
        // some code goes here
    	super.open();
    	m_child.open();
    }

    public void close() {
        // some code goes here
    	m_child.close();
    	super.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
    	m_child.rewind();
    }

    /**
     * Inserts tuples read from child into the tableid specified by the
     * constructor. It returns a one field tuple containing the number of
     * inserted records. Inserts should be passed through BufferPool. An
     * instances of BufferPool is available via Database.getBufferPool(). Note
     * that insert DOES NOT need check to see if a particular tuple is a
     * duplicate before inserting it.
     * 
     * @return A 1-field tuple containing the number of inserted records, or
     *         null if called more than once.
     * @see Database#getBufferPool
     * @see BufferPool#insertTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
    	int count = 0;
    	if(inserted)
    		return null;
    	inserted = true;
    	
    	while(m_child.hasNext()){
    		try {
				Database.getBufferPool().insertTuple(m_tid, m_tableid, m_child.next());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				throw new DbException("Could not insert tuple");
			}
    		count++;
    	}
    	
    	
    	Tuple resultTuple = new Tuple(m_td);
    	resultTuple.setField(0, new IntField(count));
    	
        return resultTuple;
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
