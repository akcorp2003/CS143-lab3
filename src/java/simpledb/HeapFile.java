package simpledb;

import java.io.*;
import java.util.*;

import static java.lang.System.out;
/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
	private File m_file;
	private TupleDesc m_td;
	
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
    	m_file = f;
    	m_td = td;
    }
    
    

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
    	return m_file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // utilizing the recommendation of hashing
        return m_file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return m_td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
    	try{
    		RandomAccessFile random_file = new RandomAccessFile(m_file, "r");
    		int offset = BufferPool.getPageSize() * pid.pageNumber();
    		byte[] page_data = new byte[BufferPool.getPageSize()];
    		//move the pointer to where the page is
    		random_file.seek(offset);
    		//read it
    		random_file.readFully(page_data);
    		random_file.close();
    		
    		//set up the new page
    		HeapPageId pagedata_id = new HeapPageId(pid.getTableId(), pid.pageNumber());
    		HeapPage page_with_data = new HeapPage(pagedata_id, page_data);
    		return page_with_data;
    	}
    	catch (FileNotFoundException e){
    		out.println(e.getMessage());
    	}
    	catch (IOException e){
    		out.println(e.getMessage());
    	}
    	
        
        return null;
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
    	try{
    		RandomAccessFile opened_page = new RandomAccessFile(m_file, "rw");
    		//the offset as defined in the file DbFile 
    		int offset = page.getId().pageNumber() * BufferPool.getPageSize();
    		opened_page.seek(offset);
    		opened_page.write(page.getPageData()); //do we need to use the other form?
    		opened_page.close();
    	}
    	catch (IOException e){
    		out.println(e.getMessage());
    	}
        // not necessary for lab1
    
    }
    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
    	int length = (int) (Math.ceil(m_file.length() / (BufferPool.getPageSize())));
        return length;
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
    	ArrayList<Page> newPageList = new ArrayList<Page>();
    	HeapPage page = null;
    	
    	for(int x = 0; x < numPages(); x++){
    		PageId pid = new HeapPageId(getId(), x);
    		HeapPage hp = (HeapPage) Database.getBufferPool().getPage(tid, pid, Permissions.READ_WRITE);
    		if(hp.getNumEmptySlots() > 0)
    			page = hp;
    	
    	}
    	if(page != null){
    		page.insertTuple(t);
    		
    		newPageList.add(page);
    		
    		return newPageList;
    	}
    	HeapPageId newId = new HeapPageId(getId(),numPages());
    	HeapPage newPage = new HeapPage(newId,HeapPage.createEmptyPageData());
    	
    	newPage.insertTuple(t);
    	newPageList.add(newPage);	
    	writePage(newPage);
    	
        return newPageList;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
    	
    	PageId pid = t.getRecordId().getPageId();
    	HeapPage hp = (HeapPage)Database.getBufferPool().getPage(tid, pid, Permissions.READ_WRITE);
        hp.deleteTuple(t);
        ArrayList<Page> pageList = new ArrayList<Page>();
        pageList.add(hp);
    	return pageList;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        HeapFileIterator hfi = new HeapFileIterator( tid);
        return hfi;
    }
    
    public class HeapFileIterator implements DbFileIterator {
    	// Remember that heapPages have their own iterators...
    	

    	Iterator<Tuple> it;
    	private int index = 0;
    	private boolean was_opened = false;
    	private TransactionId tid;
    	private HeapFile m_hp;
    	
    	
    	
    	public HeapFileIterator(TransactionId tid) {
    		this.tid = tid;
    		//m_hp = hp;
    	}
    	
    	
    	/**
    	 * Functionality
    	 * (non-Javadoc)
    	 * Open() has to make sure that the iterator is ready to go when the next reference
    	 * to it is called, that is, the iterator must be initialized.
    	 */
    	public void open() throws DbException, TransactionAbortedException {
    		if(was_opened)
    			return;
    	
    		was_opened = true;
    		HeapPageId hpi = new HeapPageId(getId(), index);
    		HeapPage p = (HeapPage)(Database.getBufferPool().getPage(tid,hpi,Permissions.READ_WRITE));
    	
    		it = p.iterator();
    		//assembleTuples()
    		
    	}
    	
    	public boolean hasNext() throws DbException, TransactionAbortedException {
    		if(!was_opened)
    			return false;
    		if (it == null)
    			return false;
    		
    		
    		
    		
    		
    	
    		if (index < (numPages() - 1)){
    			
    			//we need to assemble the number of tuples that are on the next page
    			HeapPageId temppageid = new HeapPageId(getId(), index+1);
        		HeapPage p = (HeapPage)Database.getBufferPool().getPage(tid,temppageid,Permissions.READ_ONLY); 
        		ArrayList<Tuple> listoftuples = new ArrayList<Tuple>();
        		
        		//get all the tuples that are on the next page
        		Iterator<Tuple> it_temp = p.iterator();
        		
        		while(it_temp.hasNext()){
        			listoftuples.add(it_temp.next());
        		}
    			if(listoftuples.size() != 0)
    				return true;
    			else
    				return false;
    		}
    			
    		if (it.hasNext())
    			return true;
    		
    		return false;
    	}
    	/**
    	 * Since each page has a series of tuples, we first feed those tuples to the 
    	   requestor before we move onto the next page
    	 */
    	public Tuple next() throws DbException,TransactionAbortedException,NoSuchElementException {
    		if(hasNext()) {
    			//if the current file still has HeapPages left in it...
    			if (it.hasNext())
    				return it.next();
    			
    			//the current file has run out of tuples, load the next one in.
    			else /*if(!it.hasNext() && index < numPages()-1 )*/{
    				
    				HeapPageId hpi = new HeapPageId(getId(), ++index);
    				HeapPage p = (HeapPage)Database.getBufferPool().getPage(tid,hpi,Permissions.READ_WRITE);
    				it = p.iterator();

    				return it.next();
    			}

    		}
    		else
    			throw new NoSuchElementException();
    		
    		//return it.next();
    	}
    	public void rewind() throws DbException, TransactionAbortedException {
    		index = 0;
    		was_opened = false;
    		open();
    	}
    	
    	public void close(){
    		index = 0;
    		was_opened = false;
    	}

    	
    }
    
}


