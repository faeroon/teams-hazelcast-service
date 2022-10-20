package org.example.hz

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.PropertySource
import java.net.InetSocketAddress

@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "app")
class ApplicationProperties {

    class Cassandra {
        var contactpoints = "localhost:9042"
        var datacenter = "datacenter1"

        fun parseContactPoints(): List<InetSocketAddress> = contactpoints.split(",").map { point ->
            val parts = point.split(":")
            InetSocketAddress(parts[0], parts[1].toInt())
        }
    }

    class Hazelcast {
        var port = 5701
        var cluster = "dev"
    }

    var cassandra = Cassandra()
    var hazelcast = Hazelcast()
}