package dev.olog.msc.domain.interactor.all

import dev.olog.msc.core.entity.podcast.PodcastPlaylist
import dev.olog.msc.domain.executors.ComputationScheduler
import dev.olog.msc.core.gateway.PodcastPlaylistGateway
import dev.olog.msc.domain.interactor.base.GetGroupUseCase
import javax.inject.Inject

class GetAllPodcastPlaylistUseCase @Inject constructor(
        gateway: PodcastPlaylistGateway,
        schedulers: ComputationScheduler
) : GetGroupUseCase<PodcastPlaylist>(gateway, schedulers)