package dev.craftanything.app.saf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log

data class DocumentMetadata(
    val uri: Uri,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
)

class StorageManager(private val context: Context) {

    /**
     * Persists read and write permissions for a user-selected SAF tree URI.
     */
    fun persistTreePermission(treeUri: Uri): Boolean {
        return try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(treeUri, flags)
            Log.i(TAG, "Persisted permission for tree URI: $treeUri")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to persist permission for tree URI: $treeUri", e)
            false
        }
    }

    /**
     * Lists child documents inside an SAF folder tree URI.
     */
    fun listFolder(treeUri: Uri): List<DocumentMetadata> {
        val results = mutableListOf<DocumentMetadata>()
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)

        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )

        try {
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val sizeIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                val modIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                while (cursor.moveToNext()) {
                    val childDocId = cursor.getString(idIdx)
                    val name = cursor.getString(nameIdx)
                    val mime = cursor.getString(mimeIdx)
                    val size = cursor.getLong(sizeIdx)
                    val mod = cursor.getLong(modIdx)
                    val isDir = mime == DocumentsContract.Document.MIME_TYPE_DIR
                    val childUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childDocId)

                    results.add(DocumentMetadata(childUri, name, isDir, size, mod))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing SAF tree: $treeUri", e)
        }

        return results
    }

    companion object {
        private const val TAG = "StorageManager"
    }
}
