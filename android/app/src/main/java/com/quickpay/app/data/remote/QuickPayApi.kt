//package com.quickpay.app.data.remote
//
//import retrofit2.http.*
//import retrofit2.Response
//
//data class CreateLinkReq(
//    val amountCents: Int,
//    val currency: String,
//    val description: String? = null
//)
//
//data class CreateLinkRes(
//    val orderId: String,
//    val url: String
//)
//
//data class OrderDto(
//    val id: String,
//    val amountCents: Int,
//    val currency: String,
//    val description: String?,
//    val url: String,
//    val status: String // pending | succeeded | failed
//)
//
//interface QuickPayApi {
//    @POST("/v1/links")
//    suspend fun createLink(@Body body: CreateLinkReq): Response<CreateLinkRes>
//
//    @GET("/v1/orders/{id}")
//    suspend fun getOrder(@Path("id") id: String): Response<OrderDto>
//
//    @POST("/v1/dev/orders/{id}/mark")
//    suspend fun markOrder(
//        @Path("id") id: String,
//        @Query("status") status: String
//    ): Response<Unit>
//}

package com.quickpay.app.data.remote

import com.quickpay.app.data.remote.dto.CreateLinkReq
import com.quickpay.app.data.remote.dto.OrderDto
import com.quickpay.app.data.remote.dto.PaymentLinkDto
import retrofit2.http.*

interface QuickPayApi {
    @POST("/v1/links")
    suspend fun createLink(@Body body: CreateLinkReq): PaymentLinkDto

    @GET("/v1/orders/{id}")
    suspend fun getOrder(@Path("id") id: String): OrderDto

    @POST("/v1/dev/orders/{id}/mark")
    suspend fun markOrder(
        @Path("id") id: String,
        @Query("status") status: String
    )
}