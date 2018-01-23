package com.namics.oss.magnolia.dictionary.commands;

import com.namics.mgnl.commons.utils.NodeUtil;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.context.Context;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.publishing.command.PublicationCommand;
import info.magnolia.publishing.sender.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;

/**
 * RootPublicationCommand.
 *
 * @author mrauch, Namics AG
 * @since 18.10.2016
 */
public class RootPublicationCommand extends PublicationCommand {

	private static final Logger log = LoggerFactory.getLogger(RootPublicationCommand.class);

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
