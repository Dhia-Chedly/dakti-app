package com.dakti.app.domain.usecase

import com.dakti.app.domain.location.UserLocation
import com.dakti.app.domain.location.UserLocationProvider
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val userLocationProvider: UserLocationProvider
) {
    suspend operator fun invoke(): UserLocation? = userLocationProvider.getCurrentLocation()
}
