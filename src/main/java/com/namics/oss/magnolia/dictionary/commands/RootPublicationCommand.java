package com.namics.oss.magnolia.dictionary.commands;

import com.namics.oss.magnolia.dictionary.util.NodeUtil;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.context.Context;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.publishing.command.PublicationCommand;
import info.magnolia.publishing.sender.Sender;

import javax.jcr.Node;

/**
 * @author mrauch, Namics AG
 * @since 18.10.2016
 */
public class RootPublicationCommand extends PublicationCommand {

	public RootPublicationCommand(VersionManager versionManager, ComponentProvider componentProvider) {
		super(versionManager, componentProvider);
	}

	@Override
	public boolean execute(Context ctx) throws Exception {
		final Node originalState = getJCRNode(ctx);
		Sender sender = Components.getComponentProvider().newInstance(Sender.class);
		sender.publish(NodeUtil.asList(originalState.getNodes()), getRule());
		return true;
	}
}
