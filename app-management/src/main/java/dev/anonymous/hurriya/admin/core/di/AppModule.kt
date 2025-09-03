package dev.anonymous.hurriya.admin.core.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.anonymous.hurriya.admin.core.utils.NetworkChecker
import dev.anonymous.hurriya.admin.data.local.cleaner.LocalUserDataCleaner
import dev.anonymous.hurriya.admin.data.local.database.AppDatabase
import dev.anonymous.hurriya.admin.data.local.datastore.UserPreferences
import dev.anonymous.hurriya.admin.data.remote.datasource.AuthRemoteDataSource
import dev.anonymous.hurriya.admin.data.remote.datasource.StaffPresenceRemoteDataSource
import dev.anonymous.hurriya.admin.data.repository.AuthRepositoryImpl
import dev.anonymous.hurriya.admin.domain.presence.PresenceManager
import dev.anonymous.hurriya.admin.domain.repository.AuthRepository
import dev.anonymous.hurriya.admin.domain.usecase.auth.SignOutUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNetworkChecker(@ApplicationContext context: Context): NetworkChecker {
        return NetworkChecker(context)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun providePresenceManager(
        auth: FirebaseAuth,
        db: FirebaseDatabase
    ): PresenceManager = StaffPresenceRemoteDataSource(auth, db)

    @Provides
    @Singleton
    fun provideLocalUserDataCleaner(
        appDatabase: AppDatabase,
        userPreferences: UserPreferences,
        @ApplicationContext context: Context
    ): LocalUserDataCleaner = LocalUserDataCleaner(
        appDatabase,
        userPreferences,
        context
    )

    @Provides
    @Singleton
    fun provideAuthRemoteDataSource(
        auth: FirebaseAuth,
        functions: FirebaseFunctions
    ): AuthRemoteDataSource = AuthRemoteDataSource(auth, functions)

    @Provides
    @Singleton
    fun provideAuthRepository(
        authRemoteDataSource: AuthRemoteDataSource,
    ): AuthRepository = AuthRepositoryImpl(authRemoteDataSource)

    @Provides
    @Singleton
    fun provideSignOutUseCase(
        authRepository: AuthRepository,
        localUserDataCleaner: LocalUserDataCleaner,
        presenceManager: PresenceManager
    ): SignOutUseCase = SignOutUseCase(
        authRepository,
        localUserDataCleaner,
        presenceManager
    )
}
