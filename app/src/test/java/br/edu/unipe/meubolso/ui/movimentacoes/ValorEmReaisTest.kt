package br.edu.unipe.meubolso.ui.movimentacoes

import org.junit.Assert.*
import org.junit.Test

class ValorEmReaisTest {
    @Test fun leMoedaBrasileira() {
        assertEquals(2590L, ValorEmReais.lerCentavos("25,90"))
        assertEquals(123456L, ValorEmReais.lerCentavos("1.234,56"))
        assertEquals(120L, ValorEmReais.lerCentavos(" 1,2 "))
        assertEquals(1L, ValorEmReais.lerCentavos("0,01"))
    }

    @Test fun rejeitaZeroAgrupamentoInvalidoEExcessoDePrecisao() {
        listOf("", "0", "0,00", "-1", "1.23", "1,234", "1.00,90", "abc",
            "92233720368547758,08").forEach { assertNull(it, ValorEmReais.lerCentavos(it)) }
    }

    @Test fun editarNaoAlteraCentavos() {
        for (valor in listOf(1L, 2590L, 100000L, Long.MAX_VALUE)) {
            assertEquals(valor, ValorEmReais.lerCentavos(ValorEmReais.paraCampo(valor)))
        }
    }
}
