package dev.olog.core.interactor

import dev.olog.core.MediaId
import dev.olog.core.MediaIdCategory
import dev.olog.core.gateway.podcast.PodcastAlbumGateway
import dev.olog.core.gateway.podcast.PodcastArtistGateway
import dev.olog.core.gateway.podcast.PodcastGateway
import dev.olog.core.gateway.podcast.PodcastPlaylistGateway
import dev.olog.core.gateway.track.*
import dev.olog.core.interactor.base.FlowUseCaseWithParam
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetItemTitleUseCase @Inject constructor(
    private val folderGateway: FolderGateway,
    private val playlistGateway: PlaylistGateway,
    private val albumGateway: AlbumGateway,
    private val artistGateway: ArtistGateway,
    private val genreGateway: GenreGateway,

    private val podcastPlaylistGateway: PodcastPlaylistGateway,
    private val podcastAlbumGateway: PodcastAlbumGateway,
    private val podcastArtistGateway: PodcastArtistGateway

) : FlowUseCaseWithParam<String?, MediaId>() {


    override fun buildUseCase(param: MediaId): Flow<String?> {
        return when (param.category){
            MediaIdCategory.FOLDERS -> folderGateway.observeByParam(param.categoryValue).map { it?.title }
            MediaIdCategory.PLAYLISTS -> playlistGateway.observeByParam(param.categoryId).map { it?.title }
            MediaIdCategory.ALBUMS -> albumGateway.observeByParam(param.categoryId).map { it?.title }
            MediaIdCategory.ARTISTS -> artistGateway.observeByParam(param.categoryId).map { it?.name }
            MediaIdCategory.GENRES -> genreGateway.observeByParam(param.categoryId).map { it?.name }
            MediaIdCategory.PODCASTS_PLAYLIST -> podcastPlaylistGateway.observeByParam(param.categoryId).map { it?.title }
            MediaIdCategory.PODCASTS_ARTISTS -> podcastArtistGateway.observeByParam(param.categoryId).map { it?.name }
            MediaIdCategory.PODCASTS_ALBUMS -> podcastAlbumGateway.observeByParam(param.categoryId).map { it?.title }
            else -> throw IllegalArgumentException("invalid media category ${param.category}")
        }
    }

}