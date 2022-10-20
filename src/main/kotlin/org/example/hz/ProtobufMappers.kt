package org.example.hz

import org.example.hz.proto.Teams
import java.util.*

object ProtobufMappers {

    fun Mode.toProto(): Teams.Mode = Teams.Mode.valueOf(this.name)

    fun Teams.Mode.toDomain(): Mode = Mode.valueOf(this.name)

    fun Team.toProto(): Teams.Team {

        val builder = Teams.Team.newBuilder()
            .setLeaderId(this.leaderId.toString())
            .setMode(this.mode.toProto())

        for (memberId in this.members) {
            builder.addMembers(memberId.toString())
        }

        return builder.build()
    }

    fun Teams.Team.toDomain(): Team {

        val members = mutableSetOf<UUID>()

        for (memberId in this.membersList) {
            members.add(UUID.fromString(memberId))
        }

        return Team(
            leaderId = UUID.fromString(this.leaderId),
            mode = this.mode.toDomain(),
            members = members
        )
    }

    fun Error.Code.toProto(): Teams.Error.Code = Teams.Error.Code.valueOf(this.name)

    fun Teams.Error.Code.toDomain(): Error.Code = Error.Code.valueOf(this.name)

    fun Error.toProto(): Teams.Error {

        val builder = Teams.Error.newBuilder()
            .setCode(this.code.toProto())

        if (this.message != null) {
            builder.setMessage(message)
        }

        return builder.build()
    }

    fun Teams.Error.toDomain(): Error {
        return Error(
            code = this.code.toDomain(),
            message = if (!this.message.isNullOrBlank()) this.message else null
        )
    }

    fun Processed<*>.toProto(): Teams.Processed {

        val builder = Teams.Processed.newBuilder()

        if (this.value is Team) {
            builder.team = this.value.toProto()
        }

        return builder.build()
    }

    fun Teams.Processed.toDomain(): Processed<*> {
        return when (this.valueCase) {
            Teams.Processed.ValueCase.TEAM  -> Processed(this.team.toDomain())
            else                            -> Processed(Unit)
        }
    }

    fun Response<*>.toProto(): Teams.Response {

        val builder = Teams.Response.newBuilder()

        when (this) {
            is Processed    -> builder.processed = this.toProto()
            is Error        -> builder.error = this.toProto()
        }

        return builder.build()
    }

    fun Teams.Response.toDomain(): Response<*> {
        return when(this.resCase) {
            Teams.Response.ResCase.PROCESSED    -> this.processed.toDomain()
            Teams.Response.ResCase.ERROR        -> this.error.toDomain()
            else                                -> throw Exception("invalid case")
        }
    }

    fun CreateEntryProcessor.toProto(): Teams.CreateEntryProcessor {
        return Teams.CreateEntryProcessor.newBuilder()
            .setLeaderId(this.leaderId.toString())
            .setMode(this.mode.toProto())
            .build()
    }

    fun Teams.CreateEntryProcessor.toDomain(): CreateEntryProcessor {
        return CreateEntryProcessor(
            leaderId = UUID.fromString(leaderId),
            mode = mode.toDomain()
        )
    }

    fun DisbandEntryProcessor.toProto(): Teams.DisbandEntryProcessor {
        return Teams.DisbandEntryProcessor.newBuilder()
            .setSenderId(this.senderId.toString())
            .build()
    }

    fun Teams.DisbandEntryProcessor.toDomain(): DisbandEntryProcessor {
        return DisbandEntryProcessor(UUID.fromString(this.senderId))
    }

    fun AddMemberEntryProcessor.toProto(): Teams.AddMemberEntryProcessor {
        return Teams.AddMemberEntryProcessor.newBuilder()
            .setSenderId(this.senderId.toString())
            .setMemberId(this.memberId.toString())
            .build()
    }

    fun Teams.AddMemberEntryProcessor.toDomain(): AddMemberEntryProcessor {
        return AddMemberEntryProcessor(
            senderId = UUID.fromString(this.senderId),
            memberId = UUID.fromString(this.memberId)
        )
    }

    fun KickMemberEntryProcessor.toProto(): Teams.KickMemberEntryProcessor {
        return Teams.KickMemberEntryProcessor.newBuilder()
            .setSenderId(this.senderId.toString())
            .setMemberId(this.memberId.toString())
            .build()
    }

    fun Teams.KickMemberEntryProcessor.toDomain(): KickMemberEntryProcessor {
        return KickMemberEntryProcessor(
            senderId = UUID.fromString(this.senderId),
            memberId = UUID.fromString(this.memberId)
        )
    }

    fun LeaveEntryProcessor.toProto(): Teams.LeaveEntryProcessor {
        return Teams.LeaveEntryProcessor.newBuilder()
            .setMemberId(this.memberId.toString())
            .build()
    }

    fun Teams.LeaveEntryProcessor.toDomain(): LeaveEntryProcessor {
        return LeaveEntryProcessor(UUID.fromString(this.memberId))
    }

    fun ChangeModeEntryProcessor.toProto(): Teams.ChangeModeEntryProcessor {
        return Teams.ChangeModeEntryProcessor.newBuilder()
            .setSenderId(this.senderId.toString())
            .setMode(this.mode.toProto())
            .build()
    }

    fun Teams.ChangeModeEntryProcessor.toDomain(): ChangeModeEntryProcessor {
        return ChangeModeEntryProcessor(
            senderId = UUID.fromString(this.senderId),
            mode = this.mode.toDomain()
        )
    }

    fun ChangeLeaderEntryProcessor.toProto(): Teams.ChangeLeaderEntryProcessor {
        return Teams.ChangeLeaderEntryProcessor.newBuilder()
            .setSenderId(this.senderId.toString())
            .setMemberId(this.memberId.toString())
            .build()
    }

    fun Teams.ChangeLeaderEntryProcessor.toDomain(): ChangeLeaderEntryProcessor {
        return ChangeLeaderEntryProcessor(
            senderId = UUID.fromString(this.senderId),
            memberId = UUID.fromString(this.memberId)
        )
    }
}