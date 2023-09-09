package com.learningktor

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class ApplicationTest {


    class KtorApplicationTest {

        @Test
        fun testRoot() {
            withTestApplication(Application::module) {
                handleRequest(HttpMethod.Get, "/greet").apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals("Hello, Welcome to Ktor Tutorials using embedded server!!", response.content)
                }
            }
        }

        @Test
        fun testRoute() {
            withTestApplication(Application::module) {
                handleRequest(HttpMethod.Get, "/route").apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals("Get method using route and handle!!", response.content)
                }
            }
        }

        @Test
        fun testUserEndpoint() {
            withTestApplication(Application::module) {
                handleRequest(HttpMethod.Get, "/users/John").apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals("Hello CraftMan/CraftWoman John with header: null !!!", response.content)
                }
            }
        }

        @Test
        fun testEmployeeRoute_Success() {
            withTestApplication({
                install(ContentNegotiation) {
                    json(Json {
                        // Configure JSON serialization options if needed
                        prettyPrint = true
                    })
                }
                module()
            }) {
                handleRequest(HttpMethod.Get, "/employee").apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val expectedJson = """{"name":"Rishika","id":1852}"""
                    val actualJson = response.content?.replace("\\s".toRegex(), "") // Remove whitespace
                    assertEquals(expectedJson, actualJson)
                }
            }
        }


        @Test
        fun testEmployeeRoute_Exception() {
            val exceptionMessage = "Employee not found" // Declare exceptionMessage here

            withTestApplication({
                install(ContentNegotiation) {
                    json(Json {
                        // Configure JSON serialization options if needed
                        prettyPrint = true
                    })
                }

                // Define a separate module and routing block
                routing {
                    get("/employee") {
                        throw Exception(exceptionMessage)
                    }
                }
            }) {
                handleRequest(HttpMethod.Get, "/employee").apply {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals(exceptionMessage, response.content)
                }
            }
        }

        @Test
        fun testUpdateEmployee_NotFound() {
            val nonExistingId = 1852 // An ID that doesn't exist in the system
            val updatedEmployee = Employee("UpdatedName", nonExistingId)

            withTestApplication({
                install(ContentNegotiation) {
                    json(Json {
                        // Configure JSON serialization options if needed
                        prettyPrint = true
                    })
                }

                routing {
                    put("/{id}") {
                        val id = call.parameters["id"]?.toIntOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                        } else {
                            val existingEmployee: Employee? = null // Simulate the employee not found scenario

                            if (existingEmployee != null) {
                                val updatedEmployeeData = call.receive<Employee>()
                                updatedEmployeeData.id = id

                                call.respond(HttpStatusCode.OK, updatedEmployeeData)
                            } else {
                                call.respond(HttpStatusCode.NotFound, "Employee not found")
                            }
                        }
                    }
                }
            }) {
                handleRequest(HttpMethod.Put, "/$nonExistingId") {
                    setBody(Json.encodeToString(Employee.serializer(), updatedEmployee))
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }.apply {
                    assertEquals(HttpStatusCode.NotFound, response.status())
                    assertEquals("Employee not found", response.content)
                }
            }
        }


        @Test
        fun testUpdateEmployee_MissingData() {
            val employeeId = 123
            val invalidEmployeeData = Employee("", employeeId) // Missing data, such as an empty name

            withTestApplication({
                install(ContentNegotiation) {
                    json(Json {
                        // Configure JSON serialization options if needed
                        prettyPrint = true
                    })
                }

                routing {
                    put("/{id}") {
                        val id = call.parameters["id"]?.toIntOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                        } else {
                            val existingEmployee = 1851
                            if (existingEmployee != null) {
                                val isValidEmployeeData: (Employee) -> Boolean = { employee ->
                                    !employee.name.isBlank()
                                }

                                // Check for missing or invalid data
                                if (isValidEmployeeData(call.receive())) {
                                    val updatedEmployeeData = call.receive<Employee>()
                                    updatedEmployeeData.id = id

                                    call.respond(HttpStatusCode.OK, updatedEmployeeData)
                                } else {
                                    call.respond(HttpStatusCode.BadRequest, "Invalid employee data")
                                }
                            } else {
                                call.respond(HttpStatusCode.NotFound, "Employee not found")
                            }
                        }
                    }
                }
            }) {
                handleRequest(HttpMethod.Put, "/$employeeId") {
                    setBody(Json.encodeToString(Employee.serializer(), invalidEmployeeData))
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }.apply {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("Invalid employee data", response.content)
                }
            }
        }
    }

}
