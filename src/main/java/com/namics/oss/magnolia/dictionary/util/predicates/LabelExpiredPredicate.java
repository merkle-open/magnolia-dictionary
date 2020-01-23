package com.namics.oss.magnolia.dictionary.util.predicates;

import com.namics.oss.magnolia.dictionary.DictionaryConfiguration;
import info.magnolia.jcr.util.PropertyUtil;
import org.apache.jackrabbit.commons.predicate.Predicate;

import javax.jcr.Node;

public class LabelExpiredPredicate implements Predicate {
	@Override
	public boolean evaluate(Object o) {
		if (o instanceof Node) {
			Node node = (Node) o;
			return PropertyUtil.getBoolean(node, DictionaryConfiguration.Prop.EXPIRED, false);
		}
		return false;
	}
}
