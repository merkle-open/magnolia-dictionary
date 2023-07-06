package com.namics.oss.magnolia.dictionary.util;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
	private final Supplier<T> supplier;
	private T value;

	private Lazy(final Supplier<T> supplier) {
		this.supplier = supplier;
	}

	public static <T> Lazy<T> of(Supplier<T> supplier) {
		return new Lazy<>(supplier);
	}

	@Override
	public T get() {
		if(value == null) {
			value = supplier.get();
		}
		return value;
	}
}
