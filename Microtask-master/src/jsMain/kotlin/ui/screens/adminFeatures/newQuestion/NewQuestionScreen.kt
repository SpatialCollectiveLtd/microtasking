package ui.screens.adminFeatures.newQuestion

import androidx.compose.runtime.*
import app.softwork.routingcompose.Parameters
import app.softwork.routingcompose.Router
import kotlinx.coroutines.launch
import network.model.FileType
import network.model.ToastModel
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import org.w3c.files.File
import repository.*
import ui.model.Screens
import util.*

@Composable
fun newQuestionScreen(
    userRepository: UserRepository, questionRepository: QuestionRepository, parameters: Parameters?, router: Router
) {
    val coroutineScope = rememberCoroutineScope()
    var questionName by remember { mutableStateOf("") }
    var imageFile by remember { mutableStateOf<File?>(null) }
    var phoneNumberFile by remember { mutableStateOf<File?>(null) }
    var uploadResource by remember { mutableStateOf<Resource>(Resource.None) }
    var toastModel by remember { mutableStateOf<ToastModel?>(null) }

    headerComponent(userRepository, router)

    Div({ classes("container-fluid") }) {
        titleView("New Question")
        Form(attrs = {
            id("newQuestionForm")
            encType(FormEncType.MultipartFormData)
            classes("needs-validation")
            addEventListener("submit") {
                it.preventDefault()
            }
        }) {

            Div({ classes("row", "d-flex", "justify-content-center", "mt-5") }) {
                Div({ classes("col-md-4", "col-lg-4", "col-sm-6", "col-xl-4") }) {
                    Input(type = InputType.Text, attrs = {
                        id("questionName")
                        classes("form-control")
                        onInput { questionName = it.value }
                        placeholder("Question Name *")
                        required(true)
                    })
                }
            }

            Div({ classes("row", "d-flex", "justify-content-center", "mt-4") }) {
                Div({ classes("col-md-4", "col-lg-4", "col-sm-6", "col-xl-4") }) {
                    Label(forId = FileType.LINKS.name) { Text("Image links") }
                    FileInputView(FileType.LINKS) { imageFile = it }
                }
            }

            Div({ classes("row", "d-flex", "justify-content-center", "mt-3") }) {
                Div({ classes("col-md-4", "col-lg-4", "col-sm-6", "col-xl-4") }) {
                    Label(forId = FileType.PHONE_NUMBERS.name) { Text("User Phone numbers") }
                    FileInputView(FileType.PHONE_NUMBERS) { phoneNumberFile = it }
                }
            }

            Div({ classes("row", "d-flex", "justify-content-center", "mt-5") }) {
                Div({ classes("col-md-3", "col-lg-3", "col-sm-6", "col-xl-3") }) {
                    Button(attrs = {
                        classes("btn", "btn-dark", "btn-block", "rounded-pill", "col-12")
                        style { height(50.px) }
                        type(ButtonType.Submit)
                        if (uploadResource.isLoading()) {
                            disabled()
                        }
                        onClick {
                            if (allFileAreValid(questionName, imageFile, phoneNumberFile)) {
                                uploadResource = Resource.Loading
                                coroutineScope.launch {
                                    uploadResource = questionRepository.createNewQuestion(
                                        questionName, imageFile!!, phoneNumberFile!!
                                    )
                                    toastModel = uploadResource.toToastModel()
                                    uploadResource.whenSucceed<Resource> {
                                        questionRepository.saveQuestion(it.getData())
                                        router.navigate(Screens.Admin.route + Screens.QuestionDetails.route, true)
                                    }
                                }
                            }
                        }
                    }) {
                        if (uploadResource.isLoading()) {
                            Span(attrs = {
                                attr("aria-hidden", "true")
                                attr("role", "status")
                                classes("spinner-border", "spinner-border-sm")
                            })
                            Span(attrs = {
                                attr("aria-hidden", "true")
                                attr("role", "status")
                                classes("r-only", "spinner-border-sm")
                            }) {
                                Text("  Submitting, Please wait...")
                            }

                        } else {
                            Text("Create question")
                        }
                    }
                }
            }
        }
    }

    toastModel.isNotNull(firstAction = {
        toastView(title, description, type) {
            toastModel = null
        }
    })
}

private fun allFileAreValid(questionName: String, imageFile: File?, phoneNumberFile: File?): Boolean {
    return questionName.isNotEmpty() && imageFile.isNotNull() && phoneNumberFile.isNotNull()
}