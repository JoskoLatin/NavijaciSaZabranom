package com.navijacisazabranom.app.data.hns

import org.junit.Assert.assertEquals
import org.junit.Test

class TekstNormalizacijaTest {

    @Test
    fun `dijakritici se uklanjaju a slova spustaju`() {
        assertEquals("sibenik", normalizirajZaPretragu("Šibenik"))
        assertEquals("cacic", normalizirajZaPretragu("Čačić"))
        assertEquals("zupanja", normalizirajZaPretragu("Županja"))
    }

    @Test
    fun `dj se mapira rucno jer nema nfd dekompoziciju`() {
        assertEquals("dakovo", normalizirajZaPretragu("Đakovo"))
        assertEquals("medimurje", normalizirajZaPretragu("Međimurje"))
    }

    @Test
    fun `obicna imena ostaju netaknuta`() {
        assertEquals("hnk hajduk", normalizirajZaPretragu("HNK Hajduk"))
    }
}
