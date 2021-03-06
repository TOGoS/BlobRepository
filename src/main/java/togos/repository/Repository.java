package togos.repository;

public interface Repository<T> extends Getter<T>
{
	public boolean contains( String urn );
	/**
	 * Attempt to read the data from the given input stream and write it named with the given URN.
	 * This method should *always* call 'close' on the InputStream before returning,
	 * even if there is an exception.
	 */
	public void put( String urn, T blob )
		throws UnsupportedSchemeException, HashMismatchException, UnsuitablePayloadException, StoreException;
	public String store( T blob ) throws UnsuitablePayloadException, StoreException;
	public T get( String urn ) throws ResourceNotFoundException;
}
