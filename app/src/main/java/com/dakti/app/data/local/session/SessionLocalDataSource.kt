package com.dakti.app.data.local.session

import kotlinx.coroutines.flow.StateFlow

interface SessionLocalDataSource {
    val authenticatedUserId: StateFlow<String?>

    fun setAuthenticatedUserId(userId: String)
    fun clearAuthenticatedUserId()
}
