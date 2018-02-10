package dev.olog.msc.data.repository

import android.content.ContentResolver
import android.content.Context
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns.*
import android.provider.MediaStore.Audio.Media.DURATION
import android.provider.MediaStore.Audio.Media.TITLE
import com.squareup.sqlbrite3.BriteContentResolver
import dev.olog.msc.dagger.ApplicationContext
import dev.olog.msc.data.mapper.toSong
import dev.olog.msc.data.mapper.toUneditedSong
import dev.olog.msc.domain.entity.Song
import dev.olog.msc.domain.entity.UneditedSong
import dev.olog.msc.domain.gateway.SongGateway
import dev.olog.msc.domain.interactor.prefs.BlackListUseCase
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
        @ApplicationContext private val context: Context,
        private val contentResolver: ContentResolver,
        private val rxContentResolver: BriteContentResolver,
        private val blackListUseCase: BlackListUseCase

) : SongGateway {

    companion object {

        private val MEDIA_STORE_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        private val PROJECTION = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.DATE_ADDED
        )

        private const val SELECTION = "$IS_MUSIC <> 0 AND $IS_ALARM = 0 AND $IS_PODCAST = 0 " +
                "AND $TITLE NOT LIKE ? AND $DURATION > ?"

        private val SELECTION_ARGS = arrayOf("AUD%", "20000")

        private const val SORT_ORDER = "lower(${MediaStore.Audio.Media.TITLE})"
    }

    private val contentProviderObserver = rxContentResolver
            .createQuery(
                    MEDIA_STORE_URI,
                    PROJECTION,
                    SELECTION,
                    SELECTION_ARGS,
                    SORT_ORDER,
                    true
            )
            .mapToList { it.toSong(context) }
            .map {
                val blackListed = blackListUseCase.get()
                it.filter { !blackListed.contains(it.folderPath) }
            }
            .onErrorReturn { listOf() }
            .toFlowable(BackpressureStrategy.LATEST)
            .replay(1)
            .refCount()

    override fun getAll(): Flowable<List<Song>> = contentProviderObserver

    override fun getAllUnfiltered(): Flowable<List<Song>> {
        return rxContentResolver.createQuery(
                MEDIA_STORE_URI,
                PROJECTION,
                SELECTION,
                SELECTION_ARGS,
                SORT_ORDER,
                false
        ).mapToList { it.toSong(context) }
                .toFlowable(BackpressureStrategy.LATEST)
                .onErrorReturn { listOf() }
    }

    override fun getAllForImageCreation(): Single<List<Song>> {
        return getAllUnfiltered().firstOrError()
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun getByParam(songId: Long): Flowable<Song> {
        return getAll().map { it.first { it.id == songId } }
    }

    override fun deleteSingle(songId: Long): Completable {

        return Single.fromCallable { contentResolver.delete(MEDIA_STORE_URI,
                "${BaseColumns._ID} = ?",
                arrayOf("$songId"))
        }.filter { it > 0 }
                .flatMapSingle { getByParam(songId).firstOrError() }
                .map { File(it.path) }
                .filter { it.exists() }
                .map { it.delete() }
                .toSingle()
                .toCompletable()

    }

    override fun deleteGroup(songList: List<Song>): Completable {
        return Flowable.fromIterable(songList)
                .map { it.id }
                .flatMapCompletable { deleteSingle(it).subscribeOn(Schedulers.io()) }
    }



    override fun getByParamUnedited(songId: Long): Flowable<UneditedSong> {
        return rxContentResolver.createQuery(
                MEDIA_STORE_URI, null,"${MediaStore.Audio.Media._ID} = ?",
                arrayOf("$songId"), null, false)
                .mapToOne { it.toUneditedSong() }
                .toFlowable(BackpressureStrategy.LATEST)
    }
}
