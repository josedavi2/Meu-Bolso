package br.edu.unipe.meubolso.ui.relatorios

import br.edu.unipe.meubolso.ui.movimentacoes.Categoria
import br.edu.unipe.meubolso.ui.movimentacoes.RascunhoMovimentacao
import br.edu.unipe.meubolso.ui.movimentacoes.TipoMovimentacao
import java.math.BigInteger

data class TotaisPeriodo(val receitas: BigInteger, val despesas: BigInteger) {
    val saldo get() = receitas - despesas
}

data class ResumoFinanceiro(
    val quantidade: Int,
    val totais: TotaisPeriodo,
    val despesasPorCategoria: Map<Categoria, BigInteger>,
    val mesesDoAno: List<TotaisPeriodo>
) {
    val maioresCategorias: List<Categoria>
        get() {
            val maior = despesasPorCategoria.values.maxOrNull()
            return despesasPorCategoria.filterValues { it == maior }.keys.toList()
        }

    companion object {
        fun calcular(dados: List<RascunhoMovimentacao>, ano: Int, mes: Int?): ResumoFinanceiro {
            require(mes == null || mes in 1..12)
            val doAno = dados.filter { it.data.year == ano }
            val periodo = if (mes == null) doAno else doAno.filter { it.data.monthValue == mes }
            val categorias = periodo.filter { it.tipo == TipoMovimentacao.DESPESA }
                .groupBy { it.categoria }.toSortedMap()
                .mapValues { (_, itens) -> itens.sumOf { it.centavos.toBigInteger() } }
            val porMes = doAno.groupBy { it.data.monthValue }
            return ResumoFinanceiro(periodo.size, somar(periodo), categorias,
                (1..12).map { somar(porMes[it].orEmpty()) })
        }

        private fun somar(dados: List<RascunhoMovimentacao>): TotaisPeriodo {
            var receitas = BigInteger.ZERO
            var despesas = BigInteger.ZERO
            for (item in dados) {
                if (item.tipo == TipoMovimentacao.RECEITA) receitas += item.centavos.toBigInteger()
                else despesas += item.centavos.toBigInteger()
            }
            return TotaisPeriodo(receitas, despesas)
        }
    }
}
