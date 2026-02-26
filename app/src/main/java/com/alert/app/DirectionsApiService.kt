package com.alert.app

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface DirectionsApiService {
    @GET
    fun getDirections(@Url url: String): Call<DirectionsResponse>
}

data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val overviewPolyline: OverviewPolyline
)

data class OverviewPolyline(
    val points: String
)