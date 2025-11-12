package com.merkle.oss.magnolia.dictionary.util.predicates;

import com.merkle.oss.magnolia.dictionary.util.NodeUtil;
import org.apache.jackrabbit.commons.predicate.Predicate;

import javax.jcr.Node;
import java.util.List;
import java.util.Set;

public class NodeNameFilteringPredicate implements Predicate {
	private final Set<String> filterNames;

	public NodeNameFilteringPredicate(final Set<String> filterNames) {
		this.filterNames = filterNames;
	}

	@Override
	public boolean evaluate(final Object o) {
		if (o instanceof Node node) {
			String nodeName = NodeUtil.getName(node);
			return !filterNames.contains(nodeName);
		}
		return false;
	}
}
