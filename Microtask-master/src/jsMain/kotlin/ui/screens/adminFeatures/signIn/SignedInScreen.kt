package ui.screens.adminFeatures.signIn

import androidx.compose.runtime.*
import app.softwork.routingcompose.Parameters
import app.softwork.routingcompose.Router
import network.model.UserModel
import org.jetbrains.compose.web.dom.Div
import repository.Resource
import repository.UserRepository
import repository.getData
import repository.getErrorMessage
import ui.string.loadingPleaseWait
import util.errorDivComponent
import util.loadingView

private const val TOKEN_KEY = "token"

@Composable
fun signedInScreen(userRepository: UserRepository, parameters: Parameters?, router: Router) {
    val token = remember { parameters?.map?.get(TOKEN_KEY)?.firstOrNull() }
    var loadingResource: Resource by remember { mutableStateOf(Resource.Loading) }
    LaunchedEffect(true) {
        userRepository.signIn(token).collect { loadingResource = it }
    }
    Div({ classes("container") }) {
        when (loadingResource) {
            is Resource.Error<*> -> errorDivComponent(loadingResource.getErrorMessage())
            Resource.Loading -> loadingView(loadingPleaseWait)
            is Resource.Success<*> -> {
                userRepository.saveUser(loadingResource.getData() as UserModel)
                js("window.location.replace(baseUrl+'/#/admin/home');")
                Unit
            }
            Resource.None -> {}
        }
    }
}

