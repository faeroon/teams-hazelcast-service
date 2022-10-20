package org.example.hz

import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.config.DefaultDriverOption.*
import com.datastax.oss.driver.api.core.config.DriverConfigLoader
import com.datastax.oss.driver.internal.core.connection.ExponentialReconnectionPolicy
import com.hazelcast.config.*
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.Duration

@Configuration
class DatabaseConfiguration {

    @Bean(destroyMethod = "close")
    fun cassandraMainCqlSession(props: ApplicationProperties): CqlSession {

        val config = DriverConfigLoader.programmaticBuilder()
            .withBoolean(RECONNECT_ON_INIT, true)
            .withString(RECONNECTION_POLICY_CLASS, ExponentialReconnectionPolicy::class.java.simpleName)
            .withDuration(RECONNECTION_BASE_DELAY, Duration.ofSeconds(1))
            .withDuration(RECONNECTION_MAX_DELAY, Duration.ofSeconds(60))
            .withDuration(REQUEST_TIMEOUT, Duration.ofSeconds(15))
            .withString(REQUEST_CONSISTENCY, ConsistencyLevel.QUORUM.name())
            .build()

        return CqlSession.builder()
            .withApplicationName("example")
            .withKeyspace("teams")
            .addContactPoints(props.cassandra.parseContactPoints())
            .withLocalDatacenter(props.cassandra.datacenter)
            .withConfigLoader(config)
            .build()
    }

    @Bean(destroyMethod = "shutdown")
    fun hazelcastInstance(
        props: ApplicationProperties,
        teamMapStore: TeamMapStore
    ): HazelcastInstance {

        val config = Config()

        addSerializers(config)

        config.networkConfig.port = props.hazelcast.port
        config.networkConfig.isPortAutoIncrement = false
        config.clusterName = props.hazelcast.cluster

        config.addMapConfig(MapConfig().also { mapConfig ->
            mapConfig.name = TEAMS_MAP_NAME
            mapConfig.mapStoreConfig = MapStoreConfig().also { mapStoreConfig ->
                mapStoreConfig.isEnabled = true
                mapStoreConfig.implementation = teamMapStore
                mapStoreConfig.initialLoadMode = MapStoreConfig.InitialLoadMode.EAGER
            }
        })

        return Hazelcast.newHazelcastInstance(config)
    }

    private fun addSerializers(config: Config) {

        config.serializationConfig.serializerConfigs.add(
            SerializerConfig().also {
                it.typeClass = Team::class.java
                it.implementation = ByteArraySerializers.TEAM_SERIALIZER
            }
        )

        config.serializationConfig.serializerConfigs.add(
            SerializerConfig().also {
                it.typeClass = Response::class.java
                it.implementation = ByteArraySerializers.RESPONSE_SERIALIZER
            }
        )

        config.serializationConfig.serializerConfigs.add(
            SerializerConfig().also {
                it.typeClass = Processed::class.java
                it.implementation = ByteArraySerializers.PROCESSED_SERIALIZER
            }
        )

        config.serializationConfig.serializerConfigs.add(
            SerializerConfig().also {
                it.typeClass = Error::class.java
                it.implementation = ByteArraySerializers.ERROR_SERIALIZER
            }
        )

        config.serializationConfig.serializerConfigs.add(
            SerializerConfig().also {
                it.typeClass = CreateEntryProcessor::class.java
                it.implementation = ByteArraySerializers.CREATE_ENTRY_PROCESSOR_SERIALIZER
            }
        )

        config.serializationConfig.serializerConfigs.add(
            SerializerConfig().also {
                it.typeClass = DisbandEntryProcessor::class.java
                it.implementation = ByteArraySerializers.DISBAND_ENTRY_PROCESSOR_SERIALIZER
            }
        )

        config.serializationConfig.serializerConfigs.add(
            SerializerConfig().also {
                it.typeClass = AddMemberEntryProcessor::class.java
                it.implementation = ByteArraySerializers.ADD_MEMBER_ENTRY_PROCESSOR_SERIALIZER
            }
        )

        config.serializationConfig.serializerConfigs.add(
            SerializerConfig().also {
                it.typeClass = KickMemberEntryProcessor::class.java
                it.implementation = ByteArraySerializers.KICK_MEMBER_ENTRY_PROCESSOR_SERIALIZER
            }
        )

        config.serializationConfig.serializerConfigs.add(
            SerializerConfig().also {
                it.typeClass = LeaveEntryProcessor::class.java
                it.implementation = ByteArraySerializers.LEAVE_ENTRY_PROCESSOR_SERIALIZER
            }
        )

        config.serializationConfig.serializerConfigs.add(
            SerializerConfig().also {
                it.typeClass = ChangeModeEntryProcessor::class.java
                it.implementation = ByteArraySerializers.CHANGE_MODE_ENTRY_PROCESSOR_SERIALIZER
            }
        )

        config.serializationConfig.serializerConfigs.add(
            SerializerConfig().also {
                it.typeClass = ChangeLeaderEntryProcessor::class.java
                it.implementation = ByteArraySerializers.CHANGE_LEADER_ENTRY_PROCESSOR_SERIALIZER
            }
        )
    }

    @Bean
    fun executor(): TaskExecutor {

        val executor = ThreadPoolTaskExecutor()

        executor.maxPoolSize = 10
        executor.setAwaitTerminationSeconds(30)
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.threadNamePrefix = "service-thread-"

        return executor
    }

    companion object {
        const val TEAMS_MAP_NAME: String = "teams"
    }
}