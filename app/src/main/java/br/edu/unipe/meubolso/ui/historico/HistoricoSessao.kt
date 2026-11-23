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
class HistoricoSessao(private val persistente: Boolean = false) {
    private val app get() = br.edu.unipe.meubolso.dados.MeuBolso.app
    private val usarBanco get() = persistente && app.perfil.isNotEmpty()
    private val dados = linkedMapOf<String, RascunhoMovimentacao>()
    val itens get() = if (usarBanco) app.dao.movimentos(app.perfil).map { Lancamento(it.id, it.formulario()) } else dados.map { Lancamento(it.key, it.value) }
    fun buscar(id: String) = itens.find { it.id == id }?.dados
    fun adicionar(item: RascunhoMovimentacao): String {
        val id = UUID.randomUUID().toString()
        if (usarBanco) app.dao.inserir(br.edu.unipe.meubolso.dados.Movimento.de(id, app.perfil, item)) else dados[id] = item
        return id
    }
    fun editar(id: String, item: RascunhoMovimentacao) {
        if (usarBanco && buscar(id) != null) app.dao.atualizar(br.edu.unipe.meubolso.dados.Movimento.de(id, app.perfil, item)) else if (id in dados) dados[id] = item
    }
    fun excluir(id: String) { if (usarBanco) app.dao.excluir(id, app.perfil) else dados.remove(id) }

    companion object { val atual = HistoricoSessao(true) }
}
