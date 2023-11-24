package main.options;

public abstract class Option<T> {
	private T value;
	protected Option(T initial_value) {
		this.value = initial_value;
	}
	
	public abstract String name();
	public T value() {
		return value;
	}
	@SuppressWarnings("unchecked")
	public T set(Object new_value) {
		return this.value = (T) new_value;
	}
	
	public abstract String desc();
	public abstract Class<?> type();
}
