package br.edu.unipe.meubolso.ui.relatorios

import android.graphics.Color
import br.edu.unipe.meubolso.ui.comum.configurarAbas
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import br.edu.unipe.meubolso.databinding.TelaRelatoriosBinding
import br.edu.unipe.meubolso.ui.historico.HistoricoSessao
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.math.BigInteger
import java.text.NumberFormat
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

class RelatoriosActivity : AppCompatActivity() {
    private lateinit var tela: TelaRelatoriosBinding
    private val brasil = Locale.forLanguageTag("pt-BR")
    private val moeda = NumberFormat.getCurrencyInstance(brasil)
    private val meses = Month.values().map { it.getDisplayName(TextStyle.FULL, brasil) }
    private lateinit var anos: List<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tela = TelaRelatoriosBinding.inflate(layoutInflater)
        setContentView(tela.root)
        configurarAbas(br.edu.unipe.meubolso.R.id.aba_relatorios)
        val tinta = getColor(br.edu.unipe.meubolso.R.color.mb_ink)
        tela.pizza.legend.textColor = tinta
        tela.pizza.setHoleColor(getColor(br.edu.unipe.meubolso.R.color.mb_paper))
        tela.barras.legend.textColor = tinta
        tela.barras.xAxis.textColor = tinta
        tela.barras.axisLeft.textColor = tinta
        val hoje = LocalDate.now()
        anos = (HistoricoSessao.atual.itens.map { it.dados.data.year } + hoje.year).distinct().sortedDescending()
        tela.ano.opcoes(anos.map(Int::toString))
        tela.periodo.opcoes(listOf("Ano inteiro") + meses)
        tela.ano.setSelection(anos.indexOf(hoje.year))
        tela.periodo.setSelection(hoje.monthValue)
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) = Unit
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) = atualizar()
        }
        tela.periodo.onItemSelectedListener = listener
        tela.ano.onItemSelectedListener = listener
    }

    override fun onResume() {
        super.onResume()
        atualizar()
    }

    private fun Spinner.opcoes(itens: List<String>) {
        adapter = ArrayAdapter(this@RelatoriosActivity, android.R.layout.simple_spinner_dropdown_item, itens)
    }

    private fun reais(valor: BigInteger) = moeda.format(valor.toBigDecimal(2))

    private fun atualizar() {
        val ano = anos.getOrNull(tela.ano.selectedItemPosition) ?: return
        val mes = tela.periodo.selectedItemPosition.takeIf { it > 0 }
        val resumo = ResumoFinanceiro.calcular(HistoricoSessao.atual.itens.map { it.dados }, ano, mes)
        tela.resumo.text = with(resumo.totais) {
            "Receitas   ${reais(receitas)}\nDespesas   ${reais(despesas)}\nSaldo   ${reais(saldo)}"
        }
        tela.vazio.isVisible = resumo.quantidade == 0
        mostrarCategorias(resumo)
        tela.anual.isVisible = mes == null
        if (mes == null) mostrarMeses(resumo)
    }

    private fun mostrarCategorias(resumo: ResumoFinanceiro) {
        val categorias = resumo.despesasPorCategoria
        tela.pizza.isVisible = categorias.isNotEmpty()
        tela.detalhesCategoria.isVisible = categorias.isNotEmpty()
        if (categorias.isEmpty()) {
            tela.maior.text = "Nenhuma despesa no período."
            tela.pizza.clear()
            return
        }
        val lideres = resumo.maioresCategorias
        val nomes = lideres.joinToString(" e ") { it.rotulo }
        tela.maior.text = "Maior gasto: $nomes · ${reais(categorias.getValue(lideres.first()))}"
        tela.detalhesCategoria.text = categorias.entries.joinToString("\n") { (c, valor) ->
            "${c.rotulo}   ${reais(valor)}"
        }
        val fatias = categorias.map { (c, valor) -> PieEntry(valor.toFloat(), c.rotulo) }
        val serie = PieDataSet(fatias, "").apply {
            colors = categorias.keys.map { CORES[it.ordinal] }
            setDrawValues(false)
        }
        tela.pizza.apply {
            data = PieData(serie)
            setDrawEntryLabels(false)
            description.isEnabled = false
            legend.isWordWrapEnabled = true
            setTouchEnabled(false)
            invalidate()
        }
    }

    private fun mostrarMeses(resumo: ResumoFinanceiro) {
        val lista = resumo.mesesDoAno
        tela.barras.isVisible = resumo.quantidade > 0
        tela.detalhesMes.text = if (resumo.quantidade == 0) "Nenhum lançamento neste ano." else
            lista.mapIndexed { i, t ->
                "${meses[i]} · Receitas ${reais(t.receitas)} · Despesas ${reais(t.despesas)}"
            }.joinToString("\n")
        if (resumo.quantidade == 0) {
            tela.barras.clear()
            return
        }
        fun serie(nome: String, cor: Int, deslocamento: Float, valor: (TotaisPeriodo) -> BigInteger): BarDataSet {
            val entradas = lista.mapIndexed { i, t -> BarEntry(i + deslocamento, valor(t).toBigDecimal(2).toFloat()) }
            return BarDataSet(entradas, nome).apply { color = cor; setDrawValues(false) }
        }
        val entradas = serie("Receitas", Color.rgb(36, 107, 69), -0.19f) { it.receitas }
        val saidas = serie("Despesas", Color.rgb(158, 66, 58), 0.19f) { it.despesas }
        tela.barras.apply {
            data = BarData(entradas, saidas).apply { barWidth = 0.34f }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                axisMinimum = -0.5f
                axisMaximum = 11.5f
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(meses.map { it.take(3) })
                setDrawGridLines(false)
            }
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            description.isEnabled = false
            setTouchEnabled(false)
            invalidate()
        }
    }

    companion object {
        private val CORES = listOf("#377A62", "#477AA5", "#AC7040", "#8B588C", "#686F48").map(Color::parseColor)
    }
}
