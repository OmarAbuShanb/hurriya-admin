package dev.anonymous.hurriya.admin.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.anonymous.hurriya.admin.data.local.dao.InvitationDao
import dev.anonymous.hurriya.admin.data.local.database.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideInvitationDao(db: AppDatabase): InvitationDao = db.invitationDao()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "hurriya_database"
        ).fallbackToDestructiveMigration(false)
            .build()
    }
}