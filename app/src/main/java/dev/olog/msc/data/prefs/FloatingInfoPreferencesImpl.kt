package dev.olog.msc.data.prefs

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dev.olog.msc.domain.gateway.prefs.FloatingInfoPreferencesGateway
import dev.olog.msc.utils.k.extension.edit
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import javax.inject.Inject

class FloatingInfoPreferencesImpl @Inject constructor(
        private val preferences: SharedPreferences,
        private val rxPreferences: RxSharedPreferences
) : FloatingInfoPreferencesGateway {

    companion object {
        private const val CURRENT_INFO_REQUEST = "CURRENT_INFO_REQUEST"
    }

    override fun getInfoRequest(): Flowable<String> {
        return rxPreferences.getString(CURRENT_INFO_REQUEST).asObservable()
                .toFlowable(BackpressureStrategy.LATEST)
    }

    override fun setInfoRequest(newInfoRequest: String) {
        preferences.edit { putString(CURRENT_INFO_REQUEST, newInfoRequest) }
    }
}