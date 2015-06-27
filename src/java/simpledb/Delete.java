package simpledb;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * The delete operator. Delete reads tuples from its child operator and removes
 * them from the table they belong to.
 */
public class Delete extends Operator {

    private static final long serialVersionUID = 1L;
    
    private TransactionId m_tid;
    private DbIterator m_child;
    
    private TupleDesc m_td;
    private boolean deleted = false;

    /**
     * Constructor specifying the transaction that this delete belongs to as
     * well as the child to read from.
     * 
     * @param t
     *            The transaction this delete runs in
     * @param child
     *            The child operator from which to read tuples for deletion
     */
    public Delete(TransactionId t, DbIterator child) {
        // some code goes here
    	m_tid = t;
    	m_child = child;
    	
    	Type [] type = new Type[] {Type.INT_TYPE};
    	String [] string = new String[] {"No. of deleted tuples"};
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
     * Deletes tuples as they are read from the child operator. Deletes are
     * processed via the buffer pool (which can be accessed via the
     * Database.getBufferPool() method.
     * 
     * @return A 1-field tuple containing the number of deleted records.
     * @see Database#getBufferPool
     * @see BufferPool#deleteTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
    	int count = 0;
    	if(deleted)
    		return null;
    	deleted = true;
    	
    	while(m_child.hasNext()){
    		try {
				Database.getBufferPool().deleteTuple(m_tid, m_child.next());
			} catch (NoSuchElementException e) {
				// TODO Auto-generated catch block
				throw new DbException("Tuple was not found");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new DbException("Could not delete tuple");
			}
    		count++;
    	}
    	
    	Tuple resultTuple = new Tuple(m_td);
    	resultTuple.setField(0,new IntField(count));
    	
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
