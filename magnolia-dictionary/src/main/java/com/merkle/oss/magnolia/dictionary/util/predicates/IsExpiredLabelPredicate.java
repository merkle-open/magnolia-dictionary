package com.merkle.oss.magnolia.dictionary.util.predicates;

import java.util.function.Predicate;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.ValueConverter;
import com.merkle.oss.magnolia.powernode.predicate.IsPrimaryNodeType;

public class IsExpiredLabelPredicate extends IsPrimaryNodeType<PowerNode> implements Predicate<PowerNode> {

	public IsExpiredLabelPredicate() {
		super(DictionaryConfiguration.LABEL_NODE_TYPE);
	}

	@Override
	public boolean test(final PowerNode node) {
		return super.test(node) && node
				.getProperty(DictionaryConfiguration.Prop.EXPIRED, ValueConverter::getBoolean)
				.orElse(false);
	}
}
