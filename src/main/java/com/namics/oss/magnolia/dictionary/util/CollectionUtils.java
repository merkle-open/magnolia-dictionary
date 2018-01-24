package com.namics.oss.magnolia.dictionary.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author haug, Namics AG
 */
public class CollectionUtils {

	public static <T> List<T> asList(final T... array) {
		return new ArrayList<T>(Arrays.asList(array));
	}
}
