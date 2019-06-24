package dev.olog.msc.presentation.edit.track.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dev.olog.presentation.dagger.ViewModelKey
import dev.olog.presentation.dagger.FragmentLifecycle
import dev.olog.msc.presentation.edit.track.EditTrackFragment
import dev.olog.msc.presentation.edit.track.EditTrackFragmentViewModel
import dev.olog.core.MediaId

@Module(includes = [EditTrackFragmentModule.Binding::class])
class EditTrackFragmentModule(
        private val fragment: EditTrackFragment

) {

    @Provides
    fun provideMediaId(): MediaId {
        val mediaId = fragment.arguments!!.getString(EditTrackFragment.ARGUMENTS_MEDIA_ID)!!
        return MediaId.fromString(mediaId)
    }

    @Provides
    @FragmentLifecycle
    fun provideLifecycle() = fragment.lifecycle

    @Module
    interface Binding {

        @Binds
        @IntoMap
        @ViewModelKey(EditTrackFragmentViewModel::class)
        fun provideViewModel(viewModel: EditTrackFragmentViewModel): ViewModel

    }


}