package dev.olog.msc.presentation.preferences.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.ColorCallback
import com.afollestad.materialdialogs.color.colorChooser
import com.google.android.material.snackbar.Snackbar
import dev.olog.msc.core.MediaIdCategory
import dev.olog.msc.core.gateway.prefs.TutorialPreferenceGateway
import dev.olog.msc.imageprovider.glide.GlideApp
import dev.olog.msc.presentation.base.ImageViews
import dev.olog.msc.presentation.base.extensions.act
import dev.olog.msc.presentation.base.extensions.ctx
import dev.olog.msc.presentation.base.extensions.fragmentTransaction
import dev.olog.msc.presentation.preferences.R
import dev.olog.msc.presentation.preferences.blacklist.BlacklistFragment
import dev.olog.msc.presentation.preferences.categories.LibraryCategoriesFragment
import dev.olog.msc.presentation.preferences.credentials.LastFmCredentialsFragment
import dev.olog.msc.presentation.preferences.utils.ColorPalette
import dev.olog.msc.presentation.preferences.utils.forEach
import dev.olog.msc.pro.HasBilling
import dev.olog.msc.shared.extensions.toast
import dev.olog.msc.shared.ui.extensions.colorPrimary
import dev.olog.msc.shared.ui.extensions.subscribe
import javax.inject.Inject

@Keep
class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    ColorCallback {

    @Inject
    lateinit var tutoriaPrefs: TutorialPreferenceGateway

    private val presenter by lazy {
        SettingsFragmentPresenter(
            requireContext().applicationContext,
            (requireActivity() as HasBilling).billing,
            tutoriaPrefs
        )
    }

    private lateinit var libraryCategories: Preference
    private lateinit var podcastCategories: Preference
    private lateinit var blacklist: Preference
    private lateinit var iconShape: Preference
    private lateinit var deleteCache: Preference
    private lateinit var lastFmCredentials: Preference
    private lateinit var autoCreateImages: Preference
    private lateinit var accentColorChooser: Preference
    private lateinit var resetTutorial: Preference

    private var requestActivityToRecreate = false

    override fun onAttach(context: Context) {
        inject()
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)
        libraryCategories = preferenceScreen.findPreference(getString(R.string.prefs_library_categories_key))!!
        podcastCategories = preferenceScreen.findPreference(getString(R.string.prefs_podcast_library_categories_key))!!
        blacklist = preferenceScreen.findPreference(getString(R.string.prefs_blacklist_key))!!
        iconShape = preferenceScreen.findPreference(getString(R.string.prefs_icon_shape_key))!!
        deleteCache = preferenceScreen.findPreference(getString(R.string.prefs_delete_cached_images_key))!!
        lastFmCredentials = preferenceScreen.findPreference(getString(R.string.prefs_last_fm_credentials_key))!!
        autoCreateImages = preferenceScreen.findPreference(getString(R.string.prefs_auto_create_images_key))!!
        accentColorChooser = preferenceScreen.findPreference(getString(R.string.prefs_color_accent_key))!!
        resetTutorial = preferenceScreen.findPreference(getString(R.string.prefs_reset_tutorial_key))!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.onAttach()

        presenter.observeIsPremium()
            .subscribe(viewLifecycleOwner) { isPremium ->
                forEach(preferenceScreen) { it.isEnabled = isPremium }

                if (!isPremium) {
                    val v = act.window.decorView.findViewById<View>(android.R.id.content)
                    Snackbar.make(v, R.string.prefs_not_premium, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.prefs_not_premium_action) { presenter.purchasePremium() }
                        .show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDetach()
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        libraryCategories.setOnPreferenceClickListener {
            LibraryCategoriesFragment.newInstance(MediaIdCategory.SONGS)
                .show(activity!!.supportFragmentManager, LibraryCategoriesFragment.TAG)
            true
        }
        podcastCategories.setOnPreferenceClickListener {
            LibraryCategoriesFragment.newInstance(MediaIdCategory.PODCASTS)
                .show(activity!!.supportFragmentManager, LibraryCategoriesFragment.TAG)
            true
        }
        blacklist.setOnPreferenceClickListener {
            act.fragmentTransaction {
                setReorderingAllowed(true)
                add(BlacklistFragment.newInstance(), BlacklistFragment.TAG)
            }
            true
        }

        deleteCache.setOnPreferenceClickListener {
            showDeleteAllCacheDialog()
            true
        }
        lastFmCredentials.setOnPreferenceClickListener {
            act.fragmentTransaction {
                setReorderingAllowed(true)
                add(LastFmCredentialsFragment.newInstance(), LastFmCredentialsFragment.TAG)
            }
            true
        }
        accentColorChooser.setOnPreferenceClickListener {
            // TODO get from app press
            val prefs = PreferenceManager.getDefaultSharedPreferences(act.applicationContext)
            val initialSelection = prefs.getInt(getString(R.string.prefs_color_accent_key), ctx.colorPrimary())

            MaterialDialog(act)
                .colorChooser(
                    colors = ColorPalette.ACCENT_COLORS,
                    subColors = ColorPalette.ACCENT_COLORS_SUB,
                    initialSelection = initialSelection,
                    selection = this
                ).show()
            true
        }
        resetTutorial.setOnPreferenceClickListener {
            showResetTutorialDialog()
            true
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        libraryCategories.onPreferenceClickListener = null
        podcastCategories.onPreferenceClickListener = null
        blacklist.onPreferenceClickListener = null
        deleteCache.onPreferenceClickListener = null
        lastFmCredentials.onPreferenceClickListener = null
        accentColorChooser.onPreferenceClickListener = null
        resetTutorial.onPreferenceClickListener = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            getString(R.string.prefs_quick_action_key) -> {
                ImageViews.updateQuickAction(act)
                requestActivityToRecreate()
            }
            getString(R.string.prefs_icon_shape_key) -> {
                ImageViews.updateIconShape(act)
                requestActivityToRecreate()
            }
            getString(R.string.prefs_appearance_key) -> {
                requestActivityToRecreate()
            }
            getString(R.string.prefs_folder_tree_view_key),
            getString(R.string.prefs_blacklist_key),
            getString(R.string.prefs_show_podcasts_key),
            getString(R.string.prefs_adaptive_colors_key) -> requestActivityToRecreate()
        }
    }

    private fun showDeleteAllCacheDialog() {
        AlertDialog.Builder(ctx)
            .setTitle(R.string.prefs_delete_cached_images_title)
            .setMessage(R.string.are_you_sure)
            .setPositiveButton(R.string.common_ok) { _, _ ->
                GlideApp.get(ctx.applicationContext).clearMemory()
                presenter.clearCachedImages {
                    requestActivityToRecreate()
                    ctx.applicationContext.toast(R.string.prefs_delete_cached_images_success)
                }
            }
            .setNegativeButton(R.string.common_no, null)
            .show()
    }

    private fun showResetTutorialDialog() {
        AlertDialog.Builder(ctx)
            .setTitle(R.string.prefs_reset_tutorial_title)
            .setMessage(R.string.are_you_sure)
            .setPositiveButton(R.string.common_ok) { _, _ -> presenter.resetTutorial() }
            .setNegativeButton(R.string.common_no, null)
            .show()
    }

    override fun invoke(dialog: MaterialDialog, color: Int) {
        // TODO move to apprefs
        val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        val key = getString(R.string.prefs_color_accent_key)
        prefs.edit {
            putInt(key, color)
        }
        requireActivity().recreate()
    }

    private fun requestActivityToRecreate() {
        requestActivityToRecreate = true
    }
}