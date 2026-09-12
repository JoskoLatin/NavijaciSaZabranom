package com.navijacisazabranom.app.data.hns

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Nazivi su stvarni primjeri sa semafora (sezone 2025/26 i 2026/27), osim gdje je navedeno drukčije. */
class SeniorskiFiltarTest {

    @Test
    fun `seniorske lige prolaze`() {
        listOf(
            "SuperSport HNL",
            "SuperSport Prva NL",
            "SuperSport Druga NL",
            "Treća NL Istok 25/26",
            "4. NL SREDIŠTE B 25/26",
            "4. Nogometna liga Središta Zagreb skupina A 25/26",
            "PREMIER NSZŽ 25/26",
            "Jedinstvena 1. NSZŽ",
            "1. ŽNL KZŽ 25/26",
            "2. ŽNL Đakovo 25/26",
            "DRUGA ZAGREBAČKA LIGA - SENIORI 25/26",
            "Elitna ŽNL Varaždin 25/26",
            "Baranjska liga-seniori 25/26",
            "1. liga skupina A",
            "2. liga skupina B",
            "II ŽNL Vukovar seniori 2025/26",
        ).forEach { naziv ->
            assertTrue("Trebala je proći: $naziv", SeniorskiFiltar.jeSeniorskaLiga(naziv))
        }
    }

    @Test
    fun `mladez se filtrira ukljucujuci dijalektalne nazive`() {
        listOf(
            "1. NL juniori",
            "1. NL kadeti",
            "2. NL Središte Istok - Stariji pioniri 25/26",
            "LIGA LIMAČA NS KUTINA 25/26",
            "1. ŽNL NSKŽ Limaći 25/26",
            "1. MŽNL PAPALINE 25/26",
            "2. ŽNL TIĆI 25/26",
            "2. ŽNLNSKŽ Karlići 2 25/26",
            "1. liga početnika - Split 25/26",
            "Prva liga U-9 NSZŽ  25/26",
            "DRUGA NL ISTOK POČETNICI U11 SREDIŠTE SJEVER 25/26",
            "Kadeti 2. liga 25/26",
            "Liga U-12 25/26",
            "Liga U-10 CENTAR 25/26",
            "1. NL U 14 25/26",
            "U 11 Piceki A",
            "U 13 Pijetlovi B",
            "PREDNATJECATELJI U9  25/26",
            "LM NS Vukovar-Prednatjecatelji 2025./2026.",
            "Liga prstića NSZ 25/26",
            "PRST2526 25/26",
            "ŠPIGETE 25/26",
            "WU-13 26/27",
        ).forEach { naziv ->
            assertFalse("Trebala je biti isključena: $naziv", SeniorskiFiltar.jeSeniorskaLiga(naziv))
        }
    }

    @Test
    fun `futsal i veterani se filtriraju`() {
        listOf(
            "SuperSport HMNL",
            "Prva HMNL 25/26",
            "FUTSAL REGIJA ISTOK 25/26",
            "Superkup u malom nogometu 2025",
            "VETERANI-KUTINA 25/26",
            "VET 25/26 25/26",
            "mnkup2526 25/26",
            "ŽMNL_Veterani 25/26",
            "1. ŽMNL 25/26",
        ).forEach { naziv ->
            assertFalse("Trebala je biti isključena: $naziv", SeniorskiFiltar.jeSeniorskaLiga(naziv))
        }
    }

    @Test
    fun `kupovi i kvalifikacije se filtriraju ali skupina ne okida kup`() {
        listOf(
            "KUP ŽNS 25/26",
            "SuperSport HNK",
            "CUP NSKZŽ 25/26",
            "senkup25/26 25/26",
            "Kvalifikacije za popunu SuperSport 2 .NL 25/26",
            "Doigravanje za popunu 1. HNLŽ 25/26",
            "Završnica HNK - JUNIORI 25/26",
        ).forEach { naziv ->
            assertFalse("Trebala je biti isključena: $naziv", SeniorskiFiltar.jeSeniorskaLiga(naziv))
        }

        // "skupina" sadrži "kup" kao podniz — ne smije okinuti filtar
        assertTrue(SeniorskiFiltar.jeSeniorskaLiga("Druga HNLŽ 25/26 skupina \"A\""))
        assertTrue(SeniorskiFiltar.jeSeniorskaLiga("1. liga skupina A"))
    }

    @Test
    fun `seniorski kupovi se prepoznaju`() {
        listOf(
            "SuperSport HNK",
            "KUP NSBBŽ  26/27",
            "KUP ŽNS BPŽ 26/27",
            "KUP NSŽI 26/27",
            "HNK NSKŽ 26/27",
            "kup ŽNS ",
            "CUP NSKZŽ 26/27",
            "KUP MNS 26/27",
            "Kup NSPSŽ 26./27.",
            "Hrvatski nogometni kup 26/27 ",
            "KUP SENIORI 26/27",
            "Kup ŽNS Varaždin 26/27",
            "KUP ŽNS-VS ",
            "KUP NSZŽ 26/27",
            "Kup NS Beli Manastir - seniori 26/27",
            "Kup NS Donji Miholjac 2026./2027.",
            "Kup NSĐ 26/27",
            "KUP KT 26/27",
            "Kup NS Našice 26/27",
            "CUP 26/27",
            "Kup NS Valpovo 26/27",
            "KUP NSZ 26/27",
            "senkup25/26 25/26",
        ).forEach { naziv ->
            assertTrue("Trebao je biti seniorski kup: $naziv", SeniorskiFiltar.jeSeniorskiKup(naziv))
        }
    }

    @Test
    fun `kupovi mladih, malog nogometa i zena nisu seniorski kupovi`() {
        listOf(
            "ZNS-KUP-JUNIORI 26/27",
            "ZNS-KUP-KADETI 26/27",
            "KUP KADETI U-17 NSBBŽ 26/27",
            "KUP PIONIRI stariji U-15 NSBBŽ 26/27",
            "KUP U-19 JUNIORI  26/27",
            "Kup NSŽI juniori 26/27",
            "NSLSŽ-KUP -Pioniri 26/27",
            "JUNIORI - KUP 26/27",
            "KUP MLADEŽI ŽNS-VS  JUNIORI ",
            "Završnica HNK - JUNIORI 25/26",
            "Superkup u malom nogometu 26/27",
            "mnkup2526 25/26",
            "HNKŽ 26/27",
        ).forEach { naziv ->
            assertFalse("Nije trebao biti seniorski kup: $naziv", SeniorskiFiltar.jeSeniorskiKup(naziv))
        }
    }

    @Test
    fun `izmisljeni primjeri - veterani, zene i turniri nisu seniorski kupovi`() {
        listOf(
            "Kup veterana 26/27",
            "Kup žene 26/27",
            "Ženski kup NS 26/27",
            "Prijateljski turnir - kup grada",
        ).forEach { naziv ->
            assertFalse("Nije trebao biti seniorski kup: $naziv", SeniorskiFiltar.jeSeniorskiKup(naziv))
        }
    }

    @Test
    fun `lige i skupine nisu kupovi`() {
        listOf(
            "SuperSport HNL",
            "SuperSport Prva NL",
            "Treća NL Istok 26/27",
            "Druga HNLŽ 26/27 skupina \"A\"",
            "1. liga skupina A",
        ).forEach { naziv ->
            assertFalse("Nije trebao biti kup: $naziv", SeniorskiFiltar.jeSeniorskiKup(naziv))
        }
    }
}
