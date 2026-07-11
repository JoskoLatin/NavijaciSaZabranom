package com.navijacisazabranom.app.data.hns

import org.json.JSONArray

/**
 * getOrganizations/getCompetitions handleri vraćaju plosnat JSON niz
 * objekata s poljima "id" i "value" (+ dodatna polja koja ovdje ne trebamo).
 */
object HnsDirectoryParser {

    fun parseOrganizacije(json: String): List<Organizacija> =
        parseIdValueArray(json).map { (id, naziv) -> Organizacija(id, naziv) }

    fun parseNatjecanja(json: String): List<Natjecanje> =
        parseIdValueArray(json).map { (id, naziv) -> Natjecanje(id, naziv) }

    private fun parseIdValueArray(json: String): List<Pair<String, String>> {
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            obj.get("id").toString() to obj.getString("value")
        }
    }
}
