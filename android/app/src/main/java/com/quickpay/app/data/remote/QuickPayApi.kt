package com.quickpay.app.data.remote

import com.quickpay.app.data.remote.dto.CreateLinkReq
import com.quickpay.app.data.remote.dto.CreateLinkRes
import com.quickpay.app.data.remote.dto.OrderDto
import com.quickpay.app.data.remote.dto.PaymentLinkDto
import retrofit2.Response
import retrofit2.http.*

interface QuickPayApi {
    @POST("v1/links")
    suspend fun createLink(@Body body: CreateLinkReq): Response<CreateLinkRes>

    @GET("v1/orders/{id}")
    suspend fun getOrder(@Path("id") id: String): OrderDto

    @POST("v1/dev/orders/{id}/mark")
    suspend fun markOrder(
        @Path("id") id: String,
        @Query("status") status: String
    )

    @GET("v1/orders")
    suspend fun getOrders(
        @Query("limit") limit: Int = 20
    ): List<OrderDto>

    @POST("v1/orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String): Response<Unit>
}