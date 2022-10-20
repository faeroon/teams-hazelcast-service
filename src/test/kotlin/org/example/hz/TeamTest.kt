package org.example.hz

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TeamTest {

    private fun createTeam(): Team = Team(leaderId = UUID.randomUUID(), mode = Mode.TEAM_VS_TEAM)

    //region team tests

    @Test
    fun testAddMemberFailsForNonLeader() {

        val team = createTeam()

        val response = team.addMember(senderId = UUID.randomUUID(), memberId = UUID.randomUUID())

        assertTrue(response is Error)
        assertEquals(expected = Error.Code.FORBIDDEN, actual = response.code)
    }

    @Test
    fun testAddMemberFailsForLeader() {

        val team = createTeam()
        val leaderId = team.leaderId

        val response = team.addMember(senderId = leaderId, memberId = leaderId)

        assertTrue(response is Error)
        assertEquals(expected = Error.Code.INVALID_MEMBER_ID, actual = response.code)
    }

    @Test
    fun testAddMemberFailsIfTeamIsFull() {

        val members = mutableSetOf<UUID>()

        for (i in 1 until Mode.TEAM_VS_TEAM.membersPerTeam) {
            members.add(UUID.randomUUID())
        }

        val team = Team(leaderId = UUID.randomUUID(), mode = Mode.TEAM_VS_TEAM, members = members)

        val response = team.addMember(senderId = team.leaderId, memberId = UUID.randomUUID())

        assertTrue(response is Error)
        assertEquals(expected = Error.Code.MEMBERS_COUNT_EXCEEDED, actual = response.code)
    }

    @Test
    fun testAddMemberPassesIfTeamIsNotFull() {

        val members = mutableSetOf<UUID>()

        for (i in 1 until Mode.TEAM_VS_TEAM.membersPerTeam - 1) {
            members.add(UUID.randomUUID())
        }

        val addedMember = UUID.randomUUID()

        val leaderId = UUID.randomUUID()
        val team = Team(leaderId = leaderId, mode = Mode.TEAM_VS_TEAM, members = members)

        val response = team.addMember(team.leaderId, addedMember)

        assertEquals(
            expected = Processed(Team(leaderId, Mode.TEAM_VS_TEAM, members + addedMember)),
            actual = response
        )
    }

    @Test
    fun testAddMemberMakesNothingForAlreadyAdded() {

        val memberId = UUID.randomUUID()
        val leaderId = UUID.randomUUID()

        val team = Team(leaderId, Mode.TEAM_VS_TEAM, setOf(memberId))

        val response = team.addMember(leaderId, memberId)

        assertEquals(expected = Processed(team), actual = response)
    }

    //endregion

    //region kickMember

    @Test
    fun testKickMemberFailsForNonLeader() {

        val memberId = UUID.randomUUID()
        val leaderId = UUID.randomUUID()

        val team = Team(leaderId, Mode.TEAM_VS_TEAM, setOf(memberId))

        val response = team.kickMember(memberId, memberId)

        assertTrue(response is Error)
        assertEquals(expected = Error.Code.FORBIDDEN, actual = response.code)
    }

    @Test
    fun testCanNotKickLeader() {

        val memberId = UUID.randomUUID()
        val leaderId = UUID.randomUUID()

        val team = Team(leaderId, Mode.TEAM_VS_TEAM, setOf(memberId))

        val response = team.kickMember(leaderId, leaderId)

        assertTrue(response is Error)
        assertEquals(expected = Error.Code.INVALID_MEMBER_ID, actual = response.code)
    }

    @Test
    fun testKickMemberPasses() {

        val memberId = UUID.randomUUID()
        val leaderId = UUID.randomUUID()

        val team = Team(leaderId, Mode.TEAM_VS_TEAM, setOf(memberId))

        val response = team.kickMember(leaderId, memberId)

        assertEquals(expected = Processed(Team(leaderId, Mode.TEAM_VS_TEAM)), actual = response)
    }

    @Test
    fun testKickMemberMakesNothingIfAlreadyKicked() {

        val leaderId = UUID.randomUUID()
        val team = Team(leaderId, Mode.TEAM_VS_TEAM)

        val response = team.kickMember(leaderId, UUID.randomUUID())

        assertEquals(expected = Processed(team), actual = response)
    }

    //endregion

    //region change leader tests

    @Test
    fun testChangeLeaderFailsForNonLeader() {

        val leaderId = UUID.randomUUID()
        val memberId = UUID.randomUUID()

        val team = Team(leaderId, Mode.TEAM_VS_TEAM, setOf(memberId))

        val response = team.changeLeader(memberId, memberId)

        assertTrue(response is Error)
        assertEquals(expected = Error.Code.FORBIDDEN, actual = response.code)
    }

    @Test
    fun testChangeLeaderFailsIfMemberIsMissing() {

        val leaderId = UUID.randomUUID()

        val team = Team(leaderId, Mode.TEAM_VS_TEAM)

        val response = team.changeLeader(leaderId, memberId = UUID.randomUUID())

        assertTrue(response is Error)
        assertEquals(expected = Error.Code.INVALID_MEMBER_ID, actual = response.code)
    }

    @Test
    fun testChangeLeaderPasses() {

        val leaderId = UUID.randomUUID()
        val memberId1 = UUID.randomUUID()
        val memberId2 = UUID.randomUUID()

        val team = Team(leaderId, Mode.TEAM_VS_TEAM, setOf(memberId1, memberId2))

        val response = team.changeLeader(leaderId, memberId1)

        assertEquals(
            expected = Processed(
                Team(
                    leaderId = memberId1,
                    mode = Mode.TEAM_VS_TEAM,
                    members = setOf(leaderId, memberId2)
                )
            ),
            actual = response
        )
    }

    @Test
    fun testChangeLeaderMakesNothingForLeader() {

        val leaderId = UUID.randomUUID()

        val team = Team(leaderId, Mode.TEAM_VS_TEAM)

        val response = team.changeLeader(leaderId, leaderId)

        assertEquals(expected = Processed(team), actual = response)
    }

    //endregion

    //region change mode tests

    @Test
    fun testChangeModeFailsForNonLeader() {

        val leaderId = UUID.randomUUID()

        val team = Team(leaderId, Mode.TEAM_VS_TEAM)

        val response = team.changeMode(UUID.randomUUID(), Mode.TEAM_DEATH_MATCH)

        assertTrue(response is Error)
        assertEquals(expected = Error.Code.FORBIDDEN, actual = response.code)
    }

    @Test
    fun testChangeModeFailsIfMembersCountIsTooBig() {

        val leaderId = UUID.randomUUID()

        val members = mutableSetOf<UUID>()

        for (i in 1 until Mode.TEAM_VS_TEAM.membersPerTeam) {
            members.add(UUID.randomUUID())
        }

        val team = Team(leaderId, Mode.TEAM_VS_TEAM, members)

        val response = team.changeMode(leaderId, Mode.TEAM_DEATH_MATCH)

        assertTrue(response is Error)
        assertEquals(expected = Error.Code.MEMBERS_COUNT_EXCEEDED, actual = response.code)
    }

    @Test
    fun testChangeModePasses() {

        val leaderId = UUID.randomUUID()

        val members = mutableSetOf<UUID>()

        for (i in 1 until Mode.TEAM_DEATH_MATCH.membersPerTeam) {
            members.add(UUID.randomUUID())
        }

        val team = Team(leaderId, Mode.TEAM_VS_TEAM, members)

        val response = team.changeMode(leaderId, Mode.TEAM_DEATH_MATCH)

        assertEquals(
            expected = Processed(Team(leaderId, Mode.TEAM_DEATH_MATCH, members)),
            actual = response
        )
    }

    //endregion
}