
package com.example.tiketiin.data.local

import com.example.tiketiin.data.local.dao.OrderDao
import com.example.tiketiin.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow


class OrderLocalDataSource(
    private val orderDao: OrderDao
) {

    fun observeBuyerOrders(buyerId: String): Flow<List<OrderEntity>> {
        return orderDao.observeBuyerOrders(buyerId)
    }

    suspend fun getBuyerOrders(buyerId: String): List<OrderEntity> {
        return orderDao.getBuyerOrders(buyerId)
    }

    suspend fun getSellerOrders(sellerId: String): List<OrderEntity> {
        return orderDao.getSellerOrders(sellerId)
    }

    suspend fun getOrderById(orderId: Int): OrderEntity? {
        return orderDao.getOrderById(orderId)
    }

    suspend fun saveOrders(orders: List<OrderEntity>) {
        orderDao.insertOrders(orders)
    }

    suspend fun saveOrder(order: OrderEntity) {
        orderDao.insertOrder(order)
    }

    suspend fun updateOrder(order: OrderEntity) {
        orderDao.updateOrder(order)
    }

    suspend fun clearOrders() {
        orderDao.clearOrders()
    }

    suspend fun countOrders(): Int {
        return orderDao.countOrders()
    }

    suspend fun replaceBuyerOrders(
        buyerId: String,
        orders: List<OrderEntity>
    ) {
        orderDao.replaceBuyerOrders(buyerId, orders)
    }

    suspend fun replaceSellerOrders(
        sellerId: String,
        orders: List<OrderEntity>
    ) {
        orderDao.replaceSellerOrders(sellerId, orders)
    }
}
