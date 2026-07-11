package com.navijacisazabranom.app.data.matches

interface MatchRepository {
    suspend fun getMatchesForClub(klubId: String): Result<List<Utakmica>>
}
