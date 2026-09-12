package com.navijacisazabranom.app.data.hns

data class NatjecanjeStranica(
    val klubovi: List<Klub>,
    val utakmice: List<Utakmica>,
    /** Naziv runde po broju kola iz utakmice (u kupu npr. 10003 → "1/16 finala"). */
    val runde: Map<Int, String> = emptyMap(),
)

interface NatjecanjeRepository {
    suspend fun getNatjecanjeStranica(natjecanjeId: String): Result<NatjecanjeStranica>
}
