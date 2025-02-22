package com.funkymuse.aurora.bookdetailsdestination

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.navArgument
import com.funkymuse.aurora.navigator.NavigationDestination

/**
 * Created by funkymuse on 6/25/21 to long live and prosper !
 */
object BookDetailsDestination : NavigationDestination {

    override fun route(): String = BOOK_DETAILS_BOTTOM_NAV_ROUTE

    override val arguments: List<NamedNavArgument>
        get() = listOf(navArgument(BOOK_ID_PARAM) { type = NavType.IntType })

    const val BOOK_ID_PARAM = "book"

    private const val BOOK_DETAILS_ROUTE = "book_details"
    private const val BOOK_DETAILS_BOTTOM_NAV_ROUTE = "$BOOK_DETAILS_ROUTE/{$BOOK_ID_PARAM}"
    fun createBookDetailsRoute(bookID: Int) = "$BOOK_DETAILS_ROUTE/${bookID}"

    private const val BOOK_MIRRORS_PARAM = "mirrors"
    fun NavBackStackEntry.addBookMirrors(mirrors: List<String>) {
        arguments?.putStringArray(BOOK_MIRRORS_PARAM, mirrors.toTypedArray())
    }

    fun NavBackStackEntry.getBookMirrors(): Array<String>? = arguments?.getStringArray(BOOK_MIRRORS_PARAM)

}