package simpledb;

import java.io.Serializable;

import java.util.*;


/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    /**
     * A help class to facilitate organizing the information of each field
     * */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         * */
        public final Type fieldType;
        
        /**
         * The name of the field
         * */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }
    
    private TDItem[] TDItemList;
    private int length;
    

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */
    public Iterator<TDItem> iterator() {
    	Iterator<TDItem> it = new Iterator<TDItem>(){

    		int index = 0;
    		@Override
    		public boolean hasNext() {
    			if(index < TDItemList.length)
    				return true;
    			else
    				return false;
    		}

    		@Override
    		public TDItem next() {
    			if(hasNext()){
    				//index++;
    				return TDItemList[index++];
    			}
    			else
    				throw new NullPointerException();
    		}

    		@Override
    		public void remove() {

    		}
    		
    	};
        return it;
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        // some code goes here
    	int len = typeAr.length;
    	TDItemList = new TDItem[len];
    	for(int x = 0; x < len; x++){
    		TDItemList[x] = new TDItem(typeAr[x],fieldAr[x]);
    	}
    	length = len;
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        // some code goes here

    	int len = typeAr.length;
    	TDItemList = new TDItem[len];
    	for(int x = 0; x < len; x++){
    		TDItemList[x] = new TDItem(typeAr[x],null);
    	}
    	length = len;
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        // some code goes here
        return this.length;
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // some code goes here
    	

    	String name = TDItemList[i].fieldName;
    	
        return name;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     * 
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        // some code goes here
    	Type type = TDItemList[i].fieldType;
    	
    	if(type == null){
    		throw new NoSuchElementException();
    	}
        return type;
        
    }

    /**
     * Find the index of the field with a given name.
     * 
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        for(int x = 0; x < length; x++){
        	if(TDItemList[x].fieldName != null && TDItemList[x].fieldName.equals(name))
        		return x;
        }
        throw new NoSuchElementException();
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        
    	int size = 0;
    	for(int x = 0; x < length; x++){
    		size += TDItemList[x].fieldType.getLen();
    	}
        return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     * 
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        int size1 = td1.numFields();
        int size2 = td2.numFields();
    	int totalSize = size1+size2;
    	Type[] mergeListType = new Type[totalSize];
    	String[] mergeListStrings = new String[totalSize];
    	int index;
    	Iterator<TDItem> it1 = td1.iterator();
    	Iterator<TDItem> it2 = td2.iterator();
    	for(index = 0; index < size1; index++){
    		mergeListStrings[index] = td1.getFieldName(index);
    		mergeListType[index] = td1.getFieldType(index);
    			
    	}
    	for(;index < totalSize; index++){

    		mergeListType[index] = td2.getFieldType(index-size1);
    		mergeListStrings[index] = td2.getFieldName(index-size1);

    	}
    	
    	TupleDesc merged = new TupleDesc(mergeListType,mergeListStrings);
        return merged;
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they are the same size and if the n-th
     * type in this TupleDesc is equal to the n-th type in td.
     * 
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    public boolean equals(Object o) {
        if(!(o instanceof TupleDesc))
        	return false;
        
        TupleDesc td = (TupleDesc) o;
        int tdSize = td.numFields();
        if(tdSize != length)
        	return false;
        
        for(int x = 0; x < length; x++){
        	if(!td.getFieldType(x).equals(getFieldType(x)))
        		return false;
        	
        }
        return true;

    	
    	
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * 
     * @return String describing this descriptor.
     */
    public String toString() {
        String stuff = "";
        for(int x = 0; x < length; x++){
        	stuff += TDItemList[x].fieldType.toString();
        	stuff += "("+TDItemList[x].fieldName+")";	
        }
        return stuff;
    }
}
