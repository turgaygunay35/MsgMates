package com.msgmates.app.config

object Constants {
    // Navigation Constants
    const val NAV_CHATS = "chats"
    const val NAV_CONTACTS = "contacts"
    const val NAV_JOURNAL = "journal"
    const val NAV_MENU = "menu"

    // Menu Constants
    const val MENU_SETTINGS = "settings"
    const val MENU_WEB_MSGMATES = "web_msgmates"
    const val MENU_DISASTER_MODE = "disaster_mode"
    const val MENU_MESSAGE_CAPSULE = "message_capsule"
    const val MENU_QUICK_NOTES = "quick_notes"
    const val MENU_ARCHIVE = "archive"
    const val MENU_GAMES = "games"
    const val MENU_COMING_SOON = "coming_soon"

    // Game Constants
    const val GAME_SUDOKU = "sudoku"
    const val GAME_TIC_TAC_TOE = "tic_tac_toe"
    const val GAME_NINE_MENS_MORRIS = "nine_mens_morris"
    const val GAME_HANGMAN = "hangman"
    const val GAME_SNAKE = "snake"
    const val GAME_MEMORY = "memory"
    const val GAME_2048 = "2048"
    const val GAME_TETRIS = "tetris"

    // Disaster Mode Constants
    const val DISASTER_ALIVE_BUTTON_DURATION_MS = 60000L
    const val DISASTER_BLE_SCAN_DURATION_MS = 5000L
    const val DISASTER_BLE_ADVERTISE_DURATION_MS = 1000L

    // SOS Messages
    val SOS_MESSAGES = listOf(
        "Yardım edin!",
        "Acil durum!",
        "Konumum: ",
        "Güvende değilim!",
        "Acil tıbbi yardım!",
        "Yangın var!",
        "Deprem!",
        "Sel tehlikesi!"
    )

    // Permission Request Codes
    const val PERMISSION_CAMERA = 1001
    const val PERMISSION_RECORD_AUDIO = 1002
    const val PERMISSION_READ_EXTERNAL_STORAGE = 1003
    const val PERMISSION_WRITE_EXTERNAL_STORAGE = 1004
    const val PERMISSION_BLUETOOTH = 1005
    const val PERMISSION_LOCATION = 1006
    const val PERMISSION_NOTIFICATIONS = 1007

    // Request Codes
    const val REQUEST_CAMERA = 2001
    const val REQUEST_GALLERY = 2002
    const val REQUEST_FILE_PICKER = 2003
    const val REQUEST_VIDEO_RECORDER = 2004

    // Notification IDs
    const val NOTIFICATION_DISASTER_MODE = 1001
    const val NOTIFICATION_MESSAGE_CAPSULE = 1002
    const val NOTIFICATION_MESSAGE_RECEIVED = 1003
    const val NOTIFICATION_CALL_INCOMING = 1004

    // Fragment Tags
    const val TAG_CHATS_FRAGMENT = "chats_fragment"
    const val TAG_CONTACTS_FRAGMENT = "contacts_fragment"
    const val TAG_JOURNAL_FRAGMENT = "journal_fragment"
    const val TAG_MENU_FRAGMENT = "menu_fragment"
    const val TAG_DISASTER_FRAGMENT = "disaster_fragment"
    const val TAG_SETTINGS_FRAGMENT = "settings_fragment"

    // Animation Durations
    const val ANIMATION_DURATION_SHORT = 200L
    const val ANIMATION_DURATION_MEDIUM = 300L
    const val ANIMATION_DURATION_LONG = 500L

    // UI Constants
    const val BOTTOM_NAV_HEIGHT = 56
    const val TOOLBAR_HEIGHT = 56
    const val FAB_MARGIN = 16

    // Validation Constants
    const val PHONE_NUMBER_MIN_LENGTH = 10
    const val PHONE_NUMBER_MAX_LENGTH = 15
    const val USERNAME_MIN_LENGTH = 3
    const val USERNAME_MAX_LENGTH = 20
    const val PASSWORD_MIN_LENGTH = 8
    const val PASSWORD_MAX_LENGTH = 50

    // Error Messages
    const val ERROR_NETWORK = "Ağ bağlantısı hatası"
    const val ERROR_SERVER = "Sunucu hatası"
    const val ERROR_UNAUTHORIZED = "Yetkilendirme hatası"
    const val ERROR_FORBIDDEN = "Erişim reddedildi"
    const val ERROR_NOT_FOUND = "Kaynak bulunamadı"
    const val ERROR_VALIDATION = "Geçersiz veri"
    const val ERROR_UNKNOWN = "Bilinmeyen hata"
}
