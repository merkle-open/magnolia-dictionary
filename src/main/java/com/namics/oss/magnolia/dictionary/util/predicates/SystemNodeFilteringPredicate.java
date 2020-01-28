package com.namics.oss.magnolia.dictionary.util.predicates;

import com.namics.oss.magnolia.dictionary.util.NodeUtil;
import info.magnolia.jcr.util.NodeTypes;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.predicate.Predicate;

import javax.jcr.Node;

public class SystemNodeFilteringPredicate implements Predicate {
	@Override
	public boolean evaluate(Object o) {
		if (o instanceof Node) {
			Node node = (Node) o;
			return !StringUtils.startsWithAny(NodeUtil.getName(node),
					NodeTypes.JCR_PREFIX,
					NodeTypes.REP_PREFIX,
					NodeTypes.MGNL_PREFIX
			);
		}
		return false;
	}
}
