package dev.anonymous.hurriya.admin.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.anonymous.hurriya.admin.domain.repository.InvitationRepository
import dev.anonymous.hurriya.admin.domain.usecase.invitation.DeleteInvitationUseCase
import dev.anonymous.hurriya.admin.domain.usecase.invitation.GetInvitationsUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideGetInvitationsUseCase(repo: InvitationRepository): GetInvitationsUseCase {
        return GetInvitationsUseCase(repo)
    }

    @Provides
    @Singleton
    fun provideDeleteInvitationUseCase(repo: InvitationRepository): DeleteInvitationUseCase {
        return DeleteInvitationUseCase(repo)
    }
}