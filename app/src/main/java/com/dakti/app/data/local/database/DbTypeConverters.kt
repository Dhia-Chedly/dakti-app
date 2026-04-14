package com.dakti.app.data.local.database

import androidx.room.TypeConverter
import com.dakti.app.domain.model.AISuggestionType
import com.dakti.app.domain.model.InvitationResponseStatus
import com.dakti.app.domain.model.MatchStatus
import com.dakti.app.domain.model.NotificationType
import com.dakti.app.domain.model.ReservationStatus
import com.dakti.app.domain.model.UserRole

class DbTypeConverters {

    @TypeConverter
    fun fromUserRole(value: UserRole?): String? = value?.name

    @TypeConverter
    fun toUserRole(value: String?): UserRole? = value?.let { enumValueOrNull<UserRole>(it) }

    @TypeConverter
    fun fromReservationStatus(value: ReservationStatus?): String? = value?.name

    @TypeConverter
    fun toReservationStatus(value: String?): ReservationStatus? =
        value?.let { enumValueOrNull<ReservationStatus>(it) }

    @TypeConverter
    fun fromMatchStatus(value: MatchStatus?): String? = value?.name

    @TypeConverter
    fun toMatchStatus(value: String?): MatchStatus? = value?.let { enumValueOrNull<MatchStatus>(it) }

    @TypeConverter
    fun fromInvitationStatus(value: InvitationResponseStatus?): String? = value?.name

    @TypeConverter
    fun toInvitationStatus(value: String?): InvitationResponseStatus? =
        value?.let { enumValueOrNull<InvitationResponseStatus>(it) }

    @TypeConverter
    fun fromNotificationType(value: NotificationType?): String? = value?.name

    @TypeConverter
    fun toNotificationType(value: String?): NotificationType? =
        value?.let { enumValueOrNull<NotificationType>(it) }

    @TypeConverter
    fun fromSuggestionType(value: AISuggestionType?): String? = value?.name

    @TypeConverter
    fun toSuggestionType(value: String?): AISuggestionType? =
        value?.let { enumValueOrNull<AISuggestionType>(it) }

    private inline fun <reified T : Enum<T>> enumValueOrNull(raw: String): T? {
        return runCatching { enumValueOf<T>(raw) }.getOrNull()
    }
}
