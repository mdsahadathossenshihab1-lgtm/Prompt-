package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException

class FirebaseManager(private val context: Context) {

    companion object {
        private const val TAG = "FirebaseManager"
        const val COLLECTION_PROMPTS = "prompts"
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) {
                continuation.resume(result, null)
            }
        }
        addOnFailureListener { exception ->
            if (continuation.isActive) {
                continuation.resumeWithException(exception)
            }
        }
        addOnCanceledListener {
            if (continuation.isActive) {
                continuation.cancel()
            }
        }
    }

    fun isFirebaseInitialized(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not initialized: ${e.message}")
            false
        }
    }

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            if (isFirebaseInitialized()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring Firestore instance: ${e.message}", e)
            null
        }
    }

    suspend fun savePromptToCloud(model: FirestorePromptModel): Result<String> = withContext(Dispatchers.IO) {
        val firestore = getFirestore()
            ?: return@withContext Result.failure(
                IllegalStateException("Firebase is not initialized. Please ensure google-services.json is added to the app module.")
            )

        try {
            val collection = firestore.collection(COLLECTION_PROMPTS)
            val docRef = if (model.id.isNotBlank()) {
                collection.document(model.id)
            } else {
                collection.document()
            }

            docRef.set(model.toMap()).awaitTask()
            Log.d(TAG, "Successfully saved prompt to Firestore: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save prompt to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deletePromptFromCloud(documentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val firestore = getFirestore()
            ?: return@withContext Result.failure(
                IllegalStateException("Firebase is not initialized.")
            )

        try {
            firestore.collection(COLLECTION_PROMPTS).document(documentId).delete().awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete prompt from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun getCloudPromptsFlow(): Flow<List<FirestorePromptModel>> = callbackFlow {
        val firestore = getFirestore()
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_PROMPTS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore listen error: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data
                        if (data != null) {
                            FirestorePromptModel.fromMap(doc.id, data)
                        } else null
                    }
                    trySend(list)
                }
            }

        awaitClose {
            listener.remove()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        val firestore = getFirestore()
            ?: return@withContext Result.failure(
                IllegalStateException("Firebase is not configured yet. Add google-services.json to connect to Cloud Firestore.")
            )

        try {
            // Check collection metadata/accessibility
            val snapshot = firestore.collection(COLLECTION_PROMPTS).limit(1).get().awaitTask()
            Result.success("Connected to Firebase Firestore! Collection '$COLLECTION_PROMPTS' accessible.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
