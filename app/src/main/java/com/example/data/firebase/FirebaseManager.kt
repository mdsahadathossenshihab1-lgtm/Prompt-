package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.preferences.UserPreferencesManager
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseManager(private val context: Context) {

    private val prefs = UserPreferencesManager(context)

    companion object {
        private const val TAG = "FirebaseManager"
        const val COLLECTION_PROMPTS = "prompts"

        // Inbuilt provisioned Firebase credentials from project
        const val INBUILT_PROJECT_ID = "project-d28c75fa-a3af-48dc-ac6"
        const val INBUILT_API_KEY = "AIzaSyCBMJSy_j8LbOsc3eOwVZ31zUzHBKwkZoY"
        const val INBUILT_APP_ID = "1:875494692128:android:d28c75fa33eaf848kptq"
        const val INBUILT_FIRESTORE_DB_ID = "ai-studio-promptflowai-33eaf848-a774-4d54-9ed1-e99f251be592"
        const val INBUILT_STORAGE_BUCKET = "project-d28c75fa-a3af-48dc-ac6.firebasestorage.app"
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

    init {
        ensureFirebaseInitialized()
    }

    fun isFirebaseInitialized(): Boolean {
        return ensureFirebaseInitialized()
    }

    private fun ensureFirebaseInitialized(): Boolean {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                return true
            }

            // Check if user provided custom settings
            val customProjectId = prefs.getFirebaseProjectId()
            val customApiKey = prefs.getFirebaseApiKey()
            val customAppId = prefs.getFirebaseAppId()

            val projectId = if (customProjectId.isNotBlank()) customProjectId else INBUILT_PROJECT_ID
            val apiKey = if (customApiKey.isNotBlank()) customApiKey else INBUILT_API_KEY
            val appId = if (customAppId.isNotBlank()) customAppId else INBUILT_APP_ID

            val options = FirebaseOptions.Builder()
                .setProjectId(projectId)
                .setApiKey(apiKey)
                .setApplicationId(appId)
                .setStorageBucket(INBUILT_STORAGE_BUCKET)
                .build()

            FirebaseApp.initializeApp(context, options)
            Log.d(TAG, "Successfully initialized Firebase with project $projectId")
            return true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize Firebase: ${e.message}")
            return false
        }
    }

    fun getFirestore(): FirebaseFirestore? {
        return try {
            if (!ensureFirebaseInitialized()) {
                return null
            }

            val app = FirebaseApp.getInstance()
            try {
                // Try specific database ID first
                FirebaseFirestore.getInstance(app, INBUILT_FIRESTORE_DB_ID)
            } catch (e: Exception) {
                // Fallback to default firestore database
                FirebaseFirestore.getInstance(app)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring Firestore instance: ${e.message}", e)
            null
        }
    }

    suspend fun savePromptToCloud(model: FirestorePromptModel): Result<String> = withContext(Dispatchers.IO) {
        val firestore = getFirestore()
            ?: return@withContext Result.failure(
                IllegalStateException("Firebase is not initialized.")
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
                IllegalStateException("Firebase is initializing. Please check internet connection.")
            )

        try {
            val snapshot = firestore.collection(COLLECTION_PROMPTS).limit(1).get().awaitTask()
            Result.success("Connected to Firebase Firestore! Inbuilt Project ID: $INBUILT_PROJECT_ID")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
