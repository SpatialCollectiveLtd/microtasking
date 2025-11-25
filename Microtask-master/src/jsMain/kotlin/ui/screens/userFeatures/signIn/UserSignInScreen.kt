package ui.screens.userFeatures.signIn

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import kotlinx.coroutines.launch
import network.model.TaskModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.attributes.required
import org.jetbrains.compose.web.dom.*
import repository.*
import repository.Resource.None
import ui.string.appTitle
import util.ButtonWithLoadingState
import util.errorComponent

private const val invalidPhoneNumber = "Invalid phone number"

@Composable
fun userSignInScreen(
    userRepository: UserRepository,
    taskRepository: TaskRepository,
    router: Router
) {
    val coroutineScope = rememberCoroutineScope()
    var phoneNumber by remember { mutableStateOf("") }
    var loadingResource by remember { mutableStateOf<Resource>(None) }
    if (userRepository.isUserSignedIn.value) {
        navigateToMyTask()
    }

    Div({ classes("container") }) {
        Div({ classes("row") }) {
            Div({ classes("col-md-12", "col-lg-12", "col-sm-12", "col-xl-12", "mt-5") }) {
                Img("icons/logo.png") {
                    classes("rounded", "mx-auto", "d-block")
                }
            }
        }

        Div({ classes("row") }) {
            Div({ classes("col-md-12", "col-lg-12", "col-sm-12", "col-xl-12") }) {
                Span(attrs = { classes("h2", "d-flex", "justify-content-center", "mt-3") }) {
                    Text(appTitle)
                }
            }
        }

        Form(attrs = {
            classes("needs-validation")
            addEventListener("submit") {
                it.preventDefault()
            }
        }) {

            Div({ classes("row", "d-flex", "justify-content-center", "mt-5") }) {
                loadingResource.whenError { errorComponent(it.getErrorMessage()) }
                Div({ classes("col-md-8", "col-lg-5", "col-sm-7", "col-xl-5") }) {
                    Input(type = InputType.Text,
                        attrs = {
                            classes("form-control")
                            placeholder("Phone Number *")
                            onInput { phoneNumber = it.value }
                            value(phoneNumber)
                            required(true)
                        }
                    )
                }
            }

            Div({ classes("row", "d-flex", "justify-content-center", "mt-3") }) {
                Div({ classes("col-md-8", "col-lg-5", "col-sm-7", "col-xl-5") }) {
                    ButtonWithLoadingState("Sign in", loadingResource, "col-12") {
                        coroutineScope.launch {
                            if (phoneNumber.isNotEmpty()) {
                                loadingResource = Resource.Loading
                                loadingResource = taskRepository.getTasksByPhoneNumber(phoneNumber)
                            }
                        }
                    }
                }
            }
        }
    }

    when (loadingResource) {
        is Resource.Success<*> -> {
            val phoneNumbers = loadingResource.getData<List<TaskModel>>()
            if (phoneNumbers.isNotEmpty()) {
                userRepository.savePhoneNumber(phoneNumber)
                navigateToMyTask()
            } else {
                loadingResource = Resource.Error(invalidPhoneNumber)
            }
        }
        else -> {}
    }
}

@Composable
fun navigateToMyTask() {
    js("window.location.replace(baseUrl+'/#/my-task');")
}