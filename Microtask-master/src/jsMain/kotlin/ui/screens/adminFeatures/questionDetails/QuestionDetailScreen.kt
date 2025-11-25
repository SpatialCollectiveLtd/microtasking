package ui.screens.adminFeatures.questionDetails

import androidx.compose.runtime.*
import app.softwork.routingcompose.Parameters
import app.softwork.routingcompose.Router
import kotlinx.coroutines.launch
import network.API_URL
import network.model.*
import network.model.ToastType.ERROR
import network.model.ToastType.SUCCESS
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import org.w3c.files.File
import repository.*
import ui.string.*
import util.*


private val phoneNumberHeaders = listOf("No", "Unique Id", "Phone number", "Start Date", "Progress", "Action")
private val linksHeaders = listOf("No", "Upload Date", "Total Links", "Action")


@Composable
fun questionDetails(
    userRepository: UserRepository,
    questionRepository: QuestionRepository,
    taskRepository: TaskRepository,
    imageRepository: ImageRepository,
    parameters: Parameters?,
    router: Router
) {
    val coroutineScope = rememberCoroutineScope()
    val questionModel by remember { mutableStateOf(questionRepository.getQuestionFromStorage()) }
    var isPause by remember { mutableStateOf(questionModel.isPaused) }
    var workerResource by remember { mutableStateOf<Resource>(Resource.Loading) }
    var linkResource by remember { mutableStateOf<Resource>(Resource.Loading) }
    var workerToDelete by remember { mutableStateOf<TaskModel?>(null) }
    var linkToDelete by remember { mutableStateOf<ImageCountModel?>(null) }
    var phoneNumberFile by remember { mutableStateOf<File?>(null) }
    var imagesFile by remember { mutableStateOf<File?>(null) }
    var toastModel by remember { mutableStateOf<ToastModel?>(null) }
    var fileUploadResource by remember { mutableStateOf<Resource>(Resource.None) }

    LaunchedEffect(parameters) {
        workerResource = taskRepository.getWorkerByQuestionId(questionModel.id)
        linkResource = imageRepository.getLinksByQuestionId(questionModel.id)
    }

    headerComponent(userRepository, router)

    Div({ classes("container-fluid") }) {
        titleView(questionDetails)
        Div({ classes("row", "mt-4") }) {
            Div({ classes("col-md-12", "col-lg-9", "col-sm-12", "col-xl-9", "mt-1") }) {
                H4(attrs = { classes("p", "d-flex", "ml-5") }) {
                    Text(questionModel.name)
                }
            }
            Div({ classes("col-md-12", "col-lg-3", "col-sm-12", "col-xl-3", "mt-1") }) {
                dropDownMenu(questionModel, isPause) {
                    coroutineScope.launch {
                        questionRepository.updateQuestionState(questionModel, it)
                            .whenSucceed<QuestionModel> { resource ->
                                isPause = resource.data.isPaused
                            }
                    }
                }
            }
        }

        Div({ classes("row") }) {
            Div({ classes("col-md-12", "col-lg-6", "col-sm-12", "col-xl-6", "mt-3") }) {
                fileUploadFormView("uploadMoreWorkersForm",
                    FileType.PHONE_NUMBERS,
                    "Upload more workers",
                    fileUploadResource,
                    onChange = { phoneNumberFile = it },
                    onSubmit = {
                        phoneNumberFile.isNotNull(firstAction = { file ->
                            coroutineScope.launch {
                                fileUploadResource = Resource.Loading
                                fileUploadResource = taskRepository.uploadFile(questionModel.id, file)
                                toastModel = fileUploadResource.toToastModel()
                                fileUploadResource.whenSucceed<Resource> {
                                    workerResource = taskRepository.getWorkerByQuestionId(questionModel.id)
                                }
                            }
                        })
                    })
                phoneNumberTable(workerResource, questionModel.totalImage, workerToDelete) { taskModel, workers ->
                    workerToDelete = taskModel
                }
            }

            Div({ classes("col-md-12", "col-lg-6", "col-sm-12", "col-xl-6", "mt-3") }) {
                fileUploadFormView("uploadMoreLinksForm",
                    FileType.LINKS,
                    "Upload more links",
                    fileUploadResource,
                    onChange = { imagesFile = it },
                    onSubmit = {
                        imagesFile.isNotNull(firstAction = { file ->
                            coroutineScope.launch {
                                fileUploadResource = Resource.Loading
                                fileUploadResource = questionRepository.uploadFile(questionModel.id, file)
                                toastModel = fileUploadResource.toToastModel()
                                fileUploadResource.whenSucceed<Resource> {
                                    linkResource = imageRepository.getLinksByQuestionId(questionModel.id)
                                }
                            }
                        })
                    })
                linksTable(linkResource, linkToDelete) {
                    linkToDelete = it
                }
            }
        }
    }

    deleteDialog("deleteWorkerModal", "Delete Worker", "Are you sure you want to delete the worker?", {
        coroutineScope.launch {
            when (val resource = taskRepository.deleteWorker(workerToDelete!!)) {
                is Resource.Error<*> -> toastModel = ToastModel("Error! ", resource.getErrorMessage(), ERROR)
                is Resource.Success<*> -> {
                    workerResource = Resource.Success(workerResource.getData<MutableList<TaskModel>>().apply {
                        remove(workerToDelete)
                    })
                    val message = resource.data as String
                    toastModel = ToastModel("Succeed! ", message, SUCCESS)
                }
                Resource.Loading -> {
                }
                Resource.None -> {}
            }
            workerToDelete = null
        }
    }, {
        workerToDelete = null
    })


    deleteDialog("deleteLinksModal",
        "Delete Links",
        "Are you sure you want to delete all the links uploaded at this date ${linkToDelete?.createdAt}?",
        {
            coroutineScope.launch {
                when (val resource = imageRepository.deleteLinks(questionModel.id, linkToDelete!!.createdAt)) {
                    is Resource.Error<*> -> {
                        toastModel = ToastModel("Error! ", resource.getErrorMessage(), ERROR)
                    }
                    is Resource.Success<*> -> {
                        linkResource = Resource.Success(linkResource.getData<MutableList<ImageCountModel>>().apply {
                            remove(linkToDelete)
                        })
                        val message = resource.data as String
                        toastModel = ToastModel("Succeed! ", message, SUCCESS)
                    }
                    Resource.Loading -> {

                    }
                    Resource.None -> {}
                }
                linkToDelete = null
            }
        },
        {
            linkToDelete = null
        })

    toastModel.isNotNull({
        toastView(it.title, it.description, it.type) {
            toastModel = null
        }
    })
}

