package com.msgmates.app.ui.common

import com.msgmates.app.R

/**
 * Maps ConnectionStatus to StatusCapsuleUi for consistent status display
 * across the application.
 */
object StatusCapsuleMapper {

    /**
     * Maps a ConnectionStatus to its corresponding StatusCapsuleUi
     * @param status The connection status to map
     * @return StatusCapsuleUi with appropriate label and icon
     */
    fun mapStatusToUi(status: ConnectionStatus): StatusCapsuleUi = when (status) {
        ConnectionStatus.LIVE -> StatusCapsuleUi(
            status = ConnectionStatus.LIVE,
            label = "Canlı",
            iconRes = R.drawable.ic_status_live
        )
        ConnectionStatus.SYNC -> StatusCapsuleUi(
            status = ConnectionStatus.SYNC,
            label = "Senkron",
            iconRes = R.drawable.ic_status_sync
        )
        ConnectionStatus.OFFLINE -> StatusCapsuleUi(
            status = ConnectionStatus.OFFLINE,
            label = "Çevrimdışı",
            iconRes = R.drawable.ic_status_offline
        )
        ConnectionStatus.DISASTER -> StatusCapsuleUi(
            status = ConnectionStatus.DISASTER,
            label = "Afet",
            iconRes = R.drawable.ic_disaster
        )
    }
}
