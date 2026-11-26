
package com.example.tiketiin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tiketiin.data.local.dao.EventDao
import com.example.tiketiin.data.local.dao.HotelDao
import com.example.tiketiin.data.local.dao.OrderDao
import com.example.tiketiin.data.local.entity.CachedEventEntity
import com.example.tiketiin.data.local.entity.HotelEntity
import com.example.tiketiin.data.local.entity.OrderEntity

@Database(
    entities = [
        CachedEventEntity::class,
        HotelEntity::class,
        OrderEntity::class
    ],
    version = 7,
    exportSchema = true
)
abstract class TiketiinDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun hotelDao(): HotelDao
    abstract fun orderDao(): OrderDao
}
