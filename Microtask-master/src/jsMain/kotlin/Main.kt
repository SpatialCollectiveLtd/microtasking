import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.softwork.routingcompose.BrowserRouter
import app.softwork.routingcompose.Router
import org.jetbrains.compose.web.renderComposable
import repository.ImageRepository
import repository.QuestionRepository
import repository.TaskRepository
import repository.UserRepository
import ui.model.Screens.*
import ui.model.UserRole
import ui.screens.adminFeatures.home.homeScreen
import ui.screens.adminFeatures.newQuestion.newQuestionScreen
import ui.screens.adminFeatures.questionDetails.questionDetails
import ui.screens.adminFeatures.signIn.adminSignIn
import ui.screens.adminFeatures.signIn.signedInScreen
import ui.screens.userFeatures.signIn.userSignInScreen
import ui.screens.userFeatures.task.taskScreen
import ui.screens.userFeatures.userTask.userTaskScreen

fun main() {
    val userRepository = UserRepository()
    val taskRepository = TaskRepository()
    val questionRepository = QuestionRepository()
    val imageRepository = ImageRepository()

    renderComposable(rootElementId = "root") {
        BrowserRouter("/") {
            val router = Router.current
            val isUserSignedIn by userRepository.isUserSignedIn.collectAsState()

            route(SignIn.route) {
                userSignInScreen(userRepository, taskRepository, router)
            }
            route(MyTask.route) {
                redirectToSignInIfNotSignIn(isUserSignedIn)
                userTaskScreen(userRepository, taskRepository, router)
            }
            route(Task.route) {
                redirectToSignInIfNotSignIn(isUserSignedIn)
                taskScreen(userRepository, taskRepository, router)
            }
            route(Admin.route) {
                route(SignIn.route) {
                    adminSignIn(userRepository, router)
                }
                route(SignedIn.route) {
                    signedInScreen(userRepository, parameters, router)
                }
                route(Home.route) {
                    redirectToSignInIfNotSignIn(isUserSignedIn, true)
                    homeScreen(userRepository, questionRepository, router)
                }
                route(NewQuestion.route) {
                    redirectToSignInIfNotSignIn(isUserSignedIn, true)
                    newQuestionScreen(userRepository, questionRepository, parameters, router)
                }
                route(QuestionDetails.route) {
                    redirectToSignInIfNotSignIn(isUserSignedIn, true)
                    questionDetails(
                        userRepository,
                        questionRepository,
                        taskRepository,
                        imageRepository,
                        parameters,
                        router
                    )
                }
                noMatch {
                    adminSignIn(userRepository, router)
                }
            }
            noMatch {
                when (userRepository.getUserType()) {
                    UserRole.Admin -> adminSignIn(userRepository, router)
                    else -> userSignInScreen(userRepository, taskRepository, router)
                }
            }
        }
    }
}

private fun redirectToSignInIfNotSignIn(isUserSignedIn: Boolean, isAdminRoute: Boolean = false) {
    if (!isUserSignedIn) {
        if (isAdminRoute) {
            js("window.location.replace(baseUrl+'/admin/sign-in');")
        } else {
            js("window.location.replace(baseUrl+'/sign-in');")
        }
    }
}



