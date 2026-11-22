package br.edu.unipe.meubolso.ui.historico

import br.edu.unipe.meubolso.ui.movimentacoes.*
import java.util.UUID

data class Lancamento(val id: String, val dados: RascunhoMovimentacao)

data class Filtros(val tipo: TipoMovimentacao?, val categoria: Categoria?, val mes: Int?, val ano: Int?) {
    fun aplicar(itens: List<Lancamento>): List<Lancamento> = itens.filter { (_, dados) ->
        (tipo == null || dados.tipo == tipo) && (categoria == null || dados.categoria == categoria) &&
            (mes == null || dados.data.monthValue == mes) && (ano == null || dados.data.year == ano)
    }.sortedByDescending { it.dados.data }
}

// Armazenamento temporário do protótipo, compartilhado pelo menu e pelo histórico.
class HistoricoSessao {
    private val dados = linkedMapOf<String, RascunhoMovimentacao>()
    val itens get() = dados.map { Lancamento(it.key, it.value) }
    fun buscar(id: String) = dados[id]
    fun adicionar(item: RascunhoMovimentacao): String {
        val id = UUID.randomUUID().toString()
        dados[id] = item
        return id
    }
    fun editar(id: String, item: RascunhoMovimentacao) {
        if (id in dados) dados[id] = item
    }
    fun excluir(id: String) { dados.remove(id) }

    companion object { val atual = HistoricoSessao() }
}
