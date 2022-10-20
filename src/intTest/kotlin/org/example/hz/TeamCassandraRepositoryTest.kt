package org.example.hz

import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

@SpringBootTest
@ActiveProfiles("default")
@DisplayName("TeamCassandraRepository tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TeamCassandraRepositoryTest {

    private val teamId = UUID.randomUUID()
    private val leaderId = UUID.randomUUID()

    private val memberId1 = UUID.randomUUID()
    private val memberId2 = UUID.randomUUID()

    private val mode = Mode.TEAM_DEATH_MATCH

    @Autowired
    private lateinit var repository: TeamCassandraRepository

    @BeforeAll
    fun setUp() {
        repository.deleteAll()
    }

    @Test
    @Order(1)
    fun testFindNonExistentTeam() {
        assertNull(repository.findById(teamId))
    }

    @Test
    @Order(2)
    fun testCreate() {
        val team = Team(leaderId, mode, members = setOf(memberId1))
        repository.store(teamId, team)
    }

    @Test
    @Order(3)
    fun testFindByIdExistingTeam() {
        val team = repository.findById(teamId)
        assertEquals(expected = Team(leaderId, mode, members = setOf(memberId1)), actual = team)
    }

    @Test
    @Order(4)
    fun testUpdate() {

        val team = Team(leaderId, mode, members = setOf(memberId1, memberId2))
        repository.store(teamId, team)

        val loadedTeam = repository.findById(teamId)
        assertEquals(expected = team, actual = loadedTeam)
    }

    @Test
    @Order(5)
    fun testDelete() {
        repository.delete(teamId)
        assertNull(repository.findById(teamId))
    }
}