@Composable
fun dropDownMenu(
    questionModel: QuestionModel,
    isPause: Boolean,
    onStateChange: (Boolean) -> Unit
) {
    Div({ classes("dropdown", "d-md-flex", "justify-content-md-end") }) {
        Button({
            classes("btn", "btn-dark", "dropdown-toggle")
            id("menuButton")
            attr("data-bs-toggle", "dropdown")
            attr("aria-expanded", "false")
        }) {
            I(attrs = { classes("bi", "bi-gear", "me-3") })
            Text("Menu")
        }

        Ul({
            classes("dropdown-menu")
            attr("aria-labelledby", "menuButton")
        }) {
            Li {
                A(href = "${API_URL}/answers/${questionModel.id}", {
                    classes("dropdown-item")
                    target(ATarget.Blank)
                }) {
                    I(attrs = { classes("bi", "bi-file-earmark-arrow-down", "me-2") })
                    Text("Download Report")
                }
            }
            Li {
                Button({
                    classes("dropdown-item")
                    onClick { onStateChange(!isPause) }
                }) {
                    val iconClass = if (isPause) "bi-play" else "bi-pause"
                    val text = if (isPause) resume else pause
                    I(attrs = { classes("bi", iconClass, "me-2") })
                    Text(text)
                }
            }
        }
    }
}


