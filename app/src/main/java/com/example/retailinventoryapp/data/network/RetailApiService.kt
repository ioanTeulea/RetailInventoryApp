package com.example.retailinventoryapp.data.network

import com.example.retailinventoryapp.data.repository.*
import retrofit2.Response
import retrofit2.http.*

interface RetailApiService {

    // ========== PRODUCT ENDPOINTS ==========

    @GET("products")
    suspend fun getAllProducts(): Response<BaseResponse<List<ProductResponse>>>

    @GET("products/barcode/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): Response<BaseResponse<ProductResponse>>

    @GET("products/search")
    suspend fun searchProducts(
        @Query("query") query: String
    ): Response<BaseResponse<List<ProductResponse>>>

    @POST("products/{id}/stock")
    suspend fun updateStock(
        @Path("id") productId: Long,
        @Body request: StockUpdateRequest
    ): Response<Void>

    // ========== SALE ENDPOINTS ==========

    @GET("sales/stats/today")
    suspend fun getTodayStats(): Response<BaseResponse<TodayStatsData>>

    @POST("sales")
    suspend fun createSale(
        @Body request: SaleRequest
    ): Response<BaseResponse<SaleResponse>>

    @POST("sales/{id}/refund")
    suspend fun refundSale(
        @Path("id") saleId: Long,
        @Body request: RefundRequest
    ): Response<Void>

    // ========== STORE ENDPOINTS ==========

    @GET("stores/{id}")
    suspend fun getStoreById(
        @Path("id") storeId: Long
    ): Response<BaseResponse<StoreDetailData>>

    @GET("stores/{id}/manager")
    suspend fun getStoreManager(
        @Path("id") storeId: Long
    ): Response<BaseResponse<ManagerData>>
}

/**
 * Standard wrapper for API responses
 */
data class BaseResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T
)

/**
 * Response model for sale creation
 */
data class SaleResponse(
    val id: Long
)