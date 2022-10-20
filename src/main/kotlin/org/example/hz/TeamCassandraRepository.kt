package org.example.hz

import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.PreparedStatement
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.querybuilder.QueryBuilder
import org.springframework.stereotype.Repository
import java.nio.ByteBuffer
import java.util.*
import javax.annotation.PostConstruct


@Repository
class TeamCassandraRepository(
    private val session: CqlSession
) {

    private lateinit var selectPreparedQuery: PreparedStatement
    private lateinit var updatePreparedQuery: PreparedStatement
    private lateinit var deletePreparedQuery: PreparedStatement
    private lateinit var truncatePreparedQuery: PreparedStatement

    @PostConstruct
    fun setUp() {
        selectPreparedQuery = session.prepare(SELECT_QUERY)
        updatePreparedQuery = session.prepare(UPDATE_QUERY)
        deletePreparedQuery = session.prepare(DELETE_QUERY)
        truncatePreparedQuery = session.prepare(TRUNCATE_QUERY)
    }

    fun findById(id: UUID): Team? {

        val bound = selectPreparedQuery.boundStatementBuilder()
            .setUuid(TEAMS_COLUMN_ID, id)
            .build()

        return session.execute(bound).one()
            ?.let { row -> row.getByteBuffer(TEAMS_COLUMN_DATA)?.array() }
            ?.let { bytes -> Json.mapper.readValue(bytes, Team::class.java) }
    }

    fun store(id: UUID, team: Team) {

        val bound = updatePreparedQuery.boundStatementBuilder()
            .setUuid(TEAMS_COLUMN_ID, id)
            .setByteBuffer(TEAMS_COLUMN_DATA, ByteBuffer.wrap(Json.mapper.writeValueAsBytes(team)))
            .build()

        session.execute(bound)
    }

    fun delete(id: UUID) {

        val bound = deletePreparedQuery.boundStatementBuilder()
            .setUuid(TEAMS_COLUMN_ID, id)
            .build()

        session.execute(bound)
    }

    fun deleteAll() {
        val bound = truncatePreparedQuery.boundStatementBuilder().build()
        session.execute(bound)
    }

    companion object {

        private const val TEAMS_TABLE = "teams"
        private const val TEAMS_COLUMN_ID = "id"
        private const val TEAMS_COLUMN_DATA = "data"

        private val SELECT_QUERY: SimpleStatement = SimpleStatement.builder(
            QueryBuilder.selectFrom(TEAMS_TABLE)
                .column(TEAMS_COLUMN_ID)
                .column(TEAMS_COLUMN_DATA)
                .whereColumn(TEAMS_COLUMN_ID).isEqualTo(QueryBuilder.bindMarker(TEAMS_COLUMN_ID))
                .build()
        )
            .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
            .build()

        private val UPDATE_QUERY: SimpleStatement = SimpleStatement.builder(
            QueryBuilder.update(TEAMS_TABLE)
                .setColumn(TEAMS_COLUMN_DATA, QueryBuilder.bindMarker(TEAMS_COLUMN_DATA))
                .whereColumn(TEAMS_COLUMN_ID).isEqualTo(QueryBuilder.bindMarker(TEAMS_COLUMN_ID))
                .build()
        )
            .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
            .build()

        private val DELETE_QUERY: SimpleStatement = SimpleStatement.builder(
            QueryBuilder.deleteFrom(TEAMS_TABLE)
                .whereColumn(TEAMS_COLUMN_ID).isEqualTo(QueryBuilder.bindMarker(TEAMS_COLUMN_ID))
                .build()
        )
            .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
            .build()

        private val TRUNCATE_QUERY: SimpleStatement = SimpleStatement.builder(
            QueryBuilder.truncate(TEAMS_TABLE).build()
        )
            .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
            .build()
    }
}