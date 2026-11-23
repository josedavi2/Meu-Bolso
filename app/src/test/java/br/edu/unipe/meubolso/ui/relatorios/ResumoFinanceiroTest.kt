package br.edu.unipe.meubolso.ui.relatorios

import br.edu.unipe.meubolso.ui.movimentacoes.*
import java.time.LocalDate
import java.math.BigInteger
import org.junit.Assert.*
import org.junit.Test

class ResumoFinanceiroTest {
    private val gasto = RascunhoMovimentacao("Feira", 2500, TipoMovimentacao.DESPESA,
        Categoria.ALIMENTACAO, LocalDate.of(2026, 11, 22))
    private val bolsa = gasto.copy(tipo = TipoMovimentacao.RECEITA, centavos = 10000)

    @Test fun mensalRespeitaMesEAno() {
        val resumo = ResumoFinanceiro.calcular(listOf(gasto, bolsa, gasto.copy(data = gasto.data.minusMonths(1)),
            bolsa.copy(data = bolsa.data.minusYears(1))), 2026, 11)
        assertEquals(2, resumo.quantidade)
        assertEquals(7500.toBigInteger(), resumo.totais.saldo)
        assertEquals(mapOf(Categoria.ALIMENTACAO to 2500.toBigInteger()), resumo.despesasPorCategoria)
    }
    @Test fun anualComparaDozeMesesEIncluiEmpate() {
        val outra = gasto.copy(categoria = Categoria.LAZER, data = gasto.data.withMonth(12))
        val resumo = ResumoFinanceiro.calcular(listOf(gasto, outra, bolsa), 2026, null)
        assertEquals(12, resumo.mesesDoAno.size)
        assertEquals(BigInteger.ZERO, resumo.mesesDoAno.first().despesas)
        assertEquals(10000.toBigInteger(), resumo.mesesDoAno[10].receitas)
        assertEquals(2500.toBigInteger(), resumo.mesesDoAno.last().despesas)
        assertEquals(5000.toBigInteger(), resumo.totais.saldo)
        assertEquals(setOf(Categoria.ALIMENTACAO, Categoria.LAZER), resumo.maioresCategorias.toSet())
    }
    @Test fun periodoVazioESemDespesas() {
        assertEquals(0, ResumoFinanceiro.calcular(listOf(bolsa), 2025, null).quantidade)
        assertTrue(ResumoFinanceiro.calcular(listOf(bolsa), 2026, null).maioresCategorias.isEmpty())
    }
    @Test fun somaEmCentavosAcimaDeLong() {
        val grande = gasto.copy(centavos = Long.MAX_VALUE)
        assertEquals(Long.MAX_VALUE.toBigInteger() * 2.toBigInteger(),
            ResumoFinanceiro.calcular(listOf(grande, grande), 2026, null).totais.despesas)
    }
}
