package togos.repository;

public class UnsupportedSchemeException extends Exception
{
	private static final long serialVersionUID = 1L;

	public UnsupportedSchemeException( String message ) {
		super(message);
	}
}
