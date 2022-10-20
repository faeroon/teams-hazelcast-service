package org.example.hz

import org.example.hz.Error.Code.*
import java.util.*

data class Team(
    val leaderId: UUID,
    val mode: Mode,
    val members: Set<UUID> = setOf()
) {

    fun addMember(senderId: UUID, memberId: UUID): Response<Team> = leaderAction(senderId) {
        addMember(memberId)
    }

    private fun addMember(memberId: UUID): Response<Team> {

        if (memberId == leaderId) return Error(code = INVALID_MEMBER_ID, message = "can't add leader in team")
        if (contains(memberId)) return Processed(this)

        return if (membersCount() < mode.membersPerTeam)
            Processed(copy(members = members + memberId))
        else
            Error(code = MEMBERS_COUNT_EXCEEDED, message = "can't add more members in mode=$mode")
    }

    fun kickMember(senderId: UUID, memberId: UUID): Response<Team> = leaderAction(senderId) { removeMember(memberId) }

    fun removeMember(memberId: UUID): Response<Team> {
        return Response.check(
            test = memberId != leaderId,
            error = { Error(code = INVALID_MEMBER_ID, message = "can't remove leader from team") },
            process = { copy(members = members - memberId) }
        )
    }

    fun changeLeader(senderId: UUID, memberId: UUID): Response<Team> = leaderAction(senderId) {
        return Response.check(
            test = contains(memberId),
            error = { Error(code = INVALID_MEMBER_ID, message = "member not found") },
            process = {
                if (leaderId != memberId) copy(leaderId = memberId, members = members - memberId + leaderId)
                else this
            }
        )
    }

    fun changeMode(senderId: UUID, mode: Mode): Response<Team> = leaderAction(senderId) {
        Response.check(
            test = membersCount() <= mode.membersPerTeam,
            error = { Error(code = MEMBERS_COUNT_EXCEEDED, "members count too big for mode $mode") },
            process = { copy(mode = mode) }
        )
    }

    private inline fun leaderAction(senderId: UUID, action: () -> Response<Team>): Response<Team> {
        return if (senderId == leaderId) action.invoke()
        else Error(code = FORBIDDEN, message = "$senderId is not a leader of team")
    }

    private fun contains(memberId: UUID): Boolean = leaderId == memberId || members.contains(memberId)

    private fun membersCount(): Int = members.size + 1
}

enum class Mode(val membersPerTeam: Int) {
    TEAM_DEATH_MATCH(3),
    TEAM_VS_TEAM(5)
}
