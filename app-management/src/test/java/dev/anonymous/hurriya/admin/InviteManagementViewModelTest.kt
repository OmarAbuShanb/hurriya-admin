package dev.anonymous.hurriya.admin

import app.cash.turbine.test
import dev.anonymous.hurriya.admin.domain.models.Invitation
import dev.anonymous.hurriya.admin.domain.usecase.invitation.DeleteInvitationUseCase
import dev.anonymous.hurriya.admin.domain.usecase.invitation.GenerateInvitationUseCase
import dev.anonymous.hurriya.admin.domain.usecase.invitation.GetInvitationsUseCase
import dev.anonymous.hurriya.admin.presentation.screens.super_admin.invite_management.InviteManagementViewModel
import dev.anonymous.hurriya.admin.presentation.screens.super_admin.invite_management.InviteManagementViewModel.ResultState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InviteManagementViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: InviteManagementViewModel
    private val getInvitationsUseCase = mockk<GetInvitationsUseCase>(relaxed = true)
    private val deleteInvitationUseCase = mockk<DeleteInvitationUseCase>()
    private val generateInvitationUseCase = mockk<GenerateInvitationUseCase>()

    @Before
    fun setup() {
        coEvery { getInvitationsUseCase() } returns flow {}
        viewModel = InviteManagementViewModel(
            getInvitationsUseCase,
            deleteInvitationUseCase,
            generateInvitationUseCase
        )
    }

    @Test
    fun `generateInvite emits Loading then Success then Idle`() = runTest {
        val fakeInvitation = Invitation("id1", "code", "hint", "role")

        coEvery { generateInvitationUseCase("hint", "role") } returns Result.success(fakeInvitation)

        viewModel.generateInvite("hint", "role")

        viewModel.generateInviteState.test {
            assertEquals(ResultState.Loading, awaitItem())
            assertEquals(ResultState.Success(fakeInvitation), awaitItem())
            assertEquals(ResultState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `generateInvite emits Loading then Error`() = runTest {
        coEvery {
            generateInvitationUseCase(
                "hint",
                "role"
            )
        } returns Result.failure(Exception("فشل"))

        viewModel.generateInvite("hint", "role")

        viewModel.generateInviteState.test {
            assertEquals(ResultState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is ResultState.Error)
            assertEquals("فشل", (error as ResultState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `generateInvitationUseCase should throw`() {
        coEvery {
            generateInvitationUseCase(
                any(),
                any()
            )
        } throws Exception("UseCase error")

        assertThrows(Exception::class.java) {
            runTest {
                generateInvitationUseCase("hint", "role")
            }
        }
    }

    @Test
    fun `deleteInvitation emits Loading then Success then Idle`() = runTest {
        coEvery { deleteInvitationUseCase("id2") } returns Result.success(Unit)

        viewModel.deleteInvitation("id2")

        viewModel.deleteInviteState.test {
            assertEquals(ResultState.Loading, awaitItem())
            assertEquals(ResultState.Success(Unit), awaitItem())
            assertEquals(ResultState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteInvitation emits Error on failure`() = runTest {
        coEvery { deleteInvitationUseCase("id3") } returns Result.failure(Exception())

        viewModel.deleteInvitation("id3")
        advanceUntilIdle()

        val state = viewModel.deleteInviteState.value
        assertTrue(state is ResultState.Error)
        assertEquals("ليس لديك صلاحية حذف الدعوة.", (state as ResultState.Error).message)
    }
}
