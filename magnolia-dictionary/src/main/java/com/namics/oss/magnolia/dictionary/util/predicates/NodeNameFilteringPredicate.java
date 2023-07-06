package com.namics.oss.magnolia.dictionary.util.predicates;

import com.namics.oss.magnolia.dictionary.util.NodeUtil;
import org.apache.jackrabbit.commons.predicate.Predicate;

import javax.jcr.Node;
import java.util.List;

public class NodeNameFilteringPredicate implements Predicate {

	private List<String> filterNames;

	public NodeNameFilteringPredicate(List<String> filterNames) {
		this.filterNames = filterNames;
	}

	@Override
	public boolean evaluate(Object o) {
		if (o instanceof Node) {
			Node node = (Node) o;
			String nodeName = NodeUtil.getName(node);
			return !filterNames.contains(nodeName);
		}
		return false;
	}
}
