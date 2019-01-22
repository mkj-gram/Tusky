/* Copyright 2019 Conny Duck
 *
 * This file is a part of Tusky.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tusky is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tusky; if not,
 * see <http://www.gnu.org/licenses>. */

package com.keylesspalace.tusky.components.conversation

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import com.keylesspalace.tusky.AccountActivity
import com.keylesspalace.tusky.R
import com.keylesspalace.tusky.ViewTagActivity
import com.keylesspalace.tusky.db.AppDatabase
import com.keylesspalace.tusky.di.Injectable
import com.keylesspalace.tusky.di.ViewModelFactory
import com.keylesspalace.tusky.fragment.SFragment
import com.keylesspalace.tusky.interfaces.StatusActionListener
import com.keylesspalace.tusky.network.TimelineCases
import com.keylesspalace.tusky.util.NetworkState
import com.keylesspalace.tusky.util.ThemeUtils
import com.keylesspalace.tusky.util.hide
import kotlinx.android.synthetic.main.fragment_timeline.*
import javax.inject.Inject

class ConversationsFragment : SFragment(), StatusActionListener, Injectable {

    @Inject
    lateinit var timelineCases: TimelineCases
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    @Inject
    lateinit var db: AppDatabase

    private lateinit var viewModel: ConversationsViewModel

    private var alwaysShowSensitiveMedia = false
    private var mediaPreviewEnabled = true
    private var useAbsoluteTime = false

    private lateinit var adapter: ConversationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        viewModel = ViewModelProviders.of(this, viewModelFactory)[ConversationsViewModel::class.java]

        return inflater.inflate(R.layout.fragment_timeline, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(view.context)
        alwaysShowSensitiveMedia = preferences.getBoolean("alwaysShowSensitiveMedia", false)
        mediaPreviewEnabled = preferences.getBoolean("mediaPreviewEnabled", true)
        useAbsoluteTime = preferences.getBoolean("absoluteTimeView", false)

        adapter = ConversationAdapter(this)  {
            viewModel.retry()
        }

        recyclerView.addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.adapter = adapter

        progressBar.hide()
        nothingMessage.hide()

        initSwipeToRefresh()

        viewModel.conversations.observe(this, Observer<PagedList<ConversationEntity>> {
            adapter.submitList(it)
        })
        viewModel.networkState.observe(this, Observer {
           adapter.setNetworkState(it)
        })

        viewModel.load(0)
    }

    private fun initSwipeToRefresh() {
        viewModel.refreshState.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = it == NetworkState.LOADING
            if(it == NetworkState.LOADED) {
                recyclerView.scrollToPosition(0)
            }
        })
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
        swipeRefreshLayout.setColorSchemeResources(R.color.tusky_blue)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ThemeUtils.getColor(swipeRefreshLayout.context, android.R.attr.colorBackground))
    }

    override fun onReblog(reblog: Boolean, position: Int) {
      // its impossible to reblog private messages
    }

    override fun onFavourite(favourite: Boolean, position: Int) {
     /*   val status = searchAdapter.getStatusAtPosition(position)
        if(status != null) {
            timelineCases.favouriteWithCallback(status, favourite, object: Callback<Status> {
                override fun onResponse(call: Call<Status>?, response: Response<Status>?) {
                    status.favourited = true
                    searchAdapter.updateStatusAtPosition(
                            ViewDataUtils.statusToViewData(
                                    status,
                                    alwaysShowSensitiveMedia
                            ),
                            position
                    )
                }

                override fun onFailure(call: Call<Status>?, t: Throwable?) {
                    Log.d(TAG, "Failed to favourite status " + status.id, t)
                }

            })
        }*/
    }

    override fun onMore(view: View?, position: Int) {
       /* val status = searchAdapter.getStatusAtPosition(position)
        if(status != null) {
            more(status, view, position)
        }*/
    }

    override fun onViewMedia(position: Int, attachmentIndex: Int, view: View?) {
       /* val status = searchAdapter.getStatusAtPosition(position) ?: return
        viewMedia(attachmentIndex, status, view)*/
    }

    override fun onViewThread(position: Int) {
     /*   val status = searchAdapter.getStatusAtPosition(position)
        if(status != null) {
            viewThread(status)
        }*/
    }

    override fun onOpenReblog(position: Int) {
        // there are no reblogs in search results
    }

    override fun onExpandedChange(expanded: Boolean, position: Int) {
     /*   val status = searchAdapter.getConcreteStatusAtPosition(position)
        if(status != null) {
            val newStatus = StatusViewData.Builder(status)
                    .setIsExpanded(expanded).createStatusViewData()
            searchAdapter.updateStatusAtPosition(newStatus, position)
        }*/
    }

    override fun onContentHiddenChange(isShowing: Boolean, position: Int) {
      /*  val status = searchAdapter.getConcreteStatusAtPosition(position)
        if(status != null) {
            val newStatus = StatusViewData.Builder(status)
                    .setIsShowingSensitiveContent(isShowing).createStatusViewData()
            searchAdapter.updateStatusAtPosition(newStatus, position)
        }*/
    }

    override fun onLoadMore(position: Int) {
        // not needed here, search is not paginated
    }

    override fun onContentCollapsedChange(isCollapsed: Boolean, position: Int) {
     /*   // TODO: No out-of-bounds check in getConcreteStatusAtPosition
        val status = searchAdapter.getConcreteStatusAtPosition(position)
        if(status == null) {
            Log.e(TAG, String.format("Tried to access status but got null at position: %d", position))
            return
        }

        val updatedStatus = StatusViewData.Builder(status)
                .setCollapsed(isCollapsed)
                .createStatusViewData()
        searchAdapter.updateStatusAtPosition(updatedStatus, position)
        searchRecyclerView.post { searchAdapter.notifyItemChanged(position, updatedStatus) }*/
    }

    override fun onViewAccount(id: String) {
        val intent = AccountActivity.getIntent(requireContext(), id)
        startActivity(intent)
    }

    override fun onViewTag(tag: String) {
        val intent = Intent(context, ViewTagActivity::class.java)
        intent.putExtra("hashtag", tag)
        startActivity(intent)
    }

    companion object {
        fun newInstance() = ConversationsFragment()
    }

    override fun timelineCases(): TimelineCases {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeItem(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onReply(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
