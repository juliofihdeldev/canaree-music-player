package dev.olog.msc.domain.interactor.favorite

import dev.olog.core.entity.favorite.FavoriteEnum
import dev.olog.core.executor.IoScheduler
import dev.olog.core.gateway.FavoriteGateway
import dev.olog.msc.domain.interactor.base.ObservableUseCase
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