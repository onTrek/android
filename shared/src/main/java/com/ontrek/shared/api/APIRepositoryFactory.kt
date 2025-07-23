package com.ontrek.shared.api

import com.ontrek.shared.api.friends.FriendRepository

class APIRepositoryFactory(apiClient: ApiClient) {
    val friendRepository = FriendRepository(apiClient.getApiService())
}