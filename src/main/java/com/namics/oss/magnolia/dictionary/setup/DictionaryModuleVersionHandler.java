package com.namics.oss.magnolia.dictionary.setup;

import com.namics.mgnl.commons.module.EnhancedModuleVersionHandler;
import com.namics.oss.magnolia.dictionary.setup.tasks.InstallMultiTextFieldTask;
import com.namics.oss.magnolia.dictionary.setup.tasks.InstallRootActivationCommandTask;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.namics.mgnl.commons.utils.CollectionUtils.asList;

/**
 * @author ploetscher, Namics AG
 * @since 31.03.2016
 */
public class DictionaryModuleVersionHandler extends EnhancedModuleVersionHandler {

    public DictionaryModuleVersionHandler() {
        register(DeltaBuilder.update("1.0.0", "Updates for version 1.0.0"));
    }

    @Nullable
    @Override
    protected List<? extends Task> getEarlyInstallTasks(@Nonnull InstallContext installContext, @Nonnull Version forVersion) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    protected List<? extends Task> getInstallOnlyTasks(@Nonnull InstallContext installContext, @Nonnull Version forVersion) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    protected List<? extends Task> getInstallAndUpdateTasks(@Nonnull InstallContext installContext, @Nonnull Version forVersion) {
        return asList(
                new InstallMultiTextFieldTask(),
                new InstallRootActivationCommandTask()
        );
    }

    @Nullable
    @Override
    protected List<? extends Task> getUpdateOnlyTasks(@Nonnull InstallContext installContext, @Nonnull Version forVersion) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    protected List<? extends Task> getModuleStartupTasks(@Nonnull InstallContext installContext, @Nonnull Version forVersion) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    protected List<? extends Task> getSnapshotStartupTasks(@Nonnull InstallContext installContext, @Nonnull Version forVersion) {
        final List<Task> startupTasks = new LinkedList<Task>();

        // execute all general update tasks on snapshot
        startupTasks.addAll(Objects.requireNonNull(getInstallAndUpdateTasks(installContext, forVersion)));
        startupTasks.addAll(Objects.requireNonNull(getUpdateOnlyTasks(installContext, forVersion)));

        return startupTasks;
    }

    @Nullable
    @Override
    protected List<? extends Task> getNamicsStartupTasks(@Nonnull InstallContext installContext, @Nonnull Version forVersion) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    protected List<? extends Task> getNamicsOrLocalDevelopmentStartupTasks(@Nonnull InstallContext installContext, @Nonnull Version forVersion) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    protected List<? extends Task> getLocalDevelopmentStartupTasks(@Nonnull InstallContext installContext, @Nonnull Version forVersion) {
        return getSnapshotStartupTasks(installContext, forVersion);
    }
}
