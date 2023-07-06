package com.namics.oss.magnolia.dictionary.setup.tasks;

import com.namics.oss.magnolia.dictionary.field.MultiTextFieldDefinition;
import com.namics.oss.magnolia.dictionary.field.MultiTextFieldFactory;
import info.magnolia.jcr.nodebuilder.task.ErrorHandling;
import info.magnolia.jcr.nodebuilder.task.NodeBuilderTask;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.RepositoryException;

import static com.namics.oss.magnolia.dictionary.util.NodeUtil.setOrAddProperty;
import static info.magnolia.jcr.nodebuilder.Ops.getOrAddNode;

/**
 * @author ploetscher, Namics AG
 * @since 31.03.2016
 */
public class InstallMultiTextFieldTask extends AbstractRepositoryTask {

	public static final String MODULE_NAME = "magnolia-dictionary";

	public InstallMultiTextFieldTask() {
		super("", "");
	}

	@Override
	protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
		new NodeBuilderTask("Install MultiTextField", "", ErrorHandling.logging, RepositoryConstants.CONFIG,
				"/modules",
				getOrAddNode(MODULE_NAME, NodeTypes.Content.NAME).then(
						getOrAddNode("fieldTypes", NodeTypes.Content.NAME).then(
								getOrAddNode("multi", NodeTypes.ContentNode.NAME).then(
										setOrAddProperty("definitionClass", MultiTextFieldDefinition.class.getName()),
										setOrAddProperty("factoryClass", MultiTextFieldFactory.class.getName())
								)
						)
				)

		).execute(installContext);
	}
}