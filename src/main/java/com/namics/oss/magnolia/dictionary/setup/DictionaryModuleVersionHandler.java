package com.namics.oss.magnolia.dictionary.setup;

import com.namics.oss.magnolia.dictionary.setup.tasks.InstallMultiTextFieldTask;
import com.namics.oss.magnolia.dictionary.setup.tasks.InstallRootActivationCommandTask;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;

import java.util.LinkedList;
import java.util.List;

/**
 * @author ploetscher, Namics AG
 * @since 31.03.2016
 */
public class DictionaryModuleVersionHandler extends DefaultModuleVersionHandler {

	private List<Task> tasks = new LinkedList<>();
	private static final String SNAPSHOT_CLASSIFIER = "SNAPSHOT";

	public DictionaryModuleVersionHandler() {
		tasks.add(new InstallMultiTextFieldTask());
		tasks.add(new InstallRootActivationCommandTask());
	}

	@Override
	protected final List<Task> getExtraInstallTasks(InstallContext installContext) { //when module node does not exist
		List<Task> installTasks = new LinkedList<Task>();


		installTasks.addAll(super.getExtraInstallTasks(installContext));

		installTasks.addAll(tasks);


		return installTasks;
	}

	@Override
	protected final List<Task> getDefaultUpdateTasks(Version forVersion) { //on every module update
		List<Task> installTasks = new LinkedList<Task>();


		installTasks.addAll(super.getDefaultUpdateTasks(forVersion));

		installTasks.addAll(tasks);


		return installTasks;
	}

	@Override
	protected final List<Task> getStartupTasks(InstallContext installContext) {
		List<Task> installTasks = new LinkedList<Task>();
		Version forVersion = getVersionFromInstallContext(installContext);


		if (isSnapshot(forVersion)) {
			installTasks.addAll(tasks);
		}


		return installTasks;
	}

	private static boolean isSnapshot(Version version) {
		return SNAPSHOT_CLASSIFIER.equalsIgnoreCase(version.getClassifier());
	}

	private static Version getVersionFromInstallContext(InstallContext installContext) {
		return installContext.getCurrentModuleDefinition().getVersion();
	}

}
