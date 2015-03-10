package togos.repository;

/**
 * {@link Repository#put(String, Object)} may throw this to indicate
 * that it refuses to store the given object for some arbitrary reason
 * related to the data being stored.
 * 
 * e.g. if the repository can only contain blobs that are of a certain size
 * or are formatted a certain way.
 */
public class UnsuitablePayloadException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public UnsuitablePayloadException( String message ) {
		super(message);
	}
}
