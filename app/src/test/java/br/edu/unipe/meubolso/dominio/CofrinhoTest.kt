package br.edu.unipe.meubolso.dominio

import org.junit.Assert.assertEquals
import org.junit.Test

class CofrinhoTest {
    @Test fun faixasRespeitamOsLimitesDoPlano() {
        val receitas = 1000.toBigInteger()
        for ((saldo, faixa) in listOf(501 to Cofrinho.CHEIO, 500 to Cofrinho.MEDIO, 250 to Cofrinho.MEDIO,
            200 to Cofrinho.BAIXO, 1 to Cofrinho.BAIXO, 0 to Cofrinho.VAZIO, -1 to Cofrinho.VAZIO)) {
            assertEquals(faixa, Cofrinho.avaliar(receitas, saldo.toBigInteger()))
        }
        assertEquals(Cofrinho.SEM_RECEITA, Cofrinho.avaliar(0.toBigInteger(), (-100).toBigInteger()))
    }
}
