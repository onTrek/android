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
    val file: File
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
