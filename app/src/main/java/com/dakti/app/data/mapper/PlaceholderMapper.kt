package com.dakti.app.data.mapper

import com.dakti.app.data.remote.dto.PlaceholderDto

object PlaceholderMapper {
    fun toLabel(dto: PlaceholderDto): String = dto.title
}
