package br.edu.unipe.meubolso.ui.historico

import br.edu.unipe.meubolso.ui.movimentacoes.*
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class HistoricoTest {
    private val feira = RascunhoMovimentacao("Feira", 2590, TipoMovimentacao.DESPESA,
        Categoria.ALIMENTACAO, LocalDate.of(2026, 11, 22))

    @Test fun filtrosCombinadosEOrdem() {
        val sessao = HistoricoSessao()
        val esperado = sessao.adicionar(feira)
        sessao.adicionar(feira.copy(tipo = TipoMovimentacao.RECEITA))
        sessao.adicionar(feira.copy(categoria = Categoria.LAZER))
        sessao.adicionar(feira.copy(data = feira.data.minusYears(1)))
        sessao.adicionar(feira.copy(data = feira.data.plusMonths(1)))
        val filtro = Filtros(feira.tipo, feira.categoria, 11, 2026)
        assertEquals(esperado, filtro.aplicar(sessao.itens).single().id)
        assertTrue(filtro.copy(ano = 1900).aplicar(sessao.itens).isEmpty())
        val todos = Filtros(null, null, null, null).aplicar(sessao.itens)
        assertEquals(5, todos.size)
        assertEquals(12, todos.first().dados.data.monthValue)
    }

    @Test fun duplicatasPodemSerEditadasEExcluidasSeparadamente() {
        val sessao = HistoricoSessao()
        val a = sessao.adicionar(feira)
        val b = sessao.adicionar(feira)
        sessao.editar(a, feira.copy(centavos = 3000))
        assertEquals(3000L, sessao.buscar(a)?.centavos)
        assertEquals(feira, sessao.buscar(b))
        sessao.excluir(a)
        assertEquals(b, sessao.itens.single().id)
    }
}
