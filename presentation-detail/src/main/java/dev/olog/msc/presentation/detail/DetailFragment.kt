package dev.olog.msc.presentation.detail


import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import com.jakewharton.rxbinding2.widget.RxTextView
import dev.olog.msc.core.MediaId
import dev.olog.msc.presentation.base.extensions.*
import dev.olog.msc.presentation.base.fragment.BaseFragment
import dev.olog.msc.presentation.base.interfaces.CanChangeStatusBarColor
import dev.olog.msc.presentation.base.theme.dark.mode.isDark
import dev.olog.msc.presentation.detail.adapter.*
import dev.olog.msc.presentation.detail.listener.HeaderVisibilityScrollListener
import dev.olog.msc.presentation.navigator.Navigator
import dev.olog.msc.shared.extensions.isPortrait
import dev.olog.msc.shared.extensions.lazyFast
import dev.olog.msc.shared.ui.extensions.setVisible
import dev.olog.msc.shared.ui.extensions.toggleVisibility
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_detail.view.*
import javax.inject.Inject
import kotlin.properties.Delegates

class DetailFragment : BaseFragment(), CanChangeStatusBarColor {

    companion object {
        const val TAG = "DetailFragment"
        const val ARGUMENTS_MEDIA_ID = "$TAG.arguments.media_id"

        @JvmStatic
        fun newInstance(mediaId: MediaId): DetailFragment {
            return DetailFragment().withArguments(
                    ARGUMENTS_MEDIA_ID to mediaId.toString())
        }
    }

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazyFast { viewModelProvider<DetailFragmentViewModel>(viewModelFactory) }

    private val recyclerOnScrollListener by lazyFast { HeaderVisibilityScrollListener(this) }

    private val mediaId by lazyFast {
        val mediaId = arguments!!.getString(ARGUMENTS_MEDIA_ID)!!
        MediaId.fromString(mediaId)
    }

    private val mostPlayedAdapter by lazyFast { DetailMostPlayedAdapter(navigator) }
    private val recentlyAddedAdapter by lazyFast { DetailRecentlyAddedAdapter(navigator) }
    private val relatedArtistAdapter by lazyFast { DetailRelatedArtistsAdapter(navigator) }
    private val albumsAdapter by lazyFast { DetailAlbumsAdapter(navigator) }

    private val adapter by lazyFast {
        DetailFragmentAdapter(
            mediaId, recentlyAddedAdapter, mostPlayedAdapter, relatedArtistAdapter,
            albumsAdapter, navigator, viewModel
        )
    }

    internal var hasLightStatusBarColor by Delegates.observable(false) { _, _, new ->
        adjustStatusBarColor(new)
    }

    override fun onViewBound(view: View, savedInstanceState: Bundle?) {
        view.list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(ctx)
        view.list.adapter = adapter
        view.list.setHasFixedSize(true)

        var swipeDirections = ItemTouchHelper.LEFT
        if (adapter.canSwipeRight){
            swipeDirections = swipeDirections or ItemTouchHelper.RIGHT
        }
//        val callback = TouchHelperAdapterCallback(adapter, swipeDirections)
//        val touchHelper = ItemTouchHelper(callback)
//        touchHelper.attachToRecyclerView(view.list)
//        adapter.touchHelper = touchHelper

        view.fastScroller.attachRecyclerView(view.list)
        view.fastScroller.showBubble(false)

        view.cover?.setVisible()

        viewModel.data
            .subscribe(viewLifecycleOwner) {
                adapter.submitList(it)
                //                        if (ctx.isLandscape){
//                            // header in list is not used in landscape
//                            copy[DetailFragmentDataType.HEADER]!!.clear()
//                        }
            }

        viewModel.mostPlayed
                .subscribe(viewLifecycleOwner, mostPlayedAdapter::submitList)
//
        viewModel.recentlyAdded
                .subscribe(viewLifecycleOwner, recentlyAddedAdapter::submitList)
//
        viewModel.relatedArtists
                .subscribe(viewLifecycleOwner, relatedArtistAdapter::submitList)
//
        viewModel.siblings
                .subscribe(viewLifecycleOwner, albumsAdapter::submitList)

//        viewModel.itemLiveData.subscribe(viewLifecycleOwner) { item ->
//            if (item.isNotEmpty()){
//                headerText.text = item[0].title
//                val cover = view.findViewById<View>(R.id.cover)
//                if (!isPortrait() && cover is ShapeImageView){
//                    DisplayableItemBindingAdapter.loadBigAlbumImage(cover, item[0])
//                }
//            }
//        }

        RxTextView.afterTextChangeEvents(view.editText)
                .map { it.view() }
                .asLiveData()
                .subscribe(viewLifecycleOwner) { edit ->
                    val isEmpty = edit.text.isEmpty()
                    view.clear.toggleVisibility(!isEmpty, true)
                    viewModel.updateFilter(edit.text.toString())
                }
    }

    override fun onResume() {
        super.onResume()
        if (ctx.isPortrait){
            list.addOnScrollListener(recyclerOnScrollListener)
        }
        back.setOnClickListener { act.onBackPressed() }
        more.setOnClickListener { navigator.toDialog(viewModel.mediaId, more) }
        filter.setOnClickListener {
            searchWrapper.toggleVisibility(!searchWrapper.isVisible, true)
        }
        clear.setOnClickListener { editText.setText("") }
    }

    override fun onPause() {
        super.onPause()
        if (ctx.isPortrait){
            list.removeOnScrollListener(recyclerOnScrollListener)
//            list.removeItemDecoration(detailListMargin)
        }
        back.setOnClickListener(null)
        more.setOnClickListener(null)
        filter.setOnClickListener(null)
        clear.setOnClickListener(null)
    }

    override fun adjustStatusBarColor() {
        adjustStatusBarColor(hasLightStatusBarColor)
    }

    override fun adjustStatusBarColor(lightStatusBar: Boolean){
        if (lightStatusBar){
            setLightStatusBar()
        } else {
            removeLightStatusBar()
        }
    }

    private fun removeLightStatusBar(){
        act.window.removeLightStatusBar()
        val color = ContextCompat.getColor(ctx, R.color.detail_button_color_light)
        view?.back?.setColorFilter(color)
        more?.setColorFilter(color)
        filter?.setColorFilter(color)
    }

    private fun setLightStatusBar(){
        if (context.isDark()){
            return
        }

        act.window.setLightStatusBar()
        val color = ContextCompat.getColor(ctx, R.color.detail_button_color_dark)
        view?.back?.setColorFilter(color)
        more?.setColorFilter(color)
        filter?.setColorFilter(color)
    }

    override fun provideLayoutId(): Int = R.layout.fragment_detail
}