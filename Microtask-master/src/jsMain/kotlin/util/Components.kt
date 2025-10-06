package util

import androidx.compose.runtime.Composable
import app.softwork.routingcompose.Router
import network.model.FileType
import network.model.ToastType
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.*
import org.w3c.files.File
import org.w3c.files.get
import repository.Resource
import repository.UserRepository
import repository.isLoading
import ui.model.Screens
import ui.model.isAdmin
import ui.string.appTitle

@Composable
fun headerComponent(userRepository: UserRepository, router: Router) {
    Nav(attrs = { classes("navbar", "navbar-light", "bg-light") }) {
        Div({ classes("container-fluid") }) {
            A(attrs = {
                classes("navbar-brand", "d-inline-flex")
                style { cursor("pointer") }
                onClick {
                    if (userRepository.getUserType().isAdmin()) {
                        router.navigate(Screens.Admin.route + Screens.Home.route)
                    } else {
                        router.navigate(Screens.MyTask.route)
                    }
                }
            }) {
                Img(attrs = {
                    attr("width", "70")
                    classes("align-text-center")
                }, src = "icons/logo.png", alt = appTitle)
                P(attrs = { classes("d-none", "d-sm-block", "m-auto") }) { Text(appTitle) }
            }
            Div({ classes("d-flex") }) {
                Button(attrs = {
                    classes("btn", "btn-outline-dark", "btn-sm", "px-4", "me-4")
                    onClick { userRepository.signOut() }
                }) { Text("Sign out") }
                Span(attrs = { classes("navbar-text", "d-none", "d-sm-block") }) {
                    Text(userRepository.getSignedUserId())
                }
            }
        }
    }
}

@Composable
fun tableHeader(headers: List<String>) {
    Thead {
        Tr {
            headers.forEach {
                Th(attrs = { attr("scope", "col") }) { Text(it) }
            }
        }
    }
}


@Composable
fun responsiveTable(headers: List<String>, body: @Composable () -> Unit) {
    Div(attrs = { classes("table-responsive") }) {
        Table(attrs = { classes("table", "table-striped", "table-hover") }) {
            tableHeader(headers)
            Tbody { body() }
        }
    }
}

@Composable
fun List<String>.addColSpan(views: @Composable () -> Unit) {
    Tr {
        Td({ colspan(this@addColSpan.size) }) {
            views()
        }
    }
}

@Composable
fun errorComponent(errorMessage: String) {
    Div({ classes("d-flex", "justify-content-center") }) {
        Span(attrs = {
            classes("col-md-8", "col-lg-5", "col-sm-6", "col-xl-5", "alert", "alert-danger")
        }) {
            Text(errorMessage)
        }
    }
}


@Composable
fun errorDivComponent(errorMessage: String) {
    Div({ classes("row", "justify-content-center") }) {
        Div(attrs = { classes("col-5", "alert", "alert-danger", "mt-4") }) {
            B { Text("Error! ") }
            P { Text(errorMessage) }
        }
    }
}

@Composable
fun successAlertView(successMessage: String) {
    Div({ classes("row", "justify-content-center") }) {
        Div(attrs = { classes("col-5", "alert", "alert-success", "alert-dismissible", "fade", "show", "mt-4") }) {
            B { Text("Success! ") }
            Text(successMessage)
            Button(attrs = {
                type(ButtonType.Button)
                classes("btn-close")
                attr("data-bs-dismiss", "alert")
            })
        }
    }
}

@Composable
fun titleView(title: String) {
    Span(attrs = { classes("h2", "d-flex", "justify-content-center", "mt-5") }) {
        Text(title)
    }
}

@Composable
fun normaTextView(title: String) {
    Span(attrs = { classes("d-flex", "justify-content-center") }) {
        Text(title)
    }
}

