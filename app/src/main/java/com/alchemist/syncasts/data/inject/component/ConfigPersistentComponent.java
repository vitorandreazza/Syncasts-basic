package com.alchemist.syncasts.data.inject.component;

import com.alchemist.syncasts.data.inject.ConfigPersistentScope;
import com.alchemist.syncasts.data.inject.module.ActivityModule;

import dagger.Component;

@ConfigPersistentScope
@Component(dependencies = ApplicationComponent.class)
public interface ConfigPersistentComponent {

    ActivitySubComponent activityComponent(ActivityModule activityModule);
}
