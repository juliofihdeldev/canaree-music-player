package dev.olog.msc.presentation.mini.player

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dev.olog.msc.domain.interactor.music.service.ToggleSkipToNextVisibilityUseCase
import dev.olog.msc.domain.interactor.music.service.ToggleSkipToPreviousVisibilityUseCase
import dev.olog.shared_android.music_service.IRxMusicServiceControllerCallback
import javax.inject.Inject

class MiniPlayerFragmentViewModelFactory @Inject constructor(
        private val controllerCallback: IRxMusicServiceControllerCallback,
        private val toggleSkipToPreviousVisibilityUseCase: ToggleSkipToPreviousVisibilityUseCase,
        private val toggleSkipToNextVisibilityUseCase: ToggleSkipToNextVisibilityUseCase

) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MiniPlayerFragmentViewModel(controllerCallback,
                toggleSkipToPreviousVisibilityUseCase, toggleSkipToNextVisibilityUseCase
        ) as T
    }

}