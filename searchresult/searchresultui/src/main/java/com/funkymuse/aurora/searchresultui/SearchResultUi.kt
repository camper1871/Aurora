package com.funkymuse.aurora.searchresultui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.funkymuse.aurora.backbuttoncomponent.BackButton
import com.funkymuse.aurora.bookdetailsdestination.BookDetailsDestination
import com.funkymuse.aurora.bookui.Book
import com.funkymuse.aurora.errorcomponent.ErrorMessage
import com.funkymuse.aurora.errorcomponent.ErrorWithRetry
import com.funkymuse.aurora.paging.PagingUIProviderViewModel
import com.funkymuse.aurora.paging.appendState
import com.funkymuse.aurora.radiobutton.RadioButtonWithText
import com.funkymuse.aurora.radiobutton.RadioButtonWithTextNotClickable
import com.funkymuse.aurora.searchdata.SearchViewModel
import com.funkymuse.aurora.searchresultdata.SearchResultHandleDataViewModel
import com.funkymuse.composed.core.*
import com.funkymuse.style.color.PrimaryVariant
import com.funkymuse.style.shape.BottomSheetShapes
import com.funkymuse.style.shape.Shapes
import com.google.accompanist.insets.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Created by FunkyMuse on 25/02/21 to long live and prosper !
 */


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SearchResult(
        onBookClicked: (mirrors: List<String>) -> Unit
) {
    val searchResultViewModelViewModel: SearchResultHandleDataViewModel = hiltViewModel()
    val pagingUIUIProvider: PagingUIProviderViewModel = hiltViewModel()
    var checkedSortPosition by rememberIntSaveableDefaultZero()
    var filtersVisible by rememberBooleanSaveableDefaultFalse()

    var searchInFieldsCheckedPosition by rememberSaveable { mutableStateOf(searchResultViewModelViewModel.searchInFieldsCheckedPosition) }
    var searchWithMaskWord by rememberSaveable { mutableStateOf(searchResultViewModelViewModel.searchWithMaskWord) }

    var progressVisibility by rememberBooleanDefaultFalse()

    val pagingItems = searchResultViewModelViewModel.booksData.collectAsLazyPagingItems()

    val scope = rememberCoroutineScope()


    progressVisibility =
            pagingUIUIProvider.progressBarVisibility(pagingItems)

    filtersVisible = !pagingUIUIProvider.isDataEmptyWithError(pagingItems)
    pagingUIUIProvider.onPaginationReachedError(
            pagingItems.appendState,
            R.string.no_more_books_by_query_to_load
    )

    val retry = {
        searchResultViewModelViewModel.refresh()
        pagingItems.refresh()
    }

    ScaffoldWithBackFiltersAndContent(
            checkedSortPosition,
            searchInFieldsCheckedPosition,
            searchWithMaskWord,
            filtersVisible,
            onBackClicked = { searchResultViewModelViewModel.navigateUp() },
            onSortPositionClicked = {
                checkedSortPosition = it
                searchResultViewModelViewModel.sortByPosition(it)
                pagingItems.refresh()
            },
            onSearchInFieldsCheckedPosition = {
                searchInFieldsCheckedPosition = it
                searchResultViewModelViewModel.searchInFieldsByPosition(it)
                pagingItems.refresh()
            },
            onSearchWithMaskWord = {
                searchWithMaskWord = it
                searchResultViewModelViewModel.searchWithMaskedWord(it)
                pagingItems.refresh()
            }) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (loading, backToTop) = createRefs()
            AnimatedVisibility(visible = progressVisibility, modifier = Modifier
                    .constrainAs(loading) {
                        top.linkTo(parent.top)
                        centerHorizontallyTo(parent)
                    }
                    .wrapContentSize()
                    .padding(top = 8.dp)
                    .zIndex(2f)) {
                CircularProgressIndicator()
            }

            pagingUIUIProvider.OnError(
                    pagingItems = pagingItems,
                    scope = scope,
                    noInternetUI = {
                        ErrorMessage(R.string.no_books_loaded_no_connect)
                    },
                    errorUI = {
                        ErrorWithRetry(R.string.no_books_loaded_search) {
                            retry()
                        }
                    }
            )

            val columnState = rememberLazyListState()

            val lastVisibleIndex = columnState.lastVisibleIndex()
            AnimatedVisibility(visible = lastVisibleIndex != null && lastVisibleIndex > 20,
                    modifier = Modifier
                            .constrainAs(backToTop) {
                                bottom.linkTo(parent.bottom)
                                end.linkTo(parent.end)
                            }
                            .padding(bottom = 22.dp, end = 4.dp)
                            .zIndex(2f)) {

                Box {
                    FloatingActionButton(
                            modifier = Modifier
                                    .navigationBarsPadding(),
                            onClick = { scope.launch { columnState.scrollToItem(0) } },
                    ) {
                        Icon(
                                Icons.Filled.ArrowUpward,
                                contentDescription = stringResource(id = R.string.go_back_to_top),
                                tint = Color.White
                        )
                    }
                }
            }

            val swipeToRefreshState = rememberSwipeRefreshState(isRefreshing = false)
            SwipeRefresh(
                    state = swipeToRefreshState, onRefresh = {
                swipeToRefreshState.isRefreshing = true
                retry()
                swipeToRefreshState.isRefreshing = false
            },
                    modifier = Modifier
                            .fillMaxSize()
            ) {

                LazyColumn(
                        state = columnState,
                        modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 8.dp),
                        contentPadding = rememberInsetsPaddingValues(
                                insets = LocalWindowInsets.current.navigationBars,
                                additionalBottom = 84.dp
                        )
                ) {
                    items(pagingItems) { item ->
                        item ?: return@items

                        Book(item) {
                            val bookID = item.id?.toInt() ?: return@Book
                            onBookClicked(item.mirrors?.toList() ?: emptyList())
                            searchResultViewModelViewModel.navigate(BookDetailsDestination.createBookDetailsRoute(bookID))
                        }
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun backButtonLogic(bottomSheetState: BottomSheetState,
                            scope: CoroutineScope, dropDownMenuExpanded: Boolean, onDropDownSetFalse: () -> Unit, onBackClicked: () -> Unit) {
    when {
        bottomSheetState.isExpanded -> {
            scope.launch { bottomSheetState.collapse() }
        }
        dropDownMenuExpanded -> onDropDownSetFalse()
        else -> onBackClicked()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScaffoldWithBackFiltersAndContent(
        checkedSortPosition: Int,
        searchInFieldsCheckedPosition: Int,
        searchWithMaskWord: Boolean,
        filtersVisible: Boolean,
        onBackClicked: () -> Unit,
        onSortPositionClicked: (Int) -> Unit,
        onSearchInFieldsCheckedPosition: (Int) -> Unit,
        onSearchWithMaskWord: (Boolean) -> Unit,
        content: @Composable (PaddingValues) -> Unit
) {

    val bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)
    val scope = rememberCoroutineScope()
    val searchViewModel = hiltViewModel<SearchViewModel>()

    var dropDownMenuExpanded by remember { mutableStateOf(false) }
    val collapseDropDownMenu = { dropDownMenuExpanded = false }

    val onBack = {
        backButtonLogic(bottomSheetState, scope, dropDownMenuExpanded, collapseDropDownMenu, onBackClicked)
    }

    BackHandler {
        onBack()
    }

    BottomSheetScaffold(
            sheetContent = {
                LazyColumn {

                    item {
                        Text(
                                text = stringResource(R.string.search_in_fields), modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                        )
                    }

                    itemsIndexed(searchViewModel.searchInFieldEntries) { index, item ->
                        RadioButtonWithText(
                                text = item.title,
                                isChecked = searchInFieldsCheckedPosition == index,
                                onRadioButtonClicked = {
                                    onSearchInFieldsCheckedPosition(index)
                                    scope.launch { bottomSheetState.collapse() }
                                })
                    }

                    item {
                        Text(
                                text = stringResource(R.string.mask_word), modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                        )
                    }

                    item {
                        RadioButtonWithText(
                                text = R.string.search_with_mask_word,
                                isChecked = searchWithMaskWord,
                                onRadioButtonClicked = {
                                    onSearchWithMaskWord(!searchWithMaskWord)
                                })
                    }

                    item {
                        Spacer(modifier = Modifier.padding(bottom = 64.dp))
                    }
                }
            },
            sheetPeekHeight = 0.dp,
            modifier = Modifier.fillMaxSize(),
            scaffoldState = scaffoldState,
            sheetShape = BottomSheetShapes.large,
            topBar = {
                TopAppBar(backgroundColor = PrimaryVariant, modifier = Modifier.statusBarsPadding()) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (backButton, filter) = createRefs()
                        BackButton(
                                modifier = Modifier
                                        .constrainAs(backButton) {
                                            start.linkTo(parent.start)
                                            top.linkTo(parent.top)
                                            bottom.linkTo(parent.bottom)
                                        }
                                        .padding(8.dp), onClick = {
                            onBack()
                        }
                        )

                        if (filtersVisible) {
                            Button(
                                    onClick = {
                                        dropDownMenuExpanded = !dropDownMenuExpanded
                                        scope.launch {
                                            if (!bottomSheetState.isCollapsed) {
                                                bottomSheetState.collapse()
                                            }
                                        } // only the filter menu is visible since it takes almost the whole screen
                                    },
                                    shape = Shapes.large,
                                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface),
                                    modifier = Modifier
                                            .constrainAs(filter) {
                                                end.linkTo(parent.end)
                                                top.linkTo(parent.top)
                                                bottom.linkTo(parent.bottom)
                                            }
                                            .padding(8.dp)
                            ) {
                                Icon(
                                        imageVector = Icons.Default.FilterAlt,
                                        contentDescription = stringResource(id = R.string.title_favorites)
                                )
                            }

                            DropdownMenu(expanded = dropDownMenuExpanded,
                                    modifier = Modifier.fillMaxWidth(),
                                    offset = DpOffset(32.dp, 32.dp),
                                    onDismissRequest = collapseDropDownMenu) {
                                searchViewModel.sortList.forEach {
                                    DropdownMenuItem(onClick = {
                                        onSortPositionClicked(it.first)
                                        collapseDropDownMenu()
                                    }) {
                                        RadioButtonWithTextNotClickable(
                                                text = it.second,
                                                isChecked = checkedSortPosition == it.first
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }) {

        ConstraintLayout {
            val filter = createRef()

            //add scrim
            if (bottomSheetState.isExpanded) {
                Box(modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                                indication = null,
                                interactionSource = MutableInteractionSource()
                        ) {
                            scope.launch { bottomSheetState.collapse() }
                        }
                        .background(brush = SolidColor(Color.Black), alpha = 0.5f)
                        .zIndex(0.5f))
            }

            Box(
                    modifier = Modifier
                            .fillMaxSize()
                            .zIndex(if (bottomSheetState.isExpanded) 0.2f else 0f)
            ) {
                content(it)
            }

            Box(
                    modifier = Modifier
                            .constrainAs(filter) {
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .padding(bottom = 22.dp)
                            .zIndex(0.3f)
            ) {
                if (filtersVisible) {
                    FloatingActionButton(
                            modifier = Modifier
                                    .navigationBarsPadding(),
                            onClick = { scope.launch { bottomSheetState.expand() } },
                    ) {
                        Icon(
                                Icons.Filled.FilterList,
                                contentDescription = stringResource(id = R.string.filter),
                                tint = Color.White
                        )
                    }
                }
            }
        }

    }
}