package com.merkle.oss.magnolia.dictionary.util.predicates;

import info.magnolia.jcr.util.PropertyUtil;

import java.util.Set;

import javax.jcr.Node;

import org.apache.jackrabbit.commons.predicate.Predicate;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;

public class LabelNameFilteringPredicate implements Predicate {
	private final Set<String> filterNames;

	public LabelNameFilteringPredicate(final Set<String> filterNames) {
		this.filterNames = filterNames;
	}

	@Override
	public boolean evaluate(final Object o) {
		if (o instanceof Node node) {
			String nodeName = PropertyUtil.getString(node, DictionaryConfiguration.Prop.NAME);
			return !filterNames.contains(nodeName);
		}
		return false;
	}
}
