package com.dakti.app.presentation.assistant

import com.dakti.app.domain.usecase.ExecuteAssistantActionUseCase
import com.dakti.app.domain.usecase.GetAssistantQuickActionsUseCase
import com.dakti.app.domain.usecase.GetAssistantSuggestedPromptsUseCase
import com.dakti.app.domain.usecase.InterpretAssistantRequestUseCase
import com.dakti.app.testutil.FakeAssistantRepository
import com.dakti.app.testutil.MainDispatcherRule
import com.dakti.app.testutil.TestData
import com.dakti.app.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val assistantRepository = FakeAssistantRepository()

    private fun createViewModel(): AssistantViewModel =
        AssistantViewModel(
            interpretAssistantRequestUseCase = InterpretAssistantRequestUseCase(assistantRepository),
            executeAssistantActionUseCase = ExecuteAssistantActionUseCase(assistantRepository),
            getAssistantQuickActionsUseCase = GetAssistantQuickActionsUseCase(assistantRepository),
            getAssistantSuggestedPromptsUseCase = GetAssistantSuggestedPromptsUseCase(assistantRepository)
        )

    @Test
    fun sendCurrentMessage_blankInput_setsError() = runTest {
        val viewModel = createViewModel()

        viewModel.sendCurrentMessage()
        advanceUntilIdle()

        assertEquals("Type a message first.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun sendMessage_success_addsConversationAndPendingAction() = runTest {
        assistantRepository.interpretResult = Resource.Success(TestData.assistantReply(withProposal = true))
        val viewModel = createViewModel()
        viewModel.onInputChanged("Organize a match")

        viewModel.sendCurrentMessage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.messages.size)
        assertNotNull(state.pendingActionProposal)
        assertNull(state.errorMessage)
    }

    @Test
    fun sendMessage_whileLoading_ignoresSecondSend() = runTest {
        assistantRepository.interpretResult = Resource.Loading
        val viewModel = createViewModel()
        viewModel.onInputChanged("First message")

        viewModel.sendCurrentMessage()
        viewModel.sendSuggestedPrompt("Second message")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.messages.size)
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun confirmPendingAction_whenExecuting_ignoresDuplicateCall() = runTest {
        assistantRepository.interpretResult = Resource.Success(TestData.assistantReply(withProposal = true))
        assistantRepository.executeResult = Resource.Loading
        val viewModel = createViewModel()
        viewModel.sendSuggestedPrompt("Organize")
        advanceUntilIdle()

        viewModel.confirmPendingAction()
        viewModel.confirmPendingAction()
        advanceUntilIdle()

        assertEquals(1, assistantRepository.executeCallCount)
    }

    @Test
    fun sendMessage_withoutProposal_clearsPreviousPendingAction() = runTest {
        assistantRepository.interpretResult = Resource.Success(TestData.assistantReply(withProposal = true))
        val viewModel = createViewModel()
        viewModel.sendSuggestedPrompt("First")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.pendingActionProposal)

        assistantRepository.interpretResult = Resource.Success(TestData.assistantReply(withProposal = false))
        viewModel.sendSuggestedPrompt("Second")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingActionProposal)
    }
}
