package org.example.hz

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.EntryProcessor
import com.hazelcast.map.IMap
import org.example.hz.Error.Code.*
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

@Service
class TeamHazelcastService(
    private val hz: HazelcastInstance,
    private val executor: TaskExecutor
) {

    private lateinit var teamsHzMap: IMap<UUID, Team>

    @PostConstruct
    fun setUp() {
        teamsHzMap = hz.getMap(DatabaseConfiguration.TEAMS_MAP_NAME)
    }

    fun findById(id: UUID): Response<Team> {
        val team = teamsHzMap[id]
        return if (team != null) Processed(team) else Error(code = NOT_FOUND, message = "team is missing")
    }

    fun create(teamId: UUID, leaderId: UUID, mode: Mode): Response<Team> {
        return teamsHzMap.executeOnKey(teamId, CreateEntryProcessor(leaderId, mode))
    }

    fun disband(teamId: UUID, senderId: UUID): Response<Unit> {
        return teamsHzMap.executeOnKey(teamId, DisbandEntryProcessor(senderId))
    }

    fun addMember(teamId: UUID, senderId: UUID, memberId: UUID): Response<Team> {
        return teamsHzMap.executeOnKey(teamId, AddMemberEntryProcessor(senderId, memberId))
    }

    fun kickMember(teamId: UUID, senderId: UUID, memberId: UUID): Response<Team> {
        return teamsHzMap.executeOnKey(teamId, KickMemberEntryProcessor(senderId, memberId))
    }

    fun leave(teamId: UUID, memberId: UUID): Response<Team> {
        return teamsHzMap.executeOnKey(teamId, LeaveEntryProcessor(memberId))
    }

    fun changeMode(teamId: UUID, senderId: UUID, mode: Mode): Response<Team> {
        return teamsHzMap.executeOnKey(teamId, ChangeModeEntryProcessor(senderId, mode))
    }

    fun changeLeader(teamId: UUID, senderId: UUID, memberId: UUID): Response<Team> {
        return teamsHzMap.executeOnKey(teamId, ChangeLeaderEntryProcessor(senderId, memberId))
    }
}

class CreateEntryProcessor(val leaderId: UUID, val mode: Mode) : EntryProcessor<UUID, Team, Response<Team>> {

    override fun process(entry: MutableMap.MutableEntry<UUID, Team?>): Response<Team> {

        val loadedTeam: Team? = entry.value

        return if (loadedTeam == null) {
            val team = Team(leaderId, mode)
            entry.setValue(team)
            Processed(team)
        } else if (loadedTeam.leaderId == leaderId && loadedTeam.mode == mode) {
            Processed(loadedTeam)
        } else {
            Error(code = ALREADY_CREATED, message = "team with id=${entry.key} already created")
        }
    }
}

class DisbandEntryProcessor(val senderId: UUID) : EntryProcessor<UUID, Team, Response<Unit>> {
    override fun process(entry: MutableMap.MutableEntry<UUID, Team?>): Response<Unit> {

        val team = entry.value

        return if (team != null) {
            if (team.leaderId == senderId) {
                entry.setValue(null)
                Processed(Unit)
            } else Error(code = FORBIDDEN, "no right for disband")
        } else Processed(Unit)
    }
}

abstract class TeamEntryProcessor : EntryProcessor<UUID, Team, Response<Team>> {

    abstract fun processTeam(team: Team): Response<Team>

    override fun process(entry: MutableMap.MutableEntry<UUID, Team?>): Response<Team> {

        val team = entry.value

        return if (team != null) {

            val response = processTeam(team)

            if (response is Processed<Team>) {
                entry.setValue(response.value)
            }

            response

        } else Error(code = NOT_FOUND, "team not found")
    }
}

class AddMemberEntryProcessor(val senderId: UUID, val memberId: UUID) : TeamEntryProcessor() {

    override fun processTeam(team: Team): Response<Team> {
        return team.addMember(senderId, memberId)
    }
}

class KickMemberEntryProcessor(val senderId: UUID, val memberId: UUID) : TeamEntryProcessor() {

    override fun processTeam(team: Team): Response<Team> {
        return team.kickMember(senderId, memberId)
    }
}

class LeaveEntryProcessor(val memberId: UUID) : TeamEntryProcessor() {

    override fun processTeam(team: Team): Response<Team> {
        return team.removeMember(memberId)
    }
}

class ChangeModeEntryProcessor(val senderId: UUID, val mode: Mode) : TeamEntryProcessor() {

    override fun processTeam(team: Team): Response<Team> {
        return team.changeMode(senderId, mode)
    }
}

class ChangeLeaderEntryProcessor(val senderId: UUID, val memberId: UUID) : TeamEntryProcessor() {

    override fun processTeam(team: Team): Response<Team> {
        return team.changeLeader(senderId, memberId)
    }
}