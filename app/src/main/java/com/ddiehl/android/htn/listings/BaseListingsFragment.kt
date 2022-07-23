package com.ddiehl.android.htn.listings

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.listings.report.ReportViewRouter
import com.ddiehl.android.htn.routing.AppRouter
import com.ddiehl.android.htn.view.BaseFragment
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

abstract class BaseListingsFragment : BaseFragment(),
    ListingsView,
    SwipeRefreshLayout.OnRefreshListener {

    @Inject
    internal lateinit var appRouter: AppRouter
    @Inject
    internal lateinit var reportViewRouter: ReportViewRouter

    lateinit var recyclerView: RecyclerView
    protected lateinit var swipeRefreshLayout: SwipeRefreshLayout

    protected lateinit var listingsPresenter: BaseListingsPresenter
    protected abstract val listingsAdapter: ListingsAdapter
    protected lateinit var callbacks: ListingsView.Callbacks

    private val onScrollListener: RecyclerView.OnScrollListener
        get() = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                with(recyclerView.layoutManager as LinearLayoutManager) {
                    handleScroll(childCount, itemCount, findFirstVisibleItemPosition())
                }
            }

            private fun handleScroll(visible: Int, total: Int, firstVisible: Int) {
                if (firstVisible == 0) {
                    callbacks.onFirstItemShown()
                } else if (visible + firstVisible >= total) {
                    callbacks.onLastItemShown()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
        listenForReportViewResults()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        return super.onCreateView(inflater, container, state).also {
            recyclerView = it.findViewById(R.id.recycler_view);
            instantiateListView()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener(this)
    }

    private fun instantiateListView() {
        with(recyclerView) {
            layoutManager = LinearLayoutManager(activity)
            clearOnScrollListeners()
            addOnScrollListener(onScrollListener)
            adapter = listingsAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        // FIXME Do we need to check nextRequested here?
        if (!listingsPresenter.hasData()) {
            listingsPresenter.refreshData()
        }
    }

    override fun onDestroy() {
        recyclerView.adapter = null

        // To disable the memory dereferencing functionality just comment these lines
        listingsPresenter.clearData()

        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                listingsPresenter.refreshData()
                return true
            }
            R.id.action_settings -> {
                appRouter.showSettings()
                return true
            }
        }
        return false
    }

    protected fun hideTimespanOptionIfUnsupported(menu: Menu, sort: String) {
        menu.findItem(R.id.action_change_sort).isVisible = true
        when (sort) {
            "controversial", "top" -> menu.findItem(R.id.action_change_timespan).isVisible = true
            "hot", "new", "rising" -> menu.findItem(R.id.action_change_timespan).isVisible = false
            else -> menu.findItem(R.id.action_change_timespan).isVisible = false
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return listingsPresenter.onContextItemSelected(item)
    }

    override fun notifyDataSetChanged() = listingsAdapter.notifyDataSetChanged()
    override fun notifyItemChanged(position: Int) = listingsAdapter.notifyItemChanged(position)
    override fun notifyItemInserted(position: Int) = listingsAdapter.notifyItemInserted(position)
    override fun notifyItemRemoved(position: Int) = listingsAdapter.notifyItemRemoved(position)
    override fun notifyItemRangeChanged(position: Int, count: Int) =
        listingsAdapter.notifyItemRangeChanged(position, count)

    override fun notifyItemRangeInserted(position: Int, count: Int) =
        listingsAdapter.notifyItemRangeInserted(position, count)

    override fun notifyItemRangeRemoved(position: Int, count: Int) =
        listingsAdapter.notifyItemRangeRemoved(position, count)

    override fun onRefresh() {
        swipeRefreshLayout.isRefreshing = false
        listingsPresenter.refreshData()
    }

    private fun listenForReportViewResults() {
        reportViewRouter.observeReportResults()
            .subscribe { result ->
                when (result) {
                    ReportViewRouter.ReportResult.SUCCESS -> showReportSuccessToast()
                    ReportViewRouter.ReportResult.CANCELED -> showReportErrorToast()
                    null -> { }
                }
            }
    }

    private fun showReportSuccessToast() {
        Snackbar.make(chromeView, R.string.report_successful, Snackbar.LENGTH_LONG).show()
    }

    private fun showReportErrorToast() {
        Snackbar.make(chromeView, R.string.report_error, Snackbar.LENGTH_LONG).show()
    }
}
