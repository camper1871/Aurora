package com.funkymuse.aurora.api

import com.crazylegend.retrofit.retrofitResult.RetrofitResult
import com.funkymuse.aurora.consts.FIELDS_QUERY
import com.funkymuse.aurora.consts.FIELDS_QUERY_CONST
import com.funkymuse.aurora.consts.IDS_QUERY_CONST
import com.funkymuse.aurora.consts.JSON_PHP_PAGE_CONST
import com.funkymuse.aurora.dto.DetailedBookModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


/**
 * Created by hristijan on 10/22/19 to long live and prosper !
 */
interface LibgenAPI {

    @GET(JSON_PHP_PAGE_CONST)
    suspend fun getDetailedBook(
        @Query(IDS_QUERY_CONST) id: Int,
        @Query(FIELDS_QUERY_CONST) fields: String = FIELDS_QUERY
    ): RetrofitResult<List<DetailedBookModel>>
}