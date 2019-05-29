package dev.olog.msc.presentation.playlist.chooser.di

import dagger.Component
import dev.olog.msc.app.injection.AppComponent
import dev.olog.msc.app.injection.InjectionHelper
import dev.olog.msc.core.dagger.scope.PerFragment
import dev.olog.msc.presentation.playlist.chooser.PlaylistChooserActivity

@Component(
    modules = [
        PlaylistChooserActivityModule::class
    ], dependencies = [AppComponent::class]
)
@PerFragment
interface PlaylistChooserActivityComponent : InjectionHelper<PlaylistChooserActivity> {

    @Component.Factory
    interface Factory {

        fun create(component: AppComponent): PlaylistChooserActivityComponent
    }

}