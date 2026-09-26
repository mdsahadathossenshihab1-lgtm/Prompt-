package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.firebase.FirebaseManager
import com.example.data.firebase.FirestorePromptModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Interface defining basic and advanced CRUD operations for storing and managing
 * AI generated prompts in Cloud Firestore.
 */
interface FirestorePromptRepository {
    /**
     * Create a new prompt in Cloud Firestore.
     * @param prompt The prompt model to store.
     * @return Result containing the created Firestore document ID.
     */
    suspend fun createPrompt(prompt: FirestorePromptModel): Result<String>

    /**
     * Retrieve a single prompt by its Firestore document ID.
     * @param documentId Document ID of the prompt.
     * @return Result containing the prompt model if found, or null if it doesn't exist.
     */
    suspend fun getPromptById(documentId: String): Result<FirestorePromptModel?>

    /**
     * Retrieve all saved prompts as a one-shot query, ordered by timestamp descending.
     * @return Result containing list of stored prompts.
     */
    suspend fun getAllPrompts(): Result<List<FirestorePromptModel>>

    /**
     * Observe real-time changes to the prompts collection in Cloud Firestore.
     * @return Flow emitting updated list of prompts whenever documents change.
     */
    fun getPromptsFlow(): Flow<List<FirestorePromptModel>>

    /**
     * Update an existing prompt in Cloud Firestore.
     * @param prompt The prompt model with updated values.
     * @return Result indicating success or failure.
     */
    suspend fun updatePrompt(prompt: FirestorePromptModel): Result<Unit>

    /**
     * Delete a prompt document by its ID.
     * @param documentId Document ID to delete.
     * @return Result indicating success or failure.
     */
    suspend fun deletePrompt(documentId: String): Result<Unit>

    /**
     * Delete all prompt documents from the Firestore collection.
     * @return Result indicating success or failure.
     */
    suspend fun deleteAllPrompts(): Result<Unit>
}

/**
 * Implementation of [FirestorePromptRepository] for storing generated AI prompts in Cloud Firestore
 * with database ID: "ai-studio-promptflowai-33eaf848-a774-4d54-9ed1-e99f251be592".
 */
class FirestorePromptRepositoryImpl(
    private val firebaseManager: FirebaseManager
) : FirestorePromptRepository {

    constructor(context: Context) : this(FirebaseManager(context))

    companion object {
        private const val TAG = "FirestorePromptRepo"
        const val COLLECTION_PROMPTS = FirebaseManager.COLLECTION_PROMPTS
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) {
                continuation.resume(result)
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

    private fun getFirestore(): FirebaseFirestore? = firebaseManager.getFirestore()

    override suspend fun createPrompt(prompt: FirestorePromptModel): Result<String> = withContext(Dispatchers.IO) {
        val firestore = getFirestore()
            ?: return@withContext Result.failure(
                IllegalStateException("Firebase is not initialized or offline.")
            )

        try {
            val collection = firestore.collection(COLLECTION_PROMPTS)
            val docRef = if (prompt.id.isNotBlank()) {
                collection.document(prompt.id)
            } else {
                collection.document()
            }

            val data = prompt.copy(id = docRef.id).toMap()
            docRef.set(data).awaitTask()
            Log.d(TAG, "Successfully created prompt with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating prompt in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getPromptById(documentId: String): Result<FirestorePromptModel?> = withContext(Dispatchers.IO) {
        if (documentId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Document ID cannot be blank."))
        }

        val firestore = getFirestore()
            ?: return@withContext Result.failure(
                IllegalStateException("Firebase is not initialized.")
            )

        try {
            val snapshot = firestore.collection(COLLECTION_PROMPTS).document(documentId).get().awaitTask()
            if (!snapshot.exists()) {
                Result.success(null)
            } else {
                val data = snapshot.data
                if (data != null) {
                    Result.success(FirestorePromptModel.fromMap(snapshot.id, data))
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching prompt $documentId: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getAllPrompts(): Result<List<FirestorePromptModel>> = withContext(Dispatchers.IO) {
        val firestore = getFirestore()
            ?: return@withContext Result.failure(
                IllegalStateException("Firebase is not initialized.")
            )

        try {
            val snapshot = firestore.collection(COLLECTION_PROMPTS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .awaitTask()

            val list = snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    FirestorePromptModel.fromMap(doc.id, data)
                } else null
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all prompts: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun getPromptsFlow(): Flow<List<FirestorePromptModel>> = callbackFlow {
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
                    Log.e(TAG, "Firestore listener error: ${error.message}", error)
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

    override suspend fun updatePrompt(prompt: FirestorePromptModel): Result<Unit> = withContext(Dispatchers.IO) {
        if (prompt.id.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Cannot update prompt without a valid document ID.")
            )
        }

        val firestore = getFirestore()
            ?: return@withContext Result.failure(
                IllegalStateException("Firebase is not initialized.")
            )

        try {
            val docRef = firestore.collection(COLLECTION_PROMPTS).document(prompt.id)
            docRef.update(prompt.toMap()).awaitTask()
            Log.d(TAG, "Successfully updated prompt ${prompt.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating prompt ${prompt.id}: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deletePrompt(documentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (documentId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Document ID cannot be blank."))
        }

        val firestore = getFirestore()
            ?: return@withContext Result.failure(
                IllegalStateException("Firebase is not initialized.")
            )

        try {
            firestore.collection(COLLECTION_PROMPTS).document(documentId).delete().awaitTask()
            Log.d(TAG, "Successfully deleted prompt $documentId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting prompt $documentId: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteAllPrompts(): Result<Unit> = withContext(Dispatchers.IO) {
        val firestore = getFirestore()
            ?: return@withContext Result.failure(
                IllegalStateException("Firebase is not initialized.")
            )

        try {
            val snapshot = firestore.collection(COLLECTION_PROMPTS).get().awaitTask()
            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().awaitTask()
            Log.d(TAG, "Successfully deleted all prompts from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all prompts: ${e.message}", e)
            Result.failure(e)
        }
    }
}
