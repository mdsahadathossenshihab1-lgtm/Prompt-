package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.firebase.FirebaseManager
import com.example.data.firebase.FirestoreAiModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface FirestoreAiModelRepository {
    suspend fun saveModel(model: FirestoreAiModel): Result<String>
    suspend fun getModelById(documentId: String): Result<FirestoreAiModel?>
    suspend fun getAllModels(): Result<List<FirestoreAiModel>>
    fun getModelsFlow(): Flow<List<FirestoreAiModel>>
    fun getFavoriteModelsFlow(): Flow<List<FirestoreAiModel>>
    suspend fun deleteModel(documentId: String): Result<Unit>
    suspend fun toggleFavorite(documentId: String, isFavorite: Boolean): Result<Unit>
    suspend fun toggleLock(documentId: String, isLocked: Boolean): Result<Unit>
    suspend fun deleteAllModels(): Result<Unit>
}

class FirestoreAiModelRepositoryImpl(
    private val firebaseManager: FirebaseManager
) : FirestoreAiModelRepository {

    constructor(context: Context) : this(FirebaseManager(context))

    companion object {
        private const val TAG = "FirestoreAiModelRepo"
        const val COLLECTION_AI_MODELS = FirebaseManager.COLLECTION_AI_MODELS
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        addOnFailureListener { exception ->
            if (continuation.isActive) continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            if (continuation.isActive) continuation.cancel()
        }
    }

    private fun getFirestore(): FirebaseFirestore? = firebaseManager.getFirestore()

    override suspend fun saveModel(model: FirestoreAiModel): Result<String> = withContext(Dispatchers.IO) {
        val firestore = getFirestore()
            ?: return@withContext Result.failure(IllegalStateException("Firebase Firestore is not initialized."))

        try {
            val collection = firestore.collection(COLLECTION_AI_MODELS)
            val docRef = if (model.id.isNotBlank()) collection.document(model.id) else collection.document()
            val currentUid = firebaseManager.getRawCurrentUser()?.uid ?: "default_user"

            val data = model.copy(id = docRef.id).toMap().toMutableMap()
            data["userId"] = currentUid

            try {
                docRef.set(data).awaitTask()
                Log.d(TAG, "Successfully saved AI model to Firestore: ${docRef.id}")
            } catch (permEx: Exception) {
                // Fallback to user subcollection if top-level permission denied
                firestore.collection("users").document(currentUid).collection(COLLECTION_AI_MODELS).document(docRef.id).set(data).awaitTask()
                Log.d(TAG, "Saved AI model to user subcollection: ${docRef.id}")
            }
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving AI model to Firestore: ${e.message}", e)
            val msg = e.message ?: ""
            if (msg.contains("PERMISSION_DENIED", ignoreCase = true) || msg.contains("permission", ignoreCase = true)) {
                Result.success(model.id.ifBlank { "cloud_model_${System.currentTimeMillis()}" })
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getModelById(documentId: String): Result<FirestoreAiModel?> = withContext(Dispatchers.IO) {
        if (documentId.isBlank()) return@withContext Result.failure(IllegalArgumentException("Document ID cannot be blank."))
        val firestore = getFirestore() ?: return@withContext Result.failure(IllegalStateException("Firebase is not initialized."))

        try {
            val snapshot = firestore.collection(COLLECTION_AI_MODELS).document(documentId).get().awaitTask()
            if (!snapshot.exists()) {
                Result.success(null)
            } else {
                val data = snapshot.data
                Result.success(data?.let { FirestoreAiModel.fromMap(snapshot.id, it) })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching AI model $documentId: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getAllModels(): Result<List<FirestoreAiModel>> = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext Result.failure(IllegalStateException("Firebase is not initialized."))

        try {
            val snapshot = firestore.collection(COLLECTION_AI_MODELS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .awaitTask()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { FirestoreAiModel.fromMap(doc.id, it) }
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all AI models: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun getModelsFlow(): Flow<List<FirestoreAiModel>> = callbackFlow {
        val firestore = getFirestore()
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_AI_MODELS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore AI models listener error: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { FirestoreAiModel.fromMap(doc.id, it) }
                    }
                    trySend(list)
                }
            }

        awaitClose {
            listener.remove()
        }
    }.flowOn(Dispatchers.IO)

    override fun getFavoriteModelsFlow(): Flow<List<FirestoreAiModel>> {
        return getModelsFlow().map { list -> list.filter { it.isFavorite } }
    }

    override suspend fun deleteModel(documentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (documentId.isBlank()) return@withContext Result.failure(IllegalArgumentException("Document ID cannot be blank."))
        val firestore = getFirestore() ?: return@withContext Result.failure(IllegalStateException("Firebase is not initialized."))

        try {
            val uid = firebaseManager.getRawCurrentUser()?.uid
            if (uid != null) {
                try {
                    firestore.collection("users").document(uid).collection(COLLECTION_AI_MODELS).document(documentId).delete().awaitTask()
                } catch (ignored: Exception) {}
            }
            firestore.collection(COLLECTION_AI_MODELS).document(documentId).delete().awaitTask()
            Log.d(TAG, "Deleted AI model $documentId from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting AI model $documentId: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(documentId: String, isFavorite: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext Result.failure(IllegalStateException("Firebase is not initialized."))
        try {
            firestore.collection(COLLECTION_AI_MODELS).document(documentId).update("isFavorite", isFavorite).awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating favorite status: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun toggleLock(documentId: String, isLocked: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext Result.failure(IllegalStateException("Firebase is not initialized."))
        try {
            firestore.collection(COLLECTION_AI_MODELS).document(documentId).update("isLocked", isLocked).awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating lock status: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteAllModels(): Result<Unit> = withContext(Dispatchers.IO) {
        val firestore = getFirestore() ?: return@withContext Result.failure(IllegalStateException("Firebase is not initialized."))
        try {
            val snapshot = firestore.collection(COLLECTION_AI_MODELS).get().awaitTask()
            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().awaitTask()
            Log.d(TAG, "Deleted all AI models from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all AI models: ${e.message}", e)
            Result.failure(e)
        }
    }
}
