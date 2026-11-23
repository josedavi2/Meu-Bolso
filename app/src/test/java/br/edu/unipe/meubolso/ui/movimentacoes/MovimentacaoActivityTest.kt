package br.edu.unipe.meubolso.ui.movimentacoes

import android.app.Activity
import android.widget.*
import br.edu.unipe.meubolso.R
import com.google.android.material.textfield.TextInputLayout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MovimentacaoActivityTest {
    private fun abrir(dados: RascunhoMovimentacao? = null): MovimentacaoActivity {
        val intent = FormularioMovimentacao().createIntent(RuntimeEnvironment.getApplication(), dados)
        return Robolectric.buildActivity(MovimentacaoActivity::class.java, intent).setup().get()
    }

    @Test fun recusaCamposVazios() {
        val tela = abrir()
        tela.findViewById<Button>(R.id.confirmar).performClick()
        assertNotNull(tela.findViewById<TextInputLayout>(R.id.campo_descricao).error)
        assertNotNull(tela.findViewById<TextInputLayout>(R.id.campo_valor).error)
        assertFalse(tela.isFinishing)
    }

    @Test fun criaReceitaERetornaParaEdicao() {
        val nova = abrir()
        nova.findViewById<EditText>(R.id.descricao).setText("Bolsa")
        nova.findViewById<EditText>(R.id.valor).setText("1.234,56")
        nova.findViewById<RadioButton>(R.id.receita).performClick()
        nova.findViewById<Spinner>(R.id.categoria).setSelection(Categoria.ESTUDOS.ordinal)
        nova.findViewById<Button>(R.id.confirmar).performClick()
        val retorno = shadowOf(nova)
        val dados = FormularioMovimentacao().parseResult(retorno.resultCode, retorno.resultIntent)!!
        assertEquals(123456L, dados.centavos)
        assertEquals("Bolsa", dados.descricao)
        assertEquals(TipoMovimentacao.RECEITA, dados.tipo)
        assertEquals(Categoria.ESTUDOS, dados.categoria)

        val anterior = dados.copy(data = LocalDate.of(2026, 10, 12))
        val edicao = abrir(anterior)
        assertEquals("12/10/2026", edicao.findViewById<Button>(R.id.data).text.toString())
        assertTrue(edicao.findViewById<RadioButton>(R.id.receita).isChecked)
        assertEquals("1234,56", edicao.findViewById<EditText>(R.id.valor).text.toString())
        edicao.findViewById<EditText>(R.id.valor).setText("900,00")
        edicao.findViewById<Button>(R.id.confirmar).performClick()
        val alterada = shadowOf(edicao)
        assertEquals(anterior.copy(centavos = 90000), FormularioMovimentacao()
            .parseResult(alterada.resultCode, alterada.resultIntent))
    }

    @Test fun cancelarDescartaEdicao() {
        val dados = RascunhoMovimentacao("Ônibus", 500, TipoMovimentacao.DESPESA,
            Categoria.TRANSPORTE, LocalDate.now())
        val edicao = abrir(dados)
        edicao.findViewById<EditText>(R.id.descricao).setText("Alterada")
        edicao.findViewById<Button>(R.id.cancelar).performClick()
        val resultado = shadowOf(edicao)
        assertTrue(edicao.isFinishing)
        assertEquals(Activity.RESULT_CANCELED, resultado.resultCode)
        assertNull(FormularioMovimentacao().parseResult(resultado.resultCode, resultado.resultIntent))
    }
}
