package org.example.hz

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.util.*

@Controller
@RequestMapping("/api")
class ApiController(
    private val service: TeamHazelcastService
) {

    @GetMapping(value = ["/teams/{teamId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findById(@PathVariable("teamId") teamId: UUID): ResponseEntity<String> = handleOk {
        service.findById(teamId)
    }

    @PostMapping(value = ["/teams/create"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody requestBytes: ByteArray): ResponseEntity<String> = handleCreated {
        val request = Json.mapper.readValue(requestBytes, CreateTeamRequest::class.java)
        service.create(request.id, request.leaderId, request.mode)
    }

    @PutMapping(value = ["/teams/{teamId}/add"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun addMember(
        @PathVariable("teamId") teamId: UUID,
        @RequestParam("sender") senderId: UUID,
        @RequestParam("member") memberId: UUID
    ): ResponseEntity<String> = handleOk {
        service.addMember(teamId, senderId, memberId)
    }

    @PutMapping(value = ["/teams/{teamId}/kick"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun kickMember(
        @PathVariable("teamId") teamId: UUID,
        @RequestParam("sender") senderId: UUID,
        @RequestParam("member") memberId: UUID
    ): ResponseEntity<String> = handleOk {
        service.kickMember(teamId, senderId, memberId)
    }

    @PutMapping(value = ["/teams/{teamId}/leave"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun leave(
        @PathVariable("teamId") teamId: UUID,
        @RequestParam("member") memberId: UUID
    ): ResponseEntity<String> = handleOk {
        service.leave(teamId, memberId)
    }

    @PutMapping(value = ["/teams/{teamId}/change-leader"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun changeLeader(
        @PathVariable("teamId") teamId: UUID,
        @RequestParam("sender") senderId: UUID,
        @RequestParam("member") memberId: UUID
    ): ResponseEntity<String> = handleOk {
        service.changeLeader(teamId, senderId, memberId)
    }

    @PutMapping(value = ["/teams/{teamId}/change-mode"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun changeMode(
        @PathVariable("teamId") teamId: UUID,
        @RequestParam("sender") senderId: UUID,
        @RequestParam("mode") mode: Mode
    ): ResponseEntity<String> = handleOk {
        service.changeMode(teamId, senderId, mode)
    }

    @DeleteMapping(value = ["/teams/{teamId}/disband"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun disband(
        @PathVariable("teamId") teamId: UUID,
        @RequestParam("sender") senderId: UUID
    ): ResponseEntity<String> = handleOk {
        service.disband(teamId, senderId)
    }

    private inline fun handleCreated(action: () -> Response<*>): ResponseEntity<String> {
        return handle(HttpStatus.CREATED, action)
    }

    private inline fun handleOk(action: () -> Response<*>): ResponseEntity<String> {
        return handle(HttpStatus.OK, action)
    }

    private inline fun handle(successStatus: HttpStatus, action: () -> Response<*>): ResponseEntity<String> {


        return try {
            when (val response = action.invoke()) {

                is Error -> {

                    val status = when (response.code) {
                        Error.Code.NOT_FOUND    -> HttpStatus.NOT_FOUND
                        Error.Code.FORBIDDEN    -> HttpStatus.FORBIDDEN
                        else                    -> HttpStatus.BAD_REQUEST
                    }

                    ResponseEntity.status(status).body(Json.mapper.writeValueAsString(response))
                }

                is Processed -> {
                    ResponseEntity.status(successStatus).body(Json.mapper.writeValueAsString(response.value))
                }
            }
        } catch (e: Throwable) {
            val error = Error(code = Error.Code.INTERNAL_ERROR, message = e.message ?: "")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Json.mapper.writeValueAsString(error))
        }
    }
}

data class CreateTeamRequest(val id: UUID, val leaderId: UUID, val mode: Mode)