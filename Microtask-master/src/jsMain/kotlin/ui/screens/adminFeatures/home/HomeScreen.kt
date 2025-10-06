package ui.screens.adminFeatures.home

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import kotlinx.coroutines.launch
import network.model.ErrorModel
import network.model.QuestionModel
import network.model.ToastModel
import network.model.ToastType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import repository.*
import ui.model.Screens
import ui.string.loadingQuestions
import ui.string.noData
import util.*

private val headers = listOf("No", "Question", "Created Date", "Total Users", "Total Links", "Action")

@Composable
fun homeScreen(userRepository: UserRepository, questionRepository: QuestionRepository, router: Router) {
    val coroutineScope = rememberCoroutineScope()
    var questionToDelete by remember { mutableStateOf<QuestionModel?>(null) }
    val loadingResource by questionRepository.resource.collectAsState()
    var toastModel by remember { mutableStateOf<ToastModel?>(null) }
    coroutineScope.launch { questionRepository.getQuestions() }

    headerComponent(userRepository, router)

    Div({ classes("container-fluid") }) {
        Span(attrs = { classes("h2", "d-flex", "justify-content-center", "mt-5") }) {
            Text("Questions")
        }

        Div({ classes("row") }) {
            Div({ classes("col-12", "pull-right") }) {
                Button(attrs = {
                    classes("btn", "btn-dark", "btn-block", "rounded-pill", "pull-right")
                    style { height(50.px) }
                    onClick { router.navigate(Screens.Admin.route + Screens.NewQuestion.route) }
                }) { Text("Create new Question") }
            }
        }

        Div(attrs = { classes("card", "mt-3", "table-responsive") }) {
            Div(attrs = { classes("card-body") }) {
                questionsTable(loadingResource, questionToDelete, { question ->
                    questionRepository.saveQuestion(question)
                    router.navigate(Screens.Admin.route + Screens.QuestionDetails.route)
                }) {
                    questionToDelete = it
                }
            }
        }
    }

    deleteDialog(
        "deleteQuestionModal",
        "Delete Question",
        "Are you sure you want to delete this question? Notes: All the answers, links, and workers associated with this question will be deleted too?",
        {
            coroutineScope.launch {
                when (val resource = questionRepository.deleteQuestion(questionToDelete!!)) {
                    is Resource.Error<*> -> {
                        val errorMessage = when (val error = resource.error) {
                            is ErrorModel -> error.message
                            else -> error as String
                        }
                        toastModel = ToastModel("Error! ", errorMessage, ToastType.ERROR)
                    }
                    is Resource.Success<*> -> {
                        val message = resource.data as String
                        toastModel = ToastModel("Succeed! ", message, ToastType.SUCCESS)
                    }
                    Resource.Loading -> {
                        console.log("loading")
                    }
                    Resource.None -> {}
                }
                questionToDelete = null
            }
        },
        {
            questionToDelete = null
        })

    toastModel.isNotNull({
        toastView(it.title, it.description, it.type) {
            toastModel = null
        }
    })
}

@Composable
fun questionsTable(
    loadingResource: Resource,
    questionToDelete: QuestionModel?,
    onClick: (QuestionModel) -> Unit,
    onDelete: (QuestionModel) -> Unit
) {
    responsiveTable(headers) {
        when (loadingResource) {
            Resource.Loading -> headers.addColSpan { loadingView(loadingQuestions) }
            is Resource.Error<*> -> headers.addColSpan { errorComponent(loadingResource.getErrorMessage()) }
            is Resource.Success<*> -> {
                val questions = loadingResource.getData<List<QuestionModel>>()
                if (questions.isEmpty()) {
                    headers.addColSpan { normaTextView(noData) }
                } else {
                    questions.forEach { question ->
                        Tr(attrs = {
                            attr("role", "button")
                            if (question.isPaused) {
                                classes("text-info")
                            }
                        }) {
                            Td(attrs = { onClick { onClick(question) } }) { Text(question.id.toString()) }
                            Td(attrs = { onClick { onClick(question) } }) { Text(question.name) }
                            Td(attrs = { onClick { onClick(question) } }) { Text(question.createdAt) }
                            Td(attrs = { onClick { onClick(question) } }) { Text(question.totalWorker.toString()) }
                            Td(attrs = { onClick { onClick(question) } }) { Text(question.totalImage.toString()) }
                            Td {
                                Button(attrs = {
                                    attr("data-bs-toggle", "modal")
                                    attr("data-bs-target", "#deleteQuestionModal")
                                    classes("btn", "btn-danger")
                                    onClick { onDelete(question) }
                                    if (questionToDelete?.id == question.id) {
                                        disabled()
                                    }
                                }) { I(attrs = { classes("bi", "bi-trash") }) }
                            }
                        }
                    }
                }
            }
            Resource.None -> {}
        }
    }
}