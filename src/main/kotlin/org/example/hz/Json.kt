package org.example.hz

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.kotlinModule

object Json {

    val mapper = ObjectMapper()

    init {
        mapper.registerModule(Jdk8Module())
        mapper.registerModule(JavaTimeModule())
        mapper.registerModule(kotlinModule { configure(KotlinFeature.NullIsSameAsDefault, true) })
    }
}