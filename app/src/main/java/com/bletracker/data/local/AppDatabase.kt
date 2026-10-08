package com.bletracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bletracker.data.local.dao.DeviceDao
import com.bletracker.data.local.entity.DeviceEntity

@Database(entities = [DeviceEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
}
