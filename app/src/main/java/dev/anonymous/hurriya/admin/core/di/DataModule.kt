package dev.anonymous.hurriya.admin.core.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.anonymous.hurriya.admin.core.utils.NetworkChecker
import dev.anonymous.hurriya.admin.data.local.dao.InvitationDao
import dev.anonymous.hurriya.admin.data.local.datasource.InvitationLocalDataSource
import dev.anonymous.hurriya.admin.data.remote.datasource.InvitationRemoteDataSource
import dev.anonymous.hurriya.admin.data.remote.mediator.InvitationRemoteMediator
import dev.anonymous.hurriya.admin.data.repository.InvitationRepositoryImpl
import dev.anonymous.hurriya.admin.domain.repository.InvitationRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideInvitationLocalDataSource(
        dao: InvitationDao
    ): InvitationLocalDataSource {
        return InvitationLocalDataSource(dao)
    }

    @Provides
    @Singleton
    fun provideInvitationRemoteDataSource(
        firestore: FirebaseFirestore,
        functions: FirebaseFunctions
    ): InvitationRemoteDataSource {
        return InvitationRemoteDataSource(firestore,functions)
    }

    @Provides
    @Singleton
    fun provideInvitationRemoteMediator(
        remoteDataSource: InvitationRemoteDataSource,
        localDataSource: InvitationLocalDataSource,
        networkChecker: NetworkChecker
    ): InvitationRemoteMediator {
        return InvitationRemoteMediator(remoteDataSource, localDataSource, networkChecker)
    }

    @Provides
    @Singleton
    fun provideInvitationRepository(
        remoteDataSource: InvitationRemoteDataSource,
        localDataSource: InvitationLocalDataSource,
        networkChecker: NetworkChecker
    ): InvitationRepository {
        return InvitationRepositoryImpl(remoteDataSource, localDataSource, networkChecker)
    }
}