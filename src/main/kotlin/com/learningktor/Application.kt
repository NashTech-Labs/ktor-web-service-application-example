package com.learningktor

import com.learningktor.plugins.configureRouting
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.lang.Exception

/**
 * This is the main function that starts the Ktor application.
 *
 * It configures an embedded server using Netty, sets up content negotiation with JSON,
 * initializes application modules, and configures routing.
 *
 * @param args The command-line arguments.
 */
fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        // Install ContentNegotiation plugin to handle JSON serialization.
        install(ContentNegotiation) {
            json()
        }
        // Initialize application modules.
        module()
        module2()
        // Configure routing for the application.
        configureRouting()
    }.start(true)
}

/**
 * Configures routing for the Ktor application.
 *
 * It defines various routes and their associated behaviors, including handling requests
 * for static resources, responding to specific HTTP methods, and handling query parameters.
 *
 * @receiver The Ktor application to which routing is being configured.
 */
fun Application.module() {
    install(Routing) {
        // Serve static resources like HTML files.
        static {
            resource("amazon.html")
            resource("linkedin.html")
        }

        // Define a simple GET route to greet users.
        get("/greet") {
            call.respondText("Hello, Welcome to Ktor Tutorials using embedded server!!")
        }

        // Define a route using 'route' and 'handle' for GET method.
        route("/route", method = HttpMethod.Get) {
            handle {
                call.respondText("Get method using route and handle!!")
            }
        }

        // Define a route that accepts usernames and handles headers.
        get("/users/{name}") {
            val name = call.parameters["name"]
            // Request header connection
            val header = call.request.headers["Connection"]
            // Set Custom header for Admin.
            if (name == "Admin") {
                call.response.header("CustomHeader", "Admin")
                call.respondText("Hello Admin", status = HttpStatusCode.OK)
            }
            call.respondText("Hello CraftMan/CraftWoman $name with header: $header !!!")
        }

        // Define a route that handles query parameters.
        get("/details") {
            val name = call.request.queryParameters["name"]
            val studio = call.request.queryParameters["studio"]
            call.respondText("Hi, I am $name from $studio Studio!!")
        }

        // Define a route that fetches employee details and handles exceptions.
        get("/employee") {
            try {
                val employee = Employee("Rishika", 1852)
                call.respond(message = employee, status = HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(message = "${e.message}", status = HttpStatusCode.BadRequest)
            }
        }
           // Define routes for redirection.
        get("/redirect-push") {
            call.respondRedirect(url = "/redirect-pop", permanent = false)
        }
        get("/redirect-pop") {
            call.respondText("Hey, You have successfully redirected!! [From - redirect-push To- redirect-pop] ***")
        }
        post {
            // Create a new employee.
            val newEmployee = call.receive<Employee>()
            // Logic to save the new employee goes here.
            call.respond(HttpStatusCode.Created, newEmployee)
        }
        put("/{id}") {
            // Update an employee by ID.
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            } else {
                val updatedEmployee = call.receive<Employee>()
                // Logic to update the employee goes here.
                call.respond(HttpStatusCode.OK, updatedEmployee)
            }
        }
    }
}

/**
 * A separate module for routing.
 *
 * It defines a route for a simple greeting message.
 *
 * @receiver The Ktor application to which routing is being configured.
 */
fun Application.module2() {
    routing {
        get("/new-greet") {
            call.respondText("This is another get call which shows that how we can use routing with different module as well..!!")
        }
    }
}

/**
 * Data class representing an Employee.
 *
 * @property name The name of the employee.
 * @property id The ID of the employee.
 */
@Serializable
data class Employee(
    val name: String,
    var id: Int
)


