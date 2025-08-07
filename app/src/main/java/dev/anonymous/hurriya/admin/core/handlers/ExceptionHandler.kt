package dev.anonymous.hurriya.admin.core.handlers

import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ExceptionHandler {

    fun handle(e: Throwable): String {
        return when (e) {
            is UnknownHostException -> "تعذر الاتصال بالإنترنت"
            is SocketTimeoutException -> "انتهت مهلة الاتصال بالخادم"
            is FirebaseNetworkException -> "فشل الاتصال بخوادم Firebase"
            is FirebaseAuthInvalidCredentialsException -> {
                when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "عنوان البريد الإلكتروني غير صالح"
                    "ERROR_WRONG_PASSWORD" -> "كلمة المرور غير صحيحة"
                    else -> "بيانات الدخول غير صحيحة"
                }
            }

            is FirebaseAuthInvalidUserException -> {
                when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "المستخدم غير موجود"
                    "ERROR_USER_DISABLED" -> "تم تعطيل هذا الحساب"
                    else -> "تعذر تسجيل الدخول لهذا المستخدم"
                }
            }

            is FirebaseAuthUserCollisionException -> {
                when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "البريد الإلكتروني مستخدم مسبقًا"
                    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "الحساب مرتبط بطريقة تسجيل دخول مختلفة"
                    else -> "البريد الإلكتروني مرتبط بحساب آخر"
                }
            }

            is FirebaseTooManyRequestsException -> "تم إجراء عدد كبير من المحاولات. حاول لاحقًا"
            is FirebaseAuthRecentLoginRequiredException -> "يجب إعادة تسجيل الدخول لتنفيذ هذا الإجراء"
            is FirebaseFirestoreException -> "حدث خطأ في قاعدة البيانات. الرجاء المحاولة لاحقًا"
            is FirebaseException -> "حدث خطأ في Firebase: ${e.message ?: "تفاصيل غير متوفرة"}"
            is ApiException -> "فشل تسجيل الدخول عبر Google: ${e.statusCode}"
            else -> e.message ?: "حدث خطأ غير متوقع"
        }
    }
}