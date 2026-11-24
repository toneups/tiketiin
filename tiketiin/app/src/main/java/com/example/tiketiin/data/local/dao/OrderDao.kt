
package com.example.tiketiin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tiketiin.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("""
        SELECT * FROM cached_orders
        WHERE buyerId = :buyerId
        ORDER BY createdAt DESC
    """)
    fun observeBuyerOrders(buyerId: String): Flow<List<OrderEntity>>

    @Query("""
        SELECT * FROM cached_orders
        WHERE buyerId = :buyerId
        ORDER BY createdAt DESC
    """)
    suspend fun getBuyerOrders(buyerId: String): List<OrderEntity>

    @Query("""
        SELECT * FROM cached_orders
        WHERE sellerId = :sellerId
        ORDER BY createdAt DESC
    """)
    suspend fun getSellerOrders(sellerId: String): List<OrderEntity>

    @Query("SELECT * FROM cached_orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Int): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("DELETE FROM cached_orders")
    suspend fun clearOrders()

    @Query("SELECT COUNT(*) FROM cached_orders")
    suspend fun countOrders(): Int

    @Query("DELETE FROM cached_orders WHERE buyerId = :buyerId")
    suspend fun clearBuyerOrders(buyerId: String)

    @Query("DELETE FROM cached_orders WHERE sellerId = :sellerId")
    suspend fun clearSellerOrders(sellerId: String)

    @Transaction
    suspend fun replaceBuyerOrders(
        buyerId: String,
        orders: List<OrderEntity>
    ) {
        clearBuyerOrders(buyerId)
        insertOrders(orders)
    }

    @Transaction
    suspend fun replaceSellerOrders(
        sellerId: String,
        orders: List<OrderEntity>
    ) {
        clearSellerOrders(sellerId)
        insertOrders(orders)
    }

}
