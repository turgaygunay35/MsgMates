package com.msgmates.app.data.local.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_TO_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add journal tables
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS journal_entries (
                id TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                isFavorite INTEGER NOT NULL DEFAULT 0,
                isArchived INTEGER NOT NULL DEFAULT 0,
                mood TEXT,
                tags TEXT
            )
        """
        )

        // Add indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_title ON journal_entries (title)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_createdAt ON journal_entries (createdAt)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_isFavorite ON journal_entries (isFavorite)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_isArchived ON journal_entries (isArchived)")

        // Add journal tags table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS journal_tags (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                color TEXT,
                createdAt INTEGER NOT NULL
            )
        """
        )

        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_journal_tags_name ON journal_tags (name)")

        // Add journal photos table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS journal_photos (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                entryId TEXT NOT NULL,
                uri TEXT NOT NULL,
                `order` INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL
            )
        """
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_photos_entryId ON journal_photos (entryId)")

        // Add journal entry tags cross reference table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS journal_entry_tags (
                entryId TEXT NOT NULL,
                tagId INTEGER NOT NULL,
                PRIMARY KEY(entryId, tagId),
                FOREIGN KEY(entryId) REFERENCES journal_entries(id) ON DELETE CASCADE,
                FOREIGN KEY(tagId) REFERENCES journal_tags(id) ON DELETE CASCADE
            )
        """
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entry_tags_entryId ON journal_entry_tags (entryId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entry_tags_tagId ON journal_entry_tags (tagId)")

        // Create FTS table for full-text search
        db.execSQL(
            """
            CREATE VIRTUAL TABLE IF NOT EXISTS journal_entries_fts USING fts4(
                content='journal_entries',
                title,
                content,
                tags
            )
        """
        )

        // Populate FTS table with existing data
        db.execSQL(
            """
            INSERT INTO journal_entries_fts(docid, title, content, tags)
            SELECT id, title, content, tags FROM journal_entries
        """
        )

        // Create triggers to keep FTS table in sync
        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS journal_entries_ai AFTER INSERT ON journal_entries BEGIN
                INSERT INTO journal_entries_fts(docid, title, content, tags)
                VALUES (new.id, new.title, new.content, new.tags);
            END
        """
        )

        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS journal_entries_ad AFTER DELETE ON journal_entries BEGIN
                DELETE FROM journal_entries_fts WHERE docid = old.id;
            END
        """
        )

        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS journal_entries_au AFTER UPDATE ON journal_entries BEGIN
                DELETE FROM journal_entries_fts WHERE docid = old.id;
                INSERT INTO journal_entries_fts(docid, title, content, tags)
                VALUES (new.id, new.title, new.content, new.tags);
            END
        """
        )
    }
}