/*
class HeapFileIterator implements DbFileIterator{
	
	Iterator<Tuple> it;
	
	private int index = -1;
	private boolean was_opened = false;
	
	private HeapFile m_heapfile;
	private TransactionId m_tid;
	
	public HeapFileIterator(HeapFile the_heap, TransactionId tid){
		m_heapfile = the_heap;
		m_tid = tid;
	}

	public void open() throws DbException, TransactionAbortedException{
		if(was_opened)
			return; //whack the user back
		else{
			was_opened = true;
			index = 0; //prepare the index for searching
			HeapPageId hpi = new HeapPageId(m_heapfile.getId(), index);
			HeapPage the_heappage = (HeapPage)(Database.getBufferPool().getPage(m_tid, hpi, Permissions.READ_ONLY));
			
			if(the_heappage == null){
				throw new NullPointerException(); 
			}
				
				
			it = the_heappage.iterator();
			index++;
		}
			
	}
	
	public boolean hasNext(){
		if(index < 0) //just in case index was mutilated by something  
			return false;
		if(!was_opened)
			return false;
		if(it == null)
			return false;
		if(it.hasNext())
			return true;
		if(index < m_heapfile.numPages() -1) //make sure we haven't gone over the page limit  
			return true;
		else 
			return false;
	}

	 
	public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException{
		if(hasNext()){
			//if the current file still has HeapPages left in it...
			if(it.hasNext()){
				return it.next();
			}
			else{
				//the current page has run out of tuples, load the next page in
				HeapPageId hpi = new HeapPageId(m_heapfile.getId(), index);
				HeapPage the_heappage = (HeapPage)(Database.getBufferPool().getPage(m_tid, hpi, Permissions.READ_ONLY));
				
				if(the_heappage == null){
					throw new NoSuchElementException(); 
				}
					
					
				it = the_heappage.iterator();
				index++;
			}
			
				
		}
		else{
			throw new NoSuchElementException();
		}
		
		
			
			
		return it.next();
		
	}
	
	public void rewind(){
		//reset the index
		index = -1;
	}
	
	public void close(){
		index = -1;
		was_opened = false;
	}
}*/

