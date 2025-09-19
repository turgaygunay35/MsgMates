package com.msgmates.app.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Future migration: Add new columns or tables
        // Example: Add message reactions table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS message_reactions (
                id TEXT PRIMARY KEY NOT NULL,
                messageId TEXT NOT NULL,
                userId TEXT NOT NULL,
                emoji TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(messageId) REFERENCES messages(id) ON DELETE CASCADE
            )
        """
        )

        // Add index for performance
        database.execSQL("CREATE INDEX IF NOT EXISTS index_message_reactions_messageId ON message_reactions(messageId)")
    }
}
