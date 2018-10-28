package dev.olog.msc.presentation.library.categories.podcast

import android.content.Context
import dev.olog.msc.dagger.qualifier.ApplicationContext
import dev.olog.msc.dagger.qualifier.ChildFragmentManager
import dev.olog.msc.domain.interactor.prefs.AppPreferencesUseCase
import dev.olog.msc.presentation.library.tab.TabFragment
import dev.olog.msc.utils.MediaIdCategory
import javax.inject.Inject

class CategoriesPodcastFragmentViewPager @Inject constructor(
        @ApplicationContext private val context: Context,
        @ChildFragmentManager private val fragmentManager: androidx.fragment.app.FragmentManager,
        prefsUseCase: AppPreferencesUseCase

) : androidx.fragment.app.FragmentStatePagerAdapter(fragmentManager) {

    private val data = prefsUseCase.getPodcastLibraryCategories()
            .filter { it.visible }

    fun getCategoryAtPosition(position: Int): MediaIdCategory? {
        try {
            return data[position].category
        } catch (ex: Exception){
            return null
        }
    }

    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        val category = data[position].category
        return TabFragment.newInstance(category)
    }

    override fun getCount(): Int = data.size

    override fun getPageTitle(position: Int): CharSequence? {
        return data[position].asString(context)
    }

    fun isEmpty() = data.isEmpty()
}