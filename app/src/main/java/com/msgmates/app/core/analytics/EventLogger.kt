package com.msgmates.app.core.analytics

import android.util.Log

object EventLogger {

    fun log(name: String, params: Map<String, Any?> = emptyMap()) {
        Log.d("Event", "name=$name params=$params")
    }

    // Convenience methods for common events
    fun logAppBarClick(action: String) {
        log("appbar_click_$action")
    }

    fun logFilterSelect(filterKey: String) {
        log("filter_select_$filterKey")
    }

    fun logStatusCapsuleRetry() {
        log("status_capsule_retry")
    }

    fun logSearchEnter() {
        log("search_enter")
    }

    fun logSearchExit() {
        log("search_exit")
    }

    fun logMultiSelectEnter() {
        log("multi_select_enter")
    }

    fun logMultiSelectExit() {
        log("multi_select_exit")
    }

    fun logCallStart(callType: String) {
        log("call_start", mapOf("type" to callType))
    }

    fun logCallEnd(callType: String, duration: Long) {
        log("call_end", mapOf("type" to callType, "duration_ms" to duration))
    }

    fun logMenuOpen(menuType: String) {
        log("menu_open_$menuType")
    }

    fun logQuickAction(action: String) {
        log("quick_action_$action")
    }

    fun logFilterReorder(filterKey: String, direction: String) {
        log("filter_reorder", mapOf("filter" to filterKey, "direction" to direction))
    }

    fun logFilterToggleVisibility(filterKey: String, visible: Boolean) {
        log("filter_toggle_visibility", mapOf("filter" to filterKey, "visible" to visible))
    }

    // Messaging events
    fun logMessageSend(conversationId: String, messageType: String) {
        log("message_send", mapOf("conversation_id" to conversationId, "type" to messageType))
    }

    fun logMessageReceived(conversationId: String, messageType: String) {
        log("message_received", mapOf("conversation_id" to conversationId, "type" to messageType))
    }

    fun logMessageDelivered(conversationId: String, messageId: String) {
        log("message_delivered", mapOf("conversation_id" to conversationId, "message_id" to messageId))
    }

    fun logMessageRead(conversationId: String, messageId: String) {
        log("message_read", mapOf("conversation_id" to conversationId, "message_id" to messageId))
    }

    // Upload/Download events
    fun logUploadStart(fileType: String, fileSize: Long) {
        log("upload_start", mapOf("file_type" to fileType, "file_size" to fileSize))
    }

    fun logUploadEnd(fileType: String, fileSize: Long, success: Boolean, duration: Long) {
        log(
            "upload_end",
            mapOf("file_type" to fileType, "file_size" to fileSize, "success" to success, "duration_ms" to duration)
        )
    }

    fun logDownloadStart(fileType: String, fileSize: Long) {
        log("download_start", mapOf("file_type" to fileType, "file_size" to fileSize))
    }

    fun logDownloadEnd(fileType: String, fileSize: Long, success: Boolean, duration: Long) {
        log(
            "download_end",
            mapOf("file_type" to fileType, "file_size" to fileSize, "success" to success, "duration_ms" to duration)
        )
    }

    // Auth events
    fun logLoginSuccess(method: String) {
        log("login_success", mapOf("method" to method))
    }

    fun logLoginFailure(method: String, error: String) {
        log("login_failure", mapOf("method" to method, "error" to error))
    }

    fun logLogout() {
        log("logout")
    }

    fun logTokenRefresh(success: Boolean) {
        log("token_refresh", mapOf("success" to success))
    }

    // WebSocket events
    fun logWsConnect() {
        log("ws_connect")
    }

    fun logWsDisconnect() {
        log("ws_disconnect")
    }

    fun logWsReconnect(attempt: Int) {
        log("ws_reconnect", mapOf("attempt" to attempt))
    }

    // Error events
    fun logError(errorType: String, errorMessage: String, context: String) {
        log("error", mapOf("type" to errorType, "message" to errorMessage, "context" to context))
    }

    fun logCrash(exception: String, stackTrace: String) {
        log("crash", mapOf("exception" to exception, "stack_trace" to stackTrace))
    }
}
