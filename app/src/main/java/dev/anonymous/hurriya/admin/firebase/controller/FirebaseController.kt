package dev.anonymous.hurriya.admin.firebase.controller

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dev.anonymous.hurriya.admin.firebase.enums.FirebaseCollection
import dev.anonymous.hurriya.admin.firebase.utils.FirebaseUtils
import dev.anonymous.hurriya.admin.domain.models.Album
import dev.anonymous.hurriya.admin.domain.models.AlbumImage
import dev.anonymous.hurriya.admin.domain.models.Book
import dev.anonymous.hurriya.admin.domain.models.News
import dev.anonymous.hurriya.admin.domain.models.Poster
import dev.anonymous.hurriya.admin.domain.models.PrisonerCard
import dev.anonymous.hurriya.admin.domain.models.Statistic
import dev.anonymous.hurriya.admin.domain.models.Video
import dev.anonymous.hurriya.admin.domain.models.WhatsappTweet
import dev.anonymous.hurriya.admin.domain.models.base.FirestoreModel
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.concurrent.Volatile

class FirebaseController private constructor() {
    private val firebaseUtils: FirebaseUtils = FirebaseUtils.instance!!
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fireStoreDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    val currentUser: FirebaseUser?
        // Authentic
        get() = firebaseAuth.currentUser

