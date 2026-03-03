package com.merkle.oss.magnolia.dictionary.util.predicates;

import java.util.Set;
import java.util.function.Predicate;

import com.merkle.oss.magnolia.dictionary.DictionaryConfiguration;
import com.merkle.oss.magnolia.powernode.PowerNode;
import com.merkle.oss.magnolia.powernode.ValueConverter;
import com.merkle.oss.magnolia.powernode.predicate.IsPrimaryNodeType;

public class IsLabelNamesAnyOfPredicate extends IsPrimaryNodeType<PowerNode> implements Predicate<PowerNode> {
	private final Set<String> labelNames;

	public IsLabelNamesAnyOfPredicate(final Set<String> labelNames) {
		super(DictionaryConfiguration.LABEL_NODE_TYPE);
		this.labelNames = labelNames;
	}

	@Override
	public boolean test(final PowerNode node) {
		return super.test(node) && node
				.getProperty(DictionaryConfiguration.Prop.NAME, ValueConverter::getString)
				.map(labelNames::contains)
				.orElse(false);
	}
}
