package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.preferences.UserPreferencesManager
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
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

class EmailNotVerifiedException(val email: String) : Exception("Email not verified for $email")

class FirebaseManager(private val context: Context) {

    private val prefs = UserPreferencesManager(context)

    companion object {
        private const val TAG = "FirebaseManager"
        const val COLLECTION_PROMPTS = "prompts"

        // Inbuilt provisioned Firebase credentials from project
        const val INBUILT_PROJECT_ID = "project-d28c75fa-a3af-48dc-ac6"
        const val INBUILT_API_KEY = "AIzaSyAlTCVPbbaKThYdp9GFlOch_pKZYgqTW2M"
        const val INBUILT_APP_ID = "1:875494692128:android:932bbda953e75cc885b345"
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

    fun reinitializeWithCustomConfig(projectId: String, apiKey: String, appId: String): Boolean {
        prefs.setFirebaseProjectId(projectId)
        prefs.setFirebaseApiKey(apiKey)
        prefs.setFirebaseAppId(appId)

        try {
            val apps = FirebaseApp.getApps(context)
            for (app in apps) {
                app.delete()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error resetting Firebase app: ${e.message}")
        }

        return ensureFirebaseInitialized()
    }

    fun getActiveProjectId(): String {
        val custom = prefs.getFirebaseProjectId()
        return if (custom.isNotBlank()) custom else INBUILT_PROJECT_ID
    }

    fun getActiveApiKey(): String {
        val custom = prefs.getFirebaseApiKey()
        return if (custom.isNotBlank()) custom else INBUILT_API_KEY
    }

    fun getActiveAppId(): String {
        val custom = prefs.getFirebaseAppId()
        return if (custom.isNotBlank()) custom else INBUILT_APP_ID
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

    // ==========================================
    // FIREBASE AUTHENTICATION SUPPORT
    // ==========================================

    fun getAuth(): FirebaseAuth? {
        return try {
            if (!ensureFirebaseInitialized()) return null
            FirebaseAuth.getInstance(FirebaseApp.getInstance())
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring FirebaseAuth instance: ${e.message}", e)
            null
        }
    }

    val currentUserFlow: Flow<FirebaseUser?> = callbackFlow {
        val auth = getAuth()
        if (auth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        trySend(auth.currentUser)
        val listener = FirebaseAuth.AuthStateListener { currentAuth ->
            trySend(currentAuth.currentUser)
        }
        auth.addAuthStateListener(listener)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }.flowOn(Dispatchers.IO)

    fun getCurrentUser(): FirebaseUser? {
        val user = getAuth()?.currentUser ?: return null
        // If anonymous, allow access. If email account, require isEmailVerified.
        if (user.isAnonymous) return user
        return if (user.isEmailVerified) user else null
    }

    fun getRawCurrentUser(): FirebaseUser? {
        return getAuth()?.currentUser
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext Result.failure(IllegalStateException("Firebase Auth not initialized"))
        try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), pass).awaitTask()
            val user = authResult.user ?: throw IllegalStateException("User not found after sign in")
            user.reload().awaitTask()
            if (!user.isEmailVerified) {
                auth.signOut()
                throw EmailNotVerifiedException(user.email ?: email.trim())
            }
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, displayName: String = ""): Result<String> = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext Result.failure(IllegalStateException("Firebase Auth not initialized"))
        try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), pass).awaitTask()
            val user = authResult.user ?: throw IllegalStateException("User creation failed")
            if (displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdates).awaitTask()
            }
            // Send verification email to ensure real email verification
            user.sendEmailVerification().awaitTask()
            // Sign out immediately so user cannot enter before verifying email
            auth.signOut()
            Result.success(user.email ?: email.trim())
        } catch (e: Exception) {
            Log.e(TAG, "Sign up failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun resendVerificationEmail(email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext Result.failure(IllegalStateException("Firebase Auth not initialized"))
        try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), pass).awaitTask()
            val user = authResult.user ?: throw IllegalStateException("User not found")
            user.sendEmailVerification().awaitTask()
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Resend verification failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun checkVerificationAndSignIn(email: String, pass: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext Result.failure(IllegalStateException("Firebase Auth not initialized"))
        try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), pass).awaitTask()
            val user = authResult.user ?: throw IllegalStateException("User not found")
            user.reload().awaitTask()
            if (!user.isEmailVerified) {
                auth.signOut()
                throw EmailNotVerifiedException(user.email ?: email.trim())
            }
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Check verification failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext Result.failure(IllegalStateException("Firebase Auth not initialized"))
        try {
            val authResult = auth.signInAnonymously().awaitTask()
            val user = authResult.user ?: throw IllegalStateException("Anonymous sign in failed")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Anonymous sign in failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            getAuth()?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error: ${e.message}", e)
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
