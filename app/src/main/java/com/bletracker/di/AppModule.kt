package com.bletracker.di

import androidx.room.Room
import com.bletracker.data.ble.BleScannerDataSource
import com.bletracker.data.local.AppDatabase
import com.bletracker.data.repository.BleRepositoryImpl
import com.bletracker.data.repository.HistoryRepositoryImpl
import com.bletracker.domain.repository.BleRepository
import com.bletracker.domain.repository.HistoryRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "ble_tracker.db"
        ).fallbackToDestructiveMigration().build()
    }
    single { get<AppDatabase>().deviceDao() }
}

val repositoryModule = module {
    single { BleScannerDataSource(androidContext()) }
    single<HistoryRepository> { HistoryRepositoryImpl(get()) }
    single<BleRepository> { BleRepositoryImpl(get(), get()) }
}

val appModules = listOf(databaseModule, repositoryModule)
