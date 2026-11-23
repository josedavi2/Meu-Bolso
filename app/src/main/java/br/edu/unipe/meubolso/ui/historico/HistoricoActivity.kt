package br.edu.unipe.meubolso.ui.historico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.unipe.meubolso.R
import br.edu.unipe.meubolso.databinding.TelaHistoricoBinding
import br.edu.unipe.meubolso.ui.movimentacoes.*
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class HistoricoActivity : AppCompatActivity() {
    private lateinit var tela: TelaHistoricoBinding
    private val sessao = HistoricoSessao.atual
    private val lista = ListaHistorico(::acoes)
    private var editando: String? = null
    private var anos = emptyList<Int>()
    private val tipos = TipoMovimentacao.values()
    private val categorias = Categoria.values()
    private val formulario = registerForActivityResult(FormularioMovimentacao()) { retorno ->
        val id = editando
        editando = null
        if (retorno != null) {
            if (id == null) sessao.adicionar(retorno) else sessao.editar(id, retorno)
            configurarAnos(anos.getOrNull(tela.ano.selectedItemPosition - 1))
            mostrar()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tela = TelaHistoricoBinding.inflate(layoutInflater)
        setContentView(tela.root)
        editando = savedInstanceState?.getString("editando")
        tela.lista.layoutManager = LinearLayoutManager(this)
        tela.lista.adapter = lista
        tela.tipo.opcoes(listOf("Todos os tipos") + tipos.map { it.rotulo })
        tela.categoria.opcoes(listOf("Todas as categorias") + categorias.map { it.rotulo })
        tela.mes.opcoes(listOf("Todos os meses") + Month.values().map {
            it.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"))
        })
        configurarAnos(savedInstanceState?.getInt("ano"))
        listOf(tela.tipo, tela.categoria, tela.mes, tela.ano).forEach {
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>) = Unit
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) = mostrar()
            }
        }
        tela.nova.setOnClickListener { editando = null; formulario.launch(null) }
        mostrar()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("editando", editando)
        anos.getOrNull(tela.ano.selectedItemPosition - 1)?.let { outState.putInt("ano", it) }
    }

    private fun configurarAnos(selecionado: Int?) {
        anos = sessao.itens.map { it.dados.data.year }.distinct().sortedDescending()
        tela.ano.opcoes(listOf("Todos os anos") + anos.map { it.toString() })
        tela.ano.setSelection(anos.indexOf(selecionado) + 1)
    }

    private fun Spinner.opcoes(itens: List<String>) {
        adapter = ArrayAdapter(this@HistoricoActivity, android.R.layout.simple_spinner_dropdown_item, itens)
    }

    private fun mostrar() {
        val filtros = Filtros(tipos.getOrNull(tela.tipo.selectedItemPosition - 1),
            categorias.getOrNull(tela.categoria.selectedItemPosition - 1),
            tela.mes.selectedItemPosition.takeIf { it > 0 }, anos.getOrNull(tela.ano.selectedItemPosition - 1))
        val itens = filtros.aplicar(sessao.itens)
        lista.submitList(itens)
        tela.vazio.visibility = if (itens.isEmpty()) View.VISIBLE else View.GONE
        tela.vazio.text = if (sessao.itens.isEmpty()) "Nenhuma movimentação cadastrada."
            else "Nenhuma movimentação corresponde aos filtros."
    }

    private fun acoes(item: Lancamento) {
        AlertDialog.Builder(this).setTitle(item.dados.descricao)
            .setItems(arrayOf("Editar", "Excluir")) { _, acao ->
                if (acao == 0) {
                    editando = item.id
                    formulario.launch(item.dados)
                } else {
                    AlertDialog.Builder(this).setTitle("Excluir movimentação?")
                        .setMessage(item.dados.descricao).setNegativeButton("Cancelar", null)
                        .setPositiveButton("Excluir") { _, _ ->
                            val ano = anos.getOrNull(tela.ano.selectedItemPosition - 1)
                            sessao.excluir(item.id)
                            configurarAnos(ano)
                            mostrar()
                        }.show()
                }
            }.show()
    }
}

private class ListaHistorico(private val clicar: (Lancamento) -> Unit) :
    androidx.recyclerview.widget.ListAdapter<Lancamento, ListaHistorico.Linha>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Lancamento>() {
            override fun areItemsTheSame(a: Lancamento, b: Lancamento) = a.id == b.id
            override fun areContentsTheSame(a: Lancamento, b: Lancamento) = a == b
        }) {
    class Linha(val texto: TextView) : RecyclerView.ViewHolder(texto)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Linha =
        Linha(LayoutInflater.from(parent.context).inflate(R.layout.linha_lancamento, parent, false) as TextView)

    override fun onBindViewHolder(holder: Linha, position: Int) {
        val item = getItem(position)
        val d = item.dados
        val sinal = if (d.tipo == TipoMovimentacao.RECEITA) "↗" else "↘"
        val data = d.data.format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))
        holder.texto.text = "$sinal ${d.descricao} · ${ValorEmReais.paraExibir(d.centavos)}\n${d.tipo.rotulo} · ${d.categoria.rotulo} · $data"
        val icone = when (d.categoria) {
            Categoria.ALIMENTACAO -> R.drawable.ic_categoria_alimentacao
            Categoria.TRANSPORTE -> R.drawable.ic_categoria_transporte
            Categoria.LAZER -> R.drawable.ic_categoria_lazer
            Categoria.ESTUDOS -> R.drawable.ic_categoria_estudos
            Categoria.OUTROS -> R.drawable.ic_categoria_outros
        }
        holder.texto.setCompoundDrawablesRelativeWithIntrinsicBounds(icone, 0, 0, 0)
        holder.texto.setOnClickListener { clicar(item) }
    }
}