    fun login(email: String, password: String, activity: Activity, callback: FirebaseAuthCallback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task: Task<AuthResult?>? ->
                if (task!!.isSuccessful) {
                    val user = task.getResult()!!.user
                    if (user != null) {
                        callback.onSuccess(user.uid)
                    }
                } else {
                    val taskException = task.exception
                    if (taskException is FirebaseAuthException) {
                        val errorCode = taskException.errorCode
                        val errorMessage = firebaseUtils.getFirebaseErrorMessage(errorCode)
                        callback.onFailure(errorMessage)
                    } else {
                        Log.e("FC", "login: taskEx went wrong")
                    }
                }
            }
    }

    fun registerNewAdmin(email: String, password: String, callback: FirebaseAuthCallback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.getResult()!!.user
                    if (user != null) {
                        callback.onSuccess(user.uid)
                    }
                } else {
                    val taskException = task.exception
                    if (taskException is FirebaseAuthException) {
                        val errorCode = taskException.errorCode
                        val errorMessage = firebaseUtils.getFirebaseErrorMessage(errorCode)
                        callback.onFailure(errorMessage)
                    } else {
                        Log.e("FC", "login: taskEx went wrong")
                    }
                }
            }
    }

    /* ****************************************************************************************** */ // FireStore
    fun setAdmin(userUid: String, callback: FirebaseCallback) {
        val data: MutableMap<String, Any> = HashMap()
        data.put(USER_ID, userUid)

        fireStoreDatabase.collection(FirebaseCollection.Admins.name)
            .document(userUid)
            .set(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onSuccess()
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "addAdmin: " + e.message)
                e.message?.let {
                    callback.onFailure(it)
                }
            }
    }

    fun setUrgentNews(urgentNewsText: String, callback: FirebaseCallback) {
        val data: MutableMap<String, Any> = HashMap()
        data.put(URGENT_TEXT, urgentNewsText)

        fireStoreDatabase.collection(FirebaseCollection.Other.name)
            .document(URGENT_DOCUMENT_ID)
            .set(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onSuccess()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateUrgentNews: " + e.message)
                e.message?.let {
                    callback.onFailure(it)
                }
            }
    }

    fun setWhatsAppGroupUrl(newUrl: String, callback: FirebaseCallback) {
        val data: MutableMap<String, Any> = HashMap()
        data.put("whatsappGroup", newUrl)

        fireStoreDatabase.collection(FirebaseCollection.social_links.name)
            .document("config")
            .set(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onSuccess()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateWhatsAppGroupUrl: " + e.message)
                e.message?.let {
                    callback.onFailure(it)
                }
            }
    }

    /* ****************************************************************************************** */
    fun <T> getData(
        collectionPath: String,
        modelType: Class<T>,
        callback: GetDataCallback<T>
    ) where T : FirestoreModel {
        fireStoreDatabase.collection(collectionPath)
            .orderBy("lastUpdate", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task: Task<QuerySnapshot> ->
                if (task.isSuccessful) {
                    val dataList = ArrayList<T>()
                    for (documentSnapshot in task.getResult()) {
                        val toObject = documentSnapshot.toObject<T>(modelType)
                        toObject.id = documentSnapshot.id
                        dataList.add(toObject)
                    }
                    callback.onSuccess(dataList)
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "get" + modelType.getName() + ": " + e.message)
                e.message?.let {
                    callback.onFailure(it)
                }
            }
    }

    fun <T> setData(collectionPath: String, model: T, callback: FirebaseCallback) {
        fireStoreDatabase.collection(collectionPath)
            .add(model!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onSuccess()
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "set" + model.javaClass.getName() + ": " + e.message)
                e.message?.let {
                    callback.onFailure(it)
                }
            }
    }

    fun deleteData(collectionPath: String, id: String, callback: FirebaseCallback) {
        fireStoreDatabase.collection(collectionPath)
            .document(id)
            .delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onSuccess()
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "delete" + collectionPath + ": " + e.message)
                e.message?.let {
                    callback.onFailure(it)
                }
            }
    }

    /* ****************************************************************************************** */
    fun setStatistic(statistic: Statistic, callback: FirebaseCallback) {
        setData(FirebaseCollection.Statistics.name, statistic, callback)
    }

    fun getStatistics(callback: GetDataCallback<Statistic>) {
        getData(FirebaseCollection.Statistics.name, Statistic::class.java, callback)
    }

    fun deleteStatistic(statisticsUuid: String, callback: FirebaseCallback) {
        deleteData(FirebaseCollection.Statistics.name, statisticsUuid, callback)
    }

    fun uploadStatisticFile(
        uuid: String,
        fileUri: Uri,
        fileName: String,
        callback: UploadFileCallback
    ) {
        val reference = firebaseStorage
            .getReference(FirebaseCollection.Statistics.name)
            .child(uuid)
            .child(fileName)

        uploadFileUri(fileUri, reference, callback)
    }

    fun deleteStatisticFiles(uuid: String, callback: FirebaseCallback) {
        val reference = firebaseStorage
            .getReference(FirebaseCollection.Statistics.name)
            .child(uuid)

        deleteFileUsingReference(reference, callback)
    }

    /* ****************************************************************************************** */
    fun setBook(book: Book, callback: FirebaseCallback) {
        setData(FirebaseCollection.Books.name, book, callback)
    }

    fun getBooks(callback: GetDataCallback<Book>) {
        getData(FirebaseCollection.Books.name, Book::class.java, callback)
    }

    fun deleteBook(bookUuid: String, callback: FirebaseCallback) {
        deleteData(FirebaseCollection.Books.name, bookUuid, callback)
    }

    fun uploadBookFile(uuid: String, fileUri: Uri, fileName: String, callback: UploadFileCallback) {
        val reference = firebaseStorage
            .getReference(FirebaseCollection.Books.name)
            .child(uuid)
            .child(fileName)

        uploadFileUri(fileUri, reference, callback)
    }

    fun deleteBookFiles(uuid: String, callback: FirebaseCallback) {
        val reference = firebaseStorage
            .getReference(FirebaseCollection.Books.name)
            .child(uuid)

        deleteFileUsingReference(reference, callback)
    }

    /* ****************************************************************************************** */
    fun setNews(news: News, callback: FirebaseCallback) {
        setData(FirebaseCollection.News.name, news, callback)
    }

    fun getNews(callback: GetDataCallback<News>) {
        getData(FirebaseCollection.News.name, News::class.java, callback)
    }

    fun deleteNews(newsId: String, callback: FirebaseCallback) {
        deleteData(FirebaseCollection.News.name, newsId, callback)
    }

    fun uploadNewsImage(fileUri: Uri, fileName: String, callback: UploadFileCallback) {
        val reference = firebaseStorage
            .getReference(FirebaseCollection.News.name)
            .child(fileName)

        uploadFileUri(fileUri, reference, callback)
    }

    /* ****************************************************************************************** */
    fun setPrisonerCard(prisonerCard: PrisonerCard, callback: FirebaseCallback) {
        setData(
            FirebaseCollection.PrisonersCards.name,
            prisonerCard,
            callback
        )
    }

    fun getPrisonersCards(callback: GetDataCallback<PrisonerCard>) {
        getData(
            FirebaseCollection.PrisonersCards.name,
            PrisonerCard::class.java,
            callback
        )
    }

    fun deletePrisonerCard(prisonerCardId: String, callback: FirebaseCallback) {
        deleteData(FirebaseCollection.PrisonersCards.name, prisonerCardId, callback)
    }

    fun uploadPrisonerImage(fileUri: Uri, fileName: String, callback: UploadFileCallback) {
        val reference = firebaseStorage
            .getReference(FirebaseCollection.PrisonersCards.name)
            .child(fileName)

        uploadFileUri(fileUri, reference, callback)
    }

    /* ****************************************************************************************** */
    fun setPoster(poster: Poster, callback: FirebaseCallback) {
        setData(FirebaseCollection.Posters.name, poster, callback)
    }

    fun getPosters(callback: GetDataCallback<Poster>) {
        getData(FirebaseCollection.Posters.name, Poster::class.java, callback)
    }

    fun deletePoster(posterId: String, callback: FirebaseCallback) {
        deleteData(FirebaseCollection.Posters.name, posterId, callback)
    }

    fun uploadPosterImage(fileUri: Uri, fileName: String, callback: UploadFileCallback) {
        val reference = firebaseStorage
            .getReference(FirebaseCollection.Posters.name)
            .child(fileName)

        uploadFileUri(fileUri, reference, callback)
    }

    /* ****************************************************************************************** */
    fun setAlbum(album: Album, callback: FirebaseCallback) {
        setData(FirebaseCollection.Albums.name, album, callback)
    }

    fun getAlbums(callback: GetDataCallback<Album>) {
        getData(FirebaseCollection.Albums.name, Album::class.java, callback)
    }

    fun deleteAlbum(uuid: String, callback: FirebaseCallback) {
        deleteData(FirebaseCollection.Albums.name, uuid, callback)
    }

    fun deleteAlbumImages(uuid: String, callback: FirebaseCallback) {
        val collection = fireStoreDatabase
            .collection(FirebaseCollection.Albums.name)
            .document(uuid)
            .collection(FirebaseCollection.Images.name)

        deleteDocumentsCollection(collection, callback)
    }

    fun deleteDocumentsCollection(collection: CollectionReference, callback: FirebaseCallback) {
        collection.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (documentSnapshot in task.getResult()!!) {
                        documentSnapshot.reference.delete()
                    }
                    callback.onSuccess()
                }
            }
            .addOnFailureListener { e ->
                e.message?.let {
                    callback.onFailure(it)
                }
            }
    }

    fun uploadAlbumImage(
        uuid: String,
        fileUri: Uri,
        fileName: String,
        callback: UploadFileCallback
    ) {
        val reference = firebaseStorage
            .getReference(FirebaseCollection.Albums.name)
            .child(uuid)
            .child(fileName)

        uploadFileUri(fileUri, reference, callback)
    }

    fun deleteAlbumFiles(albumUuid: String, callback: FirebaseCallback) {
        val reference = firebaseStorage
            .getReference(FirebaseCollection.Albums.name)
            .child(albumUuid)
            .child(FirebaseCollection.Images.name)

        deleteFileUsingReference(reference, callback)
    }

    /* ****************************************************************************************** */
    fun setAlbumImage(albumImage: AlbumImage, albumUuid: String, callback: FirebaseCallback) {
        fireStoreDatabase
            .collection(FirebaseCollection.Albums.name)
            .document(albumUuid)
            .collection(FirebaseCollection.Images.name)
            .document(albumImage.id!!)
            .set(albumImage)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onSuccess()
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "setAlbumImage: " + e.message)
                e.message?.let {
                    callback.onFailure(it)
                }
            }
    }

    fun getAlbumImages(albumUuid: String, callback: GetDataCallback<AlbumImage>) {
        fireStoreDatabase
            .collection(FirebaseCollection.Albums.name)
            .document(albumUuid)
            .collection(FirebaseCollection.Images.name)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val albumImages = ArrayList<AlbumImage>()
                    for (documentSnapshot in task.getResult()) {
                        albumImages.add(documentSnapshot.toObject(AlbumImage::class.java))
                    }
                    callback.onSuccess(albumImages)
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "getAlbum: " + e.message)
                e.message?.let {
                    callback.onFailure(it)
                }
            }
    }

    fun deleteAlbumImage(albumImageUuid: String, albumUuid: String, callback: FirebaseCallback) {
        fireStoreDatabase
            .collection(FirebaseCollection.Albums.name)
            .document(albumUuid)
            .collection(FirebaseCollection.Images.name)
            .document(albumImageUuid)
            .delete()
            .addOnCompleteListener { task: Task<Void?>? ->
                if (task!!.isSuccessful) {
                    callback.onSuccess()
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "deleteAlbumImage: " + e.message)
                e.message?.let {
                    callback.onFailure(it)
                }
            }
    }

    fun uploadAlbumImages(uuid: String, fileUri: Uri, callback: UploadFileCallback) {
        val reference = firebaseStorage
            .getReference(FirebaseCollection.Albums.name)
            .child(uuid)
            .child(FirebaseCollection.Images.name)
            .child(UUID.randomUUID().toString() + ".jpg")

        uploadFileUri(fileUri, reference, callback)
    }

    /* ****************************************************************************************** */
    fun setVideo(video: Video, callback: FirebaseCallback) {
        setData(FirebaseCollection.Videos.name, video, callback)
    }

    fun getVideos(callback: GetDataCallback<Video>) {
        getData(FirebaseCollection.Videos.name, Video::class.java, callback)
    }

    fun deleteVideo(videoUuid: String, callback: FirebaseCallback) {
        deleteData(FirebaseCollection.Videos.name, videoUuid, callback)
    }

    fun uploadVideoFile(
        uuid: String,
        fileUri: Uri,
        fileName: String,
        callback: UploadFileCallback
    ) {
        val reference = firebaseStorage
            .getReference(FirebaseCollection.Videos.name)
            .child(uuid)
            .child(fileName)

        uploadFileUri(fileUri, reference, callback)
    }

    fun uploadVideoImageBitmap(uuid: String, bitmapImage: Bitmap, callback: UploadFileCallback) {
        val reference = firebaseStorage
            .getReference(FirebaseCollection.Videos.name)
            .child(uuid)
            .child("$uuid.jpg")

        uploadImageByte(bitmapImage, reference, callback)
    }

    fun deleteVideoFiles(uuid: String, callback: FirebaseCallback) {
        val reference = firebaseStorage
            .getReference(FirebaseCollection.Videos.name)
            .child(uuid)

        deleteFileUsingReference(reference, callback)
    }

    /* ****************************************************************************************** */ //                .collection(FireStoreCollection.WhatsappTweets.name())
    //                .orderBy("postDate")
    //                .limit(3)
    //                .get()
    fun setWhatsappTweets(whatsappTweet: WhatsappTweet, callback: FirebaseCallback) {
        setData(
            FirebaseCollection.whatsapp_tweets.name,
            whatsappTweet,
            callback
        )
    }

    fun getWhatsappTweets(callback: GetDataCallback<WhatsappTweet>) {
        getData(
            FirebaseCollection.whatsapp_tweets.name,
            WhatsappTweet::class.java,
            callback
        )
    }

    fun deleteWhatsappTweets(whatsappTweetId: String, callback: FirebaseCallback) {
        deleteData(FirebaseCollection.whatsapp_tweets.name, whatsappTweetId, callback)
    }

    /* ****************************************************************************************** */ // Storage
    private fun uploadFileUri(
        fileUri: Uri,
        reference: StorageReference,
        callback: UploadFileCallback
    ) {
        reference.putFile(fileUri)
            .addOnProgressListener { taskSnapshot ->
                val progress =
                    (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                Log.d(TAG, "Upload progress: $progress")
            }
            .continueWithTask { task ->
                if (!task.isSuccessful && task.exception != null) {
                    throw task.exception as Throwable
                }
                reference.getDownloadUrl()
            }
            .addOnSuccessListener { uri ->
                callback.onSuccess(uri.toString())
            }
            .addOnFailureListener { e ->
                e.message?.let {
                    callback.onFailure(it)
                }
                Log.e(TAG, "uploadFile: " + e.message)
            }
    }

    private fun uploadImageByte(
        bitmapImage: Bitmap,
        reference: StorageReference,
        callback: UploadFileCallback
    ) {
        val imageByte = getArrayByteFromBitmapImage(bitmapImage)

        reference.putBytes(imageByte)
            .addOnProgressListener { taskSnapshot ->
                val progress =
                    (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                Log.d(TAG, "Upload progress: $progress")
            }
            .continueWithTask { task ->
                if (!task.isSuccessful && task.exception != null) {
                    throw task.exception as Throwable
                }
                reference.getDownloadUrl()
            }
            .addOnSuccessListener { uri ->
                callback.onSuccess(uri.toString())
            }
            .addOnFailureListener { e ->
                e.message?.let {
                    callback.onFailure(it)
                }
                Log.e(TAG, "uploadFile: " + e.message)
            }
    }

    private fun getArrayByteFromBitmapImage(bitmapImage: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    fun deleteFileUsingReference(reference: StorageReference, callback: FirebaseCallback) {
        reference.listAll()
            .addOnSuccessListener { listResult ->
                for (item in listResult.items) {
                    item.delete()
                }
                callback.onSuccess()
            }
            .addOnFailureListener { e ->
                e.message?.let {
                    callback.onFailure(it)
                }
                Log.e(TAG, "deleteFileUsingReference: " + e.message)
            }
    }

    fun deleteFileUsingUrl(fileUrl: String, callback: FirebaseCallback) {
        firebaseStorage
            .getReferenceFromUrl(fileUrl)
            .delete()
            .addOnSuccessListener {
                callback.onSuccess()
            }
            .addOnFailureListener { e ->
                e.message?.let {
                    callback.onFailure(it)
                }
                Log.e(TAG, "deleteFileUsingUrl: " + e.message)
            }
    }

    /* ------------------- Interfaces ------------------- */
    interface FirebaseCallback {
        fun onSuccess()

        fun onFailure(errorMessage: String)
    }

    interface FirebaseAuthCallback {
        fun onSuccess(userUid: String)

        fun onFailure(errorMessage: String)
    }

    interface GetDataCallback<T> {
        fun onSuccess(data: ArrayList<T>)

        fun onFailure(errorMessage: String)
    }

    interface UploadFileCallback {
        fun onSuccess(fileUrl: String)

        fun onFailure(errorMessage: String)
    }

    companion object {
        private const val TAG = "FirebaseController"

        @get:Synchronized
        @Volatile
        var instance: FirebaseController? = null
            get() {
                if (field == null) {
                    field = FirebaseController()
                }
                return field!!
            }
            private set

        private const val URGENT_DOCUMENT_ID = "urgent-document-id"
        private const val WHATS_APP_GROUP_URL_DOCUMENT_ID = "whats-app-group-url-document-id"

        private const val USER_ID = "userId"
        private const val URGENT_TEXT = "urgentText"
        private const val WHATS_APP_GROUP_URL_TEXT = "whatsAppGroupUrlText"
    }
}