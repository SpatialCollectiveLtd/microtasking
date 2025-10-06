package ui.screens.userFeatures.userTask

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.softwork.routingcompose.Router
import network.model.TaskModel
import network.model.getProgressStatus
import network.model.isFinished
import org.jetbrains.compose.web.dom.*
import repository.*
import repository.Resource.*
import ui.model.Screens
import ui.string.loadingQuestions
import util.*

private val headers = listOf("No", "Question", "Start Date", "Total Links", "Progress", "Status")

@Composable
fun userTaskScreen(userRepository: UserRepository, taskRepository: TaskRepository, router: Router) {
    val phoneNumber = userRepository.getPhoneNumber().orEmpty()
    val loadingResource by taskRepository.getTasksByPhoneNumberAsFlow(phoneNumber).collectAsState(Loading)

    headerComponent(userRepository, router)

    Div({ classes("container-fluid") }) {
        titleView("My Tasks")

        Div(attrs = { classes("card", "mt-5", "table-responsive") }) {
            Div(attrs = { classes("card-body") }) {
                userTaskTable(loadingResource) {
                    taskRepository.setActiveTask(it)
                    router.navigate(Screens.Task.route)
                }
            }
        }
    }
}


@Composable
fun userTaskTable(
    loadingResource: Resource,
    onClick: (TaskModel) -> Unit
) {
    responsiveTable(headers) {
        when (loadingResource) {
            Loading -> headers.addColSpan { loadingView(loadingQuestions) }
            is Error<*> -> headers.addColSpan { errorComponent(loadingResource.getErrorMessage()) }
            is Success<*> -> {
                val tasks = loadingResource.getData<List<TaskModel>>()
                tasks.forEach { taskModel ->
                    Tr(attrs = {
                        if (!taskModel.isFinished() && !taskModel.isPaused) {
                            attr("role", "button")
                            onClick { onClick(taskModel) }
                        } else {
                            classes(if (taskModel.isPaused) "text-info" else "text-success")
                        }
                    }) {
                        Th(attrs = { attr("scope", "row") }) { Text(taskModel.id.toString()) }
                        Td { Text(taskModel.questionName) }
                        Td { Text(taskModel.startDate) }
                        Td { Text(taskModel.totalLink.toString()) }
                        Td { Text("${taskModel.progress}/${taskModel.totalLink}") }
                        Td { Text(taskModel.getProgressStatus()) }
                    }
                }
            }
            else -> {}
        }
    }
}
