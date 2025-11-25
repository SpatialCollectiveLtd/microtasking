package ui.screens.userFeatures.task

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import kotlinx.coroutines.launch
import network.model.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import repository.Resource.Success
import repository.TaskRepository
import repository.UserRepository
import repository.whenError
import repository.whenSucceed
import ui.screens.userFeatures.task.model.Answer.NO
import ui.screens.userFeatures.task.model.Answer.YES
import ui.string.no
import ui.string.yes
import ui.theme.Colors
import util.errorComponent
import util.headerComponent

@Composable
fun taskScreen(userRepository: UserRepository, taskRepository: TaskRepository, router: Router) {
    val coroutineScope = rememberCoroutineScope()
    val activeTask = taskRepository.getActiveTask()
    var currentPage by remember { mutableStateOf(activeTask.progress) }
    var imagePagination by remember { mutableStateOf(ImagePaginationModel()) }
    var errorModel by remember { mutableStateOf<ErrorModel?>(null) }

    coroutineScope.launch {
        imagePagination = taskRepository.getUserTask(activeTask.questionId, currentPage)
    }

    headerComponent(userRepository, router)

    Div({ classes("container") }) {
        Div({ classes("row") }) {
            Div({ classes("col-md-12", "justify-content-center") }) {
                Div(attrs = {
                    classes("card", "text-center", "mt-2", "shadow-sm")
                    style {
                        backgroundColor(Colors.background)
                        border {
                            width = 1.px
                            color = Colors.border
                            style = LineStyle.Solid
                        }
                    }
                }) {

                    Div(attrs = { classes("card-body", "mw-100") }) {
                        H5(attrs = { classes("card-title") }) { Text(activeTask.questionName) }

                        if (imagePagination.finished()) {
                            Div({ classes("row", "justify-content-center") }) {
                                Div(attrs = { classes("col-5", "alert", "alert-success", "mt-4") }) {
                                    B { Text("Success!") }
                                    Text("You finished all the links for this question!")
                                }
                            }
                        } else {
                            Img(
                                attrs = {
                                    id("imagePreview")
                                    classes("img-fluid", "mt-2")
                                    attr("data-original", imagePagination.url)
                                },
                                src = imagePagination.url, alt = "$currentPage/${activeTask.totalLink}"
                            )

                            Div({ classes("row") }) {
                                Div({ classes("col-md-12", "col-lg-12", "col-sm-12", "col-xl-12") }) {
                                    Span(attrs = { classes("p", "d-flex", "justify-content-center", "mt-3") }) {
                                        Text("$currentPage/${activeTask.totalLink}")
                                    }
                                }
                            }

                            if (errorModel != null) {
                                errorComponent(errorModel!!.message)
                            }

                            Div({ classes("row", "justify-content-center", "mt-3") }) {

                                Div({ classes("col-6", "d-flex", "justify-content-end") }) {
                                    buttonProgressView("btn-success", yes) {
                                        coroutineScope.launch {
                                            val answer = imagePagination.constructAnswerModel(YES, activeTask)
                                            sendAnswer(answer, taskRepository, {
                                                errorModel = it
                                            }, { successData, imagePaginationModel ->
                                                errorModel = null
                                                currentPage = successData.data
                                                imagePagination = imagePaginationModel
                                            })
                                        }
                                    }
                                }
                                Div({ classes("col-6", "d-flex", "justify-content-start") }) {
                                    buttonProgressView("btn-danger", no) {
                                        coroutineScope.launch {
                                            val answerModel = imagePagination.constructAnswerModel(NO, activeTask)
                                            sendAnswer(answerModel, taskRepository, {
                                                errorModel = it
                                            }, { successData, imagePaginationModel ->
                                                errorModel = null
                                                currentPage = successData.data
                                                imagePagination = imagePaginationModel
                                            })
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }
    jsImagePreview()
}

@Composable
fun buttonProgressView(type: String, text: String, onClick: () -> Unit) {
    Button(attrs = {
        attr("type", "button")
        classes("btn", type, "btn-block", "me-3")
        style { width(100.px) }
        onClick { onClick() }
    }) {
        Text(text)
    }
}

private suspend fun sendAnswer(
    answer: AnswerModel,
    taskRepository: TaskRepository,
    onError: (ErrorModel) -> Unit,
    onSucceed: (Success<Int>, ImagePaginationModel) -> Unit
) {
    val resource = taskRepository.sendAnswer(answer)
    resource.whenError<ErrorModel> {
        onError(it.error)
    }
    resource.whenSucceed<Int> {
        val imagePaginationModel = taskRepository.getUserTask(answer.questionId, it.data)
        onSucceed(it, imagePaginationModel)
    }
}


fun jsImagePreview() {
    js(
        "$(document).ready(function(){" +
                "     var imagePreview=document.getElementById('imagePreview');" +
                "     imagePreview.addEventListener('click', function (event) {" +
                "         var viewer = new Viewer(imagePreview, {" +
                "             title: function (image) {return imagePreview.alt;}," +
                "             toolbar: {zoomIn: 4,oneToOne: 4,zoomOut: 4}," +
                "             navbar:false," +
                "             hidden: function () { viewer.destroy();}," +
                "         });" +
                "         viewer.show();" +
                "     });" +
                "});"
    )
}


