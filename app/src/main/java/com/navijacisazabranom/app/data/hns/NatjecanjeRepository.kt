package com.navijacisazabranom.app.data.hns

data class NatjecanjeStranica(
    val klubovi: List<Klub>,
    val utakmice: List<Utakmica>,
)

interface NatjecanjeRepository {
    suspend fun getNatjecanjeStranica(natjecanjeId: String): Result<NatjecanjeStranica>
}
