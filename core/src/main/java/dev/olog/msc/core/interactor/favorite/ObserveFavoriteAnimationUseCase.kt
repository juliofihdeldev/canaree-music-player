package dev.olog.msc.core.interactor.favorite

import dev.olog.msc.core.entity.favorite.FavoriteEnum
import dev.olog.msc.core.executors.IoScheduler
import dev.olog.msc.core.gateway.FavoriteGateway
import dev.olog.msc.core.interactor.base.ObservableUseCase
import io.reactivex.Observable
import javax.inject.Inject

class ObserveFavoriteAnimationUseCase @Inject constructor(
        scheduler: IoScheduler,
        private val gateway: FavoriteGateway

) : ObservableUseCase<FavoriteEnum>(scheduler) {

    override fun buildUseCaseObservable(): Observable<FavoriteEnum> {
        return gateway.observeToggleFavorite()
    }
}