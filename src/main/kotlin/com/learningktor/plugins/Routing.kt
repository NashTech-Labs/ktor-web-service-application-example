package com.learningktor.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * This function configures routing for the Ktor application.
 *
 * It sets up a basic route to respond with a welcome message when the root URL ("/") is accessed.
 *
 * @receiver The Ktor application to which routing is being configured.
 */
fun Application.configureRouting() {
    routing {
        /**
         * Defines a route for the root URL ("/").
         *
         * When a GET request is made to the root URL, it responds with a welcome message.
         */
        get("/") {
            call.respondText("Hey, Welcome to the journey of learning Ktor!!")
        }
    }
}
