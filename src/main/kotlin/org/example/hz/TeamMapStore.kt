package org.example.hz

import com.hazelcast.map.MapStore
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TeamMapStore(
    private val repository: TeamCassandraRepository
) : MapStore<UUID, Team> {

    override fun load(key: UUID): Team? {
        return repository.findById(key)
    }

    override fun loadAll(keys: MutableCollection<UUID>): MutableMap<UUID, Team> {

        val result = mutableMapOf<UUID, Team>()

        for (key in keys) {

            val team = repository.findById(key)

            if (team != null) {
                result[key] = team
            }
        }

        return result
    }

    override fun loadAllKeys(): MutableIterable<UUID>? {
        return null
    }

    override fun deleteAll(keys: MutableCollection<UUID>) {
        for (key in keys) {
            repository.delete(key)
        }
    }

    override fun delete(key: UUID) {
        repository.delete(key)
    }

    override fun storeAll(map: MutableMap<UUID, Team>) {
        for ((id, team) in map) {
            repository.store(id, team)
        }
    }

    override fun store(key: UUID, value: Team) {
        repository.store(key, value)
    }
}