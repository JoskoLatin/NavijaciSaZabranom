package com.navijacisazabranom.app.data.hns

interface DirectoryRepository {
    suspend fun getOrganizacije(): Result<List<Organizacija>>
    suspend fun getNatjecanja(organizacijaId: String): Result<List<Natjecanje>>
}
