package togos.repository;

public interface Getter<T>
{
	/**
	 * @param name
	 * @return
	 * @throws ResourceNotFoundException, ExecutionException, InterruptedException, etc
	 */
	public T get(String name) throws ResourceNotFoundException;
}
