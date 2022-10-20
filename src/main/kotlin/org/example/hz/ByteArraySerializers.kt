package org.example.hz

import com.hazelcast.nio.serialization.ByteArraySerializer
import org.example.hz.ProtobufMappers.toDomain
import org.example.hz.ProtobufMappers.toProto
import org.example.hz.proto.Teams

object ByteArraySerializers {

    val TEAM_SERIALIZER = object : ByteArraySerializer<Team> {

        override fun getTypeId(): Int = 1

        override fun read(buffer: ByteArray): Team = Teams.Team.parseFrom(buffer).toDomain()

        override fun write(team: Team): ByteArray = team.toProto().toByteArray()
    }

    val ERROR_SERIALIZER = object : ByteArraySerializer<Error> {

        override fun getTypeId(): Int = 2

        override fun read(buffer: ByteArray): Error = Teams.Error.parseFrom(buffer).toDomain()

        override fun write(error: Error): ByteArray = error.toProto().toByteArray()
    }

    val PROCESSED_SERIALIZER = object : ByteArraySerializer<Processed<*>> {

        override fun getTypeId(): Int = 3

        override fun read(buffer: ByteArray): Processed<*> = Teams.Processed.parseFrom(buffer).toDomain()

        override fun write(processed: Processed<*>): ByteArray = processed.toProto().toByteArray()
    }

    val RESPONSE_SERIALIZER = object : ByteArraySerializer<Response<*>> {

        override fun getTypeId(): Int = 4

        override fun read(buffer: ByteArray): Response<*> = Teams.Response.parseFrom(buffer).toDomain()

        override fun write(response: Response<*>): ByteArray = response.toProto().toByteArray()
    }

    val CREATE_ENTRY_PROCESSOR_SERIALIZER = object : ByteArraySerializer<CreateEntryProcessor> {

        override fun getTypeId(): Int = 5

        override fun read(buffer: ByteArray): CreateEntryProcessor =
            Teams.CreateEntryProcessor.parseFrom(buffer).toDomain()

        override fun write(response: CreateEntryProcessor): ByteArray = response.toProto().toByteArray()
    }

    val DISBAND_ENTRY_PROCESSOR_SERIALIZER = object : ByteArraySerializer<DisbandEntryProcessor> {

        override fun getTypeId(): Int = 6

        override fun read(buffer: ByteArray): DisbandEntryProcessor =
            Teams.DisbandEntryProcessor.parseFrom(buffer).toDomain()

        override fun write(response: DisbandEntryProcessor): ByteArray = response.toProto().toByteArray()
    }

    val ADD_MEMBER_ENTRY_PROCESSOR_SERIALIZER = object : ByteArraySerializer<AddMemberEntryProcessor> {

        override fun getTypeId(): Int = 7

        override fun read(buffer: ByteArray): AddMemberEntryProcessor =
            Teams.AddMemberEntryProcessor.parseFrom(buffer).toDomain()

        override fun write(response: AddMemberEntryProcessor): ByteArray = response.toProto().toByteArray()
    }

    val KICK_MEMBER_ENTRY_PROCESSOR_SERIALIZER = object : ByteArraySerializer<KickMemberEntryProcessor> {

        override fun getTypeId(): Int = 8

        override fun read(buffer: ByteArray): KickMemberEntryProcessor =
            Teams.KickMemberEntryProcessor.parseFrom(buffer).toDomain()

        override fun write(response: KickMemberEntryProcessor): ByteArray = response.toProto().toByteArray()
    }

    val LEAVE_ENTRY_PROCESSOR_SERIALIZER = object : ByteArraySerializer<LeaveEntryProcessor> {

        override fun getTypeId(): Int = 9

        override fun read(buffer: ByteArray): LeaveEntryProcessor =
            Teams.LeaveEntryProcessor.parseFrom(buffer).toDomain()

        override fun write(response: LeaveEntryProcessor): ByteArray = response.toProto().toByteArray()
    }

    val CHANGE_MODE_ENTRY_PROCESSOR_SERIALIZER = object : ByteArraySerializer<ChangeModeEntryProcessor> {

        override fun getTypeId(): Int = 10

        override fun read(buffer: ByteArray): ChangeModeEntryProcessor =
            Teams.ChangeModeEntryProcessor.parseFrom(buffer).toDomain()

        override fun write(response: ChangeModeEntryProcessor): ByteArray = response.toProto().toByteArray()
    }

    val CHANGE_LEADER_ENTRY_PROCESSOR_SERIALIZER = object : ByteArraySerializer<ChangeLeaderEntryProcessor> {

        override fun getTypeId(): Int = 11

        override fun read(buffer: ByteArray): ChangeLeaderEntryProcessor =
            Teams.ChangeLeaderEntryProcessor.parseFrom(buffer).toDomain()

        override fun write(response: ChangeLeaderEntryProcessor): ByteArray = response.toProto().toByteArray()
    }
}