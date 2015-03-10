package togos.repository;

public class ResourceNotFoundException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	protected final String resourceName;
	
	public ResourceNotFoundException( String resourceName ) {
		this.resourceName = resourceName;
	}
	public ResourceNotFoundException( String resourceName, String message ) {
		super(message);
		this.resourceName = resourceName;
	}
	public ResourceNotFoundException( String resourceName, String message, Throwable cause ) {
		super(message, cause);
		this.resourceName = resourceName;
	}
	
	public String getResourceName() {
		return resourceName;
	}
}
