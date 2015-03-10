package togos.repository;

/**
 * Something that goes wrong behind the scenes
 * while trying to store stuff in a repository.
 */
public class StoreException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public StoreException( String message ) { super(message); }
	public StoreException( Throwable cause ) { super(cause); }
	public StoreException( String message, Throwable cause ) { super(message, cause); }
}
