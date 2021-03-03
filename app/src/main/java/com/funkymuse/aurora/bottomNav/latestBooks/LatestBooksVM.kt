package com.funkymuse.aurora.bottomNav.latestBooks

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.crazylegend.kotlinextensions.context.isOnline
import com.crazylegend.kotlinextensions.livedata.context
import com.crazylegend.kotlinextensions.log.debug
import com.crazylegend.retrofit.retrofitResult.*
import com.crazylegend.retrofit.throwables.NoConnectionException
import com.funkymuse.aurora.consts.*
import com.funkymuse.aurora.dto.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

@HiltViewModel
class LatestBooksVM @Inject constructor (application: Application) : AndroidViewModel(application) {

    private val booksDataHolder: MutableStateFlow<RetrofitResult<List<Book>>> =
        MutableStateFlow(RetrofitResult.EmptyData)
    val booksData = booksDataHolder.asStateFlow()

    private var page = 1
    private var canLoadMore = true
    private val adapterList = mutableStateListOf<Book>()
    private var sortQuery = ""
    private var sortType = ""

    init {
        searchForBook()
    }

    fun searchForBook() {
        if (canLoadMore) {
            booksDataHolder.loading()
            if (context.isOnline) {
                viewModelScope.launch {
                    try {
                        val it = withContext(Dispatchers.Default) { getData() }
                        if (it == null) {
                            booksDataHolder.emptyData()
                        } else {
                            val list = processDocument(it)
                            debug { "LIST $list" }
                            if (list.isNullOrEmpty()) {
                                booksDataHolder.emptyData()
                            } else {
                                adapterList += list
                                booksDataHolder.success(adapterList)
                            }
                        }
                    } catch (t: Throwable) {
                        booksDataHolder.callError(t)
                    }
                }
            } else {
                booksDataHolder.callError(NoConnectionException())
            }
        }
    }

    private fun getData(): Document? {
        val jsoup = Jsoup.connect(SEARCH_BASE_URL)
            .timeout(DEFAULT_API_TIMEOUT)
            .data(SORT_QUERY, sortQuery)
            .data(VIEW_QUERY, VIEW_QUERY_PARAM)
            .data(LAST_MODE, LAST_QUERY)
            .data(COLUM_QUERY, COLUMN_QUERY_PARAM)
            .data(RES_CONST, PAGE_SIZE)
            .data(SORT_TYPE, sortType)
            .data(PAGE_CONST, page.toString())
        return jsoup.get()
    }


    private fun processDocument(doc: Document?): List<Book>? {
        return doc?.let { document ->

            val trs = document.select("table")[2].select("tr")
            trs.removeAt(0)

            if (trs.size < PAGE_SIZE.toInt()) {
                //cant load more
                canLoadMore = false
            } else {
                page++
                //has more pages
            }

            return@let trs.map {
                return@map Book(it)
            }
        }
    }

    private fun resetOnSort() {
        adapterList.clear()
        sortType = ""
        sortQuery = ""
        page = 1
        canLoadMore = true
    }

    fun sortByYearDESC(
        scope: CoroutineScope,
        retrofitResult: MutableStateFlow<RetrofitResult<List<Book>>>
    ) {
        resetOnSort()
        sortType = SORT_YEAR_CONST
        sortQuery = SORT_TYPE_DESC
        searchForBook()
    }

    fun sortByYearASC(
        scope: CoroutineScope,
        retrofitResult: MutableStateFlow<RetrofitResult<List<Book>>>
    ) {
        resetOnSort()
        sortType = SORT_YEAR_CONST
        sortQuery = SORT_TYPE_ASC
        searchForBook()
    }

    fun sortByDefault(
        scope: CoroutineScope,
        retrofitResult: MutableStateFlow<RetrofitResult<List<Book>>>
    ) {
        resetOnSort()
        searchForBook()
    }

    fun sortBySizeDESC(
        scope: CoroutineScope,
        retrofitResult: MutableStateFlow<RetrofitResult<List<Book>>>
    ) {
        resetOnSort()
        sortType = SORT_SIZE
        sortQuery = SORT_TYPE_DESC
        searchForBook()
    }

    fun sortBySizeASC(
        scope: CoroutineScope,
        retrofitResult: MutableStateFlow<RetrofitResult<List<Book>>>
    ) {
        resetOnSort()
        sortType = SORT_SIZE
        sortQuery = SORT_TYPE_ASC
        searchForBook()
    }

    fun refresh() {
        resetOnSort()
        searchForBook()
    }

}