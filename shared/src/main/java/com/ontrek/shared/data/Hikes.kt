package com.ontrek.shared.data

data class File(
    val filename: String,
    val file_id: Int
)

data class FileID(
    val file_id: Int
)

data class GroupDoc(
    val created_at: String,
    val created_by: String,
    val description: String,
    val group_id: Int,
    val file: File,
    val members_number: Int,
)

data class GroupID(
    val group_id: Int
)

data class GroupIDCreation(
    val file_id: Int? = null,
    val description: String
)

data class GroupMember(
    val color: String,
    val id: String,
    val username: String,
)

data class GroupInfoResponseDoc(
    val created_at: String,
    val description: String,
    val members: List<GroupMember>,
    val created_by: UserMinimal
)

data class MemberInfo(
    val user: UserMinimal,
    val accuracy: Double,
    val altitude: Double,
    val going_to: String,
    val help_request: Boolean = false,
    val latitude: Double,
    val longitude: Double,
    val time_stamp: String
)

data class MemberInfoUpdate(
    val accuracy: Double,
    val altitude: Double,
    val going_to: String,
    val help_request: Boolean,
    val latitude: Double,
    val longitude: Double
)