package ui.model

enum class Screens(val route: String) {
    SignIn("/sign-in"), MyTask("/my-task"), Task("/task"),
    Admin("/admin"), SignedIn("/signedIn"), Home("/home"),
    NewQuestion("/new-question"), QuestionDetails("/question-details")
}