@Composable
fun toastView(
    title: String,
    description: String,
    type: ToastType,
    onClose: () -> Unit
) {
    Div(attrs = {
        classes("position-fixed", "bottom-0", "end-0", "p-3")
        style {
            property("z-index", 11)
        }
    }) {
        Div(attrs = {
            classes("toast", type.type, "text-white", "show", "fade")
            attr("role", "alert")
            attr("aria-live", "assertive")
            attr("aria-atomic", "true")
        }) {
            Div(attrs = {
                classes("toast-header", type.type, "text-white")
            }) {
                B(attrs = { classes("me-auto") }) { Text(title) }
                Button(attrs = {
                    type(ButtonType.Button)
                    classes("btn-close", "btn-close-white")
                    attr("data-bs-dismiss", "toast")
                    attr("aria-label", "Close")
                    onClick { onClose() }
                })
            }
            Div(attrs = {
                classes("toast-body")
            }) {
                Text(description)
            }
        }
    }
}

@Composable
fun deleteDialog(
    id: String,
    title: String,
    description: String,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    Div(attrs = {
        id(id)
        classes("modal")
        attr("data-bs-backdrop", "static")
        attr("data-bs-keyboard", "false")
        tabIndex(-1)
    }) {
        Div(attrs = { classes("modal-dialog", "modal-dialog-centered") }) {
            Div(attrs = { classes("modal-content") }) {
                Div(attrs = { classes("modal-header") }) {
                    H5 { Text(title) }
                    Button(attrs = {
                        type(ButtonType.Button)
                        classes("btn-close")
                        attr("data-bs-dismiss", "modal")
                        attr("aria-label", "Close")
                        onClick { onCancel() }
                    })
                }
                Div(attrs = { classes("modal-body") }) {
                    P { Text(description) }
                }
                Div(attrs = { classes("modal-footer") }) {
                    Button(attrs = {
                        type(ButtonType.Button)
                        classes("btn", "btn-secondary")
                        attr("data-bs-dismiss", "modal")
                        onClick { onCancel() }
                    }) {
                        Text("Cancel")
                    }
                    Button(attrs = {
                        type(ButtonType.Button)
                        classes("btn", "btn-danger")
                        attr("data-bs-dismiss", "modal")
                        onClick { onDelete() }
                    }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun loadingView(message: String) {
    Div(attrs = { classes("d-flex", "align-items-center", "justify-content-center") }) {
        B { Text(message) }
        Div(attrs = {
            classes("spinner-border", "ml-auto")
            attr("role", "status")
            attr("aria-hidden", "true")
        })
    }
}

@Composable
fun PrimaryButton(
    text: String,
    vararg extraClasses: String,
    onClick: () -> Unit
) {
    val classes = extraClasses.plus(arrayOf("btn", "btn-dark", "btn-block", "rounded-pill", "btn-sm", "px-5"))
    Button(attrs = {
        classes(*classes)
        style { height(35.px) }
        type(ButtonType.Button)
        onClick { onClick() }
    }) { Text(text) }
}

@Composable
fun PrimaryButtonLink(
    text: String,
    vararg extraClasses: String,
    link: String? = null,
    target: ATarget = ATarget.Blank,
    onClick: () -> Unit = {}
) {
    val classes = extraClasses.plus(arrayOf("btn", "btn-dark", "btn-block", "rounded-pill", "btn-sm", "px-5"))
    A(attrs = {
        classes(*classes)
        style { height(35.px) }
        attr("type", "button")
        link?.let { href(it) }
        target(target)
        onClick { onClick() }
    }) { Text(text) }
}

@Composable
fun ButtonWithLoadingState(
    text: String,
    resource: Resource,
    vararg extraClasses: String,
    onClick: () -> Unit
) {
    val classes = extraClasses.plus(arrayOf("btn", "btn-dark", "btn-block", "rounded-pill", "btn-sm", "px-5"))
    Button(attrs = {
        classes(*classes)
        style { height(50.px) }
        onClick { onClick() }
        if (resource.isLoading()) {
            disabled()
        }
    }) {
        if (resource.isLoading()) {
            Span(attrs = {
                classes("spinner-border", "spinner-border-sm", "me-3")
                attr("role", "status")
                attr("aria-hidden", "true")
            })
        }
        Text(text)
    }
}

@Composable
fun FileInputView(
    fileType: FileType,
    onChange: (File) -> Unit
) {
    Input(type = InputType.File, attrs = {
        id(fileType.name)
        accept(".csv")
        required(true)
        classes("form-control")
        onChange {
            it.target.files?.let { fileList ->
                fileList[0]?.let { file ->
                    onChange(file)
                }
            }
        }
    })
}