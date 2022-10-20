package org.example.hz

import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("default")
@DisplayName("TeamService tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TeamServiceTest {

    @Autowired
    private lateinit var service: TeamHazelcastService

    private val teamId: UUID = UUID.randomUUID()
    private val leaderId: UUID = UUID.randomUUID()

    private val mode = Mode.TEAM_DEATH_MATCH

    private val memberId1: UUID = UUID.randomUUID()
    private val memberId2: UUID = UUID.randomUUID()

    @Test
    @Order(1)
    fun testCreateTeam() {
        service.create(teamId, leaderId, mode)

        val result = service.findById(teamId)

        assertEquals(expected = Processed(Team(leaderId, mode)), actual = result)
    }

    @Test
    @Order(2)
    fun testAddMember() {

        val addResult1 = service.addMember(teamId, leaderId, memberId1)
        val addResult2 = service.addMember(teamId, leaderId, memberId2)

        assertAll(
            {
                assertEquals(
                    expected = Processed(Team(leaderId, mode, setOf(memberId1))),
                    actual = addResult1
                )
            },
            {
                assertEquals(
                    expected = Processed(Team(leaderId, mode, setOf(memberId1, memberId2))),
                    actual = addResult2
                )
            }
        )
    }

    @Test
    @Order(3)
    fun testKickMember() {

        val kickResult = service.kickMember(teamId, leaderId, memberId1)

        assertEquals(
            expected = Processed(Team(leaderId, mode, setOf(memberId2))),
            actual = kickResult
        )
    }

    @Test
    @Order(4)
    fun testLeave() {

        val kickResult = service.leave(teamId, memberId2)

        assertEquals(
            expected = Processed(Team(leaderId, mode)),
            actual = kickResult
        )
    }

    @Test
    @Order(4)
    fun testChangeMode() {

        val changeModeResult = service.changeMode(teamId, leaderId, Mode.TEAM_VS_TEAM)

        assertEquals(
            expected = Processed(Team(leaderId, Mode.TEAM_VS_TEAM)),
            actual = changeModeResult
        )
    }

    @Test
    @Order(5)
    fun testDisband() {

        val disbandResult = service.disband(teamId, leaderId)
        val findResult = service.findById(teamId)

        assertAll(
            { assertEquals(expected = Processed(Unit), actual = disbandResult) },
            { assertTrue(findResult is Error) },
            { assertEquals(expected = Error.Code.NOT_FOUND, actual = (findResult as Error).code) }
        )
    }

    @Test
    @Order(6)
    fun teamConsistencyTest() {

        val teamId = UUID.randomUUID()
        val leaderId = UUID.randomUUID()

        val mode = Mode.TEAM_VS_TEAM

        service.create(teamId, leaderId, mode)

        val concurrentRequests = (Mode.TEAM_VS_TEAM.membersPerTeam - 1) * 2
        val executor = Executors.newFixedThreadPool(concurrentRequests)

        val futures = mutableListOf<Future<Response<*>>>()

        val latch = CountDownLatch(1)

        for (i in 0 until concurrentRequests) {
            futures.add(
                executor.submit(
                    Callable {
                        latch.await()
                        service.addMember(teamId, leaderId, memberId = UUID.randomUUID())
                    }
                )
            )
        }

        latch.countDown()

        var successful = 0
        var failed = 0

        for (future in futures) {
            when (future.get()) {
                is Processed    -> successful++
                is Error        -> failed++
            }
        }

        val findResult = service.findById(teamId)

        assertTrue(findResult is Processed)
        assertEquals(expected = mode.membersPerTeam - 1, actual = findResult.value.members.size)
        assertEquals(expected = mode.membersPerTeam - 1, successful)
        assertEquals(expected = mode.membersPerTeam - 1, failed)
    }
}