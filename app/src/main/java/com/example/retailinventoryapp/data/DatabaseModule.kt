package com.example.retailinventoryapp.data

import android.content.Context
import androidx.room.Room
import com.example.retailinventoryapp.data.daos.ProductDao
import com.example.retailinventoryapp.data.daos.SaleDao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // This makes the dependencies live as long as the app
object DatabaseModule {

    @Provides
    @Singleton // We only want ONE database instance for the whole app
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "retail_database" // The name of the SQLite file
        )
            .fallbackToDestructiveMigration() // Useful during development: wipes DB if schema changes
            .build()
    }

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao {
        // Hilt takes the database provided above and extracts the DAO
        return database.productDao()
    }
    @Provides
    fun provideSaleDao(database: AppDatabase): SaleDao {
        // Hilt takes the database provided above and extracts the DAO
        return database.saleDao()
    }

    // When you add SalesDao, you will add another @Provides here
}
