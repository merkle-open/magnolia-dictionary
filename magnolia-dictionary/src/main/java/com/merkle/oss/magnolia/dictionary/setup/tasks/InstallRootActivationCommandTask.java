package com.merkle.oss.magnolia.dictionary.setup.tasks;

import com.merkle.oss.magnolia.dictionary.commands.RootPublicationCommand;
import info.magnolia.jcr.nodebuilder.task.ErrorHandling;
import info.magnolia.jcr.nodebuilder.task.NodeBuilderTask;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.RepositoryException;

import static com.merkle.oss.magnolia.dictionary.util.NodeUtil.setOrAddProperty;
import static info.magnolia.jcr.nodebuilder.Ops.getOrAddNode;

public class InstallRootActivationCommandTask extends AbstractRepositoryTask {

	public static final String MODULE_NAME = "publishing-core";

	public InstallRootActivationCommandTask() {
		super(
				"Install dictionary root activation command",
				"Install dictionary root activation command"
		);
	}

	@Override
	protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
		new NodeBuilderTask("", "", ErrorHandling.logging, RepositoryConstants.CONFIG,
				"/modules",
				getOrAddNode(MODULE_NAME, NodeTypes.Content.NAME).then(
						getOrAddNode("commands", NodeTypes.Content.NAME).then(
								getOrAddNode("versioned", NodeTypes.Content.NAME).then(
										getOrAddNode("publishAll", NodeTypes.ContentNode.NAME).then(
												setOrAddProperty("class", RootPublicationCommand.class.getName())
										)
								)
						)
				)
		).execute(installContext);
	}
}