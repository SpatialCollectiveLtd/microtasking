package ui.screens.adminFeatures.signIn

import androidx.compose.runtime.Composable
import app.softwork.routingcompose.Router
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import repository.UserRepository
import ui.model.Screens.*
import ui.string.appTitle
import util.titleView

@Composable
fun adminSignIn(userRepository: UserRepository, router: Router) {
    if (userRepository.isUserSignedIn.value) {
        router.navigate(Admin.route + Home.route)
    }

    Div({ classes("container") }) {
        Div({ classes("row") }) {
            Div({ classes("col-md-12", "col-lg-12", "col-sm-12", "col-xl-12", "mt-5") }) {
                Img("/icons/logo.png") {
                    classes("rounded", "mx-auto", "d-block")
                }
            }
        }

        Div({ classes("row") }) {
            Div({ classes("col-md-12", "col-lg-12", "col-sm-12", "col-xl-12") }) {
                titleView(appTitle)
            }
        }

        Div({ classes("row", "d-flex", "justify-content-center", "mt-3") }) {
            Div({ classes("col-md-4", "col-lg-4", "col-sm-6", "col-xl-4") }) {
                Div(attrs = {
                    classes("g-signin2", "col-12", "d-flex", "justify-content-center")
                    attr("data-client_id", "34768107959-ad0p0iecdct89f4m401vsq4jo9u4afl5.apps.googleusercontent.com")
                    attr("data-callback", "handleCredentialResponse")
                    id("g_id_onload")
                })

                Div(attrs = {
                    classes("g_id_signin", "col-12", "d-flex", "justify-content-center")
                    attr("data-type", "standard")
                })

            }
        }
    }
}