@Composable
fun phoneNumberTable(
    resource: Resource,
    totalLink: Long,
    workerToDelete: TaskModel?,
    onDelete: (TaskModel, List<TaskModel>) -> Unit
) {
    responsiveTable(phoneNumberHeaders) {
        when (resource) {
            Resource.Loading -> phoneNumberHeaders.addColSpan { loadingView(loadingWorker) }
            is Resource.Error<*> -> phoneNumberHeaders.addColSpan { errorComponent(resource.getErrorMessage()) }
            is Resource.Success<*> -> {
                val workers = resource.getData<List<TaskModel>>()
                if (workers.isEmpty()) {
                    phoneNumberHeaders.addColSpan { normaTextView(noData) }
                } else {
                    workers.forEachIndexed { index, taskModel ->
                        Tr {
                            Td { Text(index.plus(1).toString()) }
                            Td { Text(taskModel.workerUniqueId) }
                            Td { Text(taskModel.phoneNumber) }
                            Td { Text(taskModel.startDate) }
                            Td { Text("${taskModel.progress}/${totalLink}") }
                            Td {
                                Button(attrs = {
                                    attr("data-bs-toggle", "modal")
                                    attr("data-bs-target", "#deleteWorkerModal")
                                    classes("btn", "btn-danger")
                                    onClick { onDelete(taskModel, workers) }
                                    if (workerToDelete?.id == taskModel.id) {
                                        disabled()
                                    }
                                }) { I(attrs = { classes("bi", "bi-trash") }) }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}


@Composable
fun linksTable(resource: Resource, linkToDelete: ImageCountModel?, onDelete: (ImageCountModel) -> Unit) {
    responsiveTable(linksHeaders) {
        when (resource) {
            Resource.Loading -> linksHeaders.addColSpan { loadingView(loadingLinks) }
            is Resource.Error<*> -> linksHeaders.addColSpan { errorComponent(resource.getErrorMessage()) }
            is Resource.Success<*> -> {
                val links = resource.getData<List<ImageCountModel>>()
                if (links.isEmpty()) {
                    Tr { Td({ colspan(linksHeaders.size) }) { normaTextView(noData) } }
                } else {
                    links.forEach { imageCountModel ->
                        Tr {
                            Th { Text(imageCountModel.id.plus(1).toString()) }
                            Td { Text(imageCountModel.createdAt) }
                            Td { Text(imageCountModel.totalImage.toString()) }
                            Td {
                                Button(attrs = {
                                    attr("data-bs-toggle", "modal")
                                    attr("data-bs-target", "#deleteLinksModal")
                                    classes("btn", "btn-danger")
                                    onClick { onDelete(imageCountModel) }
                                    if (linkToDelete?.id == imageCountModel.id) {
                                        disabled()
                                    }
                                }) { I(attrs = { classes("bi", "bi-trash") }) }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun fileUploadFormView(
    formId: String,
    fileType: FileType,
    label: String,
    fileUploadResource: Resource,
    onChange: (File) -> Unit,
    onSubmit: (FileType) -> Unit,
) {
    Form(attrs = {
        id(formId)
        encType(FormEncType.MultipartFormData)
        classes("needs-validation")
        addEventListener("submit") {
            it.preventDefault()
        }
    }) {
        Div({ classes("row", "d-flex", "justify-content-center", "mt-4") }) {
            Div({ classes("col-9") }) {
                Label(forId = fileType.name) { Text(label) }
                FileInputView(fileType, onChange)
            }
            Div({ classes("col-3", "d-grid", "gap-2") }) {
                Button(attrs = {
                    classes("btn", "btn-dark", "btn-block", "rounded-pill", "btn-sm", "align-self-end")
                    style { height(35.px) }
                    type(ButtonType.Submit)
                    if (fileUploadResource.isLoading()) {
                        disabled()
                    }
                    onClick { onSubmit(fileType) }
                }) { Text("Submit") }
            }
        }
    }
}
