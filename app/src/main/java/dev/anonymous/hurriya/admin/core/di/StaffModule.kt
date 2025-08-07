package dev.anonymous.hurriya.admin.core.di

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.anonymous.hurriya.admin.data.remote.datasource.StaffRemoteDataSource
import dev.anonymous.hurriya.admin.data.repository.StaffRepositoryImpl
import dev.anonymous.hurriya.admin.domain.repository.StaffRepository
import dev.anonymous.hurriya.admin.domain.usecase.staff.DeleteStaffUseCase
import dev.anonymous.hurriya.admin.domain.usecase.staff.FetchStaffListUseCase
import dev.anonymous.hurriya.admin.domain.usecase.staff.ListenToPresenceUpdatesUseCase
import dev.anonymous.hurriya.admin.domain.usecase.staff.UpdateStaffRoleUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StaffModule {

    @Provides
    @Singleton
    fun provideStaffRemoteDataSource(
        firestore: FirebaseFirestore,
        realtimeDb: FirebaseDatabase,
        functions: FirebaseFunctions
    ): StaffRemoteDataSource = StaffRemoteDataSource(
        firestore,
        realtimeDb,
        functions
    )

    @Provides
    @Singleton
    fun provideStaffRepository(
        remoteDataSource: StaffRemoteDataSource
    ): StaffRepository = StaffRepositoryImpl(remoteDataSource)

    @Provides
    @Singleton
    fun provideFetchStaffListUseCase(
        repository: StaffRepository
    ): FetchStaffListUseCase = FetchStaffListUseCase(repository)

    @Provides
    @Singleton
    fun provideListenToPresenceUpdatesUseCase(
        repository: StaffRepository
    ): ListenToPresenceUpdatesUseCase = ListenToPresenceUpdatesUseCase(repository)

    @Provides
    @Singleton
    fun provideUpdateStaffRoleUseCase(
        repository: StaffRepository
    ): UpdateStaffRoleUseCase = UpdateStaffRoleUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteStaffUseCase(
        repository: StaffRepository
    ): DeleteStaffUseCase = DeleteStaffUseCase(repository)
}
