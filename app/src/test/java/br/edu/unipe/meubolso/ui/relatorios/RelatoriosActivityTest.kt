package br.edu.unipe.meubolso.ui.relatorios

import android.view.View
import android.widget.Spinner
import android.widget.TextView
import android.os.Looper
import br.edu.unipe.meubolso.R
import br.edu.unipe.meubolso.ui.historico.HistoricoSessao
import br.edu.unipe.meubolso.ui.movimentacoes.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RelatoriosActivityTest {
    @Test fun graficosLeemOHistoricoEAtualizamAposExcluir() {
        val sessao = HistoricoSessao.atual
        val id = sessao.adicionar(RascunhoMovimentacao("Mercado", 25000, TipoMovimentacao.DESPESA,
            Categoria.ALIMENTACAO, LocalDate.now()))
        try {
            val controller = Robolectric.buildActivity(RelatoriosActivity::class.java).setup()
            val tela = controller.get()
            val pizza = tela.findViewById<PieChart>(R.id.pizza)
            assertEquals(1, pizza.data.entryCount)
            assertTrue(tela.findViewById<TextView>(R.id.maior).text.contains("Alimentação"))
            tela.findViewById<Spinner>(R.id.periodo).setSelection(0)
            controller.pause().resume()
            shadowOf(Looper.getMainLooper()).idle()
            val barras = tela.findViewById<BarChart>(R.id.barras)
            assertEquals(View.VISIBLE, tela.findViewById<View>(R.id.anual).visibility)
            assertEquals(24, barras.data.entryCount)
            sessao.excluir(id)
            controller.pause().resume()
            assertEquals(View.VISIBLE, tela.findViewById<View>(R.id.vazio).visibility)
            assertEquals(View.GONE, pizza.visibility)
            controller.pause().stop().destroy()
        } finally {
            sessao.excluir(id)
        }
    }
}
