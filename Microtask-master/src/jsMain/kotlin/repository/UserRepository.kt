package repository

import io.ktor.client.request.*
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import network.client
import network.model.UserModel
import org.w3c.dom.get
import ui.model.UserRole
import ui.model.UserRole.*
import ui.model.isAdmin
import ui.string.invalidTokenError
import util.getBody
import util.tryAndReturnResource

const val PHONE_NUMBER_KEY = "phoneNumberKey"
const val EMAIL_KEY = "emailKey"
const val USER_TYPE_KEY = "userTypeKey"

class UserRepository {
    private val storage = localStorage

    val isUserSignedIn = MutableStateFlow(!getPhoneNumber().isNullOrEmpty() || !getEmail().isNullOrEmpty())

    fun savePhoneNumber(phoneNumber: String) {
        storage.setItem(PHONE_NUMBER_KEY, phoneNumber)
        storage.setItem(USER_TYPE_KEY, Worker.name)
        isUserSignedIn.value = true
    }

    fun deletePhoneNumber() {
        storage.removeItem(PHONE_NUMBER_KEY)
        isUserSignedIn.value = false
    }

    fun getPhoneNumber(): String? {
        return storage[PHONE_NUMBER_KEY]
    }

    fun signIn(token: String?) = flow {
        val resource = if (token.isNullOrEmpty()) {
            Resource.Error(invalidTokenError)
        } else {
            tryAndReturnResource {
                val userModel = client.post("/user/sign-in") { parameter("token", token) }.getBody<UserModel>()
                Resource.Success(data = userModel)
            }
        }
        emit(resource)
    }

    fun saveUser(userModel: UserModel) {
        storage.setItem(EMAIL_KEY, userModel.email)
        storage.setItem(USER_TYPE_KEY, Admin.name)
        isUserSignedIn.value = true
    }

    fun deleteEmail() {
        storage.removeItem(EMAIL_KEY)
        isUserSignedIn.value = false
    }


    fun getUserType(): UserRole? {
        return storage[USER_TYPE_KEY]?.let { valueOf(it) }
    }

    fun getEmail(): String? {
        return storage[EMAIL_KEY]
    }

    fun getSignedUserId(): String {
        return if (getUserType().isAdmin()) {
            getEmail()
        } else {
            getPhoneNumber()
        }.orEmpty()
    }

    fun signOut() {
        if (getUserType().isAdmin()) {
            deleteEmail()
            js("google.accounts.id.disableAutoSelect();")
        } else {
            deletePhoneNumber()
        }
    }
}