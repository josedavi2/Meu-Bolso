package br.edu.unipe.meubolso.dados

import android.app.NotificationManager
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import br.edu.unipe.meubolso.notificacoes.Lembretes
import br.edu.unipe.meubolso.ui.historico.HistoricoSessao
import br.edu.unipe.meubolso.ui.relatorios.ResumoFinanceiro
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.time.LocalDate
import java.time.YearMonth

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class IntegracaoDuplaTest {
    private val app get() = RuntimeEnvironment.getApplication() as MeuBolso
    private val mes = YearMonth.of(2027, 2)
    private fun conta(id: String, perfil: String, dia: Int = 31) =
        ContaFixa(id, perfil, "Internet", 9000, "OUTROS", dia, mes.toString())

    @Test fun parcelaGeraUmaDespesaNoMesDoPagamento() {
        app.perfil = "ana"
        val c = conta("internet", app.perfil)
        val dao = app.dao
        dao.salvarConta(c, mes, true)
        dao.preparar(app.perfil, mes)
        val parcela = dao.parcelas(c.id).single()
        assertEquals("2027-02-28", parcela.vencimento)
        assertEquals("Vencido", parcela.status(mes.plusMonths(1).atDay(1)))
        val dia = mes.plusMonths(1).atDay(2)
        repeat(2) { dao.pagar(c, mes.toString(), dia) }
        val itens = HistoricoSessao.atual.itens
        assertEquals(1, itens.size)
        val dados = itens.map { it.dados }
        assertEquals(0, ResumoFinanceiro.calcular(dados, 2027, 2).quantidade)
        assertEquals(9000.toBigInteger(), ResumoFinanceiro.calcular(dados, 2027, 3).totais.despesas)
        app.perfil = "bia"
        assertTrue(HistoricoSessao.atual.itens.isEmpty())
        dao.excluir(itens.single().id, app.perfil)
        assertEquals("Pago", dao.parcelas(c.id).single().status(dia))
        app.perfil = "ana"
        dao.excluir(itens.single().id, app.perfil)
        assertNull(dao.parcelas(c.id).single().movimento)
        assertTrue(HistoricoSessao.atual.itens.isEmpty())
    }

    @Test fun avisaUmaVezSomenteNoVencimentoDoPerfilAtivo() {
        val hoje = mes.atEndOfMonth()
        app.perfil = "ana"
        val dao = app.dao
        listOf(conta("vence", "ana"), conta("ontem", "ana", 27), conta("outra", "bia"), conta("paga", "ana"))
            .forEach { dao.salvarConta(it, mes, true) }
        dao.pagar(conta("paga", "ana"), mes.toString(), hoje)
        val gestor = app.getSystemService(NotificationManager::class.java)
        Lembretes.verificar(app, hoje)
        assertEquals("vence/$mes", gestor.activeNotifications.single().tag)
        assertEquals(hoje.toString(), dao.parcelas("vence").single().ultimoAviso)
        assertNull(dao.parcelas("outra").single().ultimoAviso)
        gestor.cancelAll()
        Lembretes.verificar(app, hoje)
        assertTrue(gestor.activeNotifications.isEmpty())
    }

    @Test fun migrarDoBancoAnteriorPreservaDadosEPreencheCategorias() {
        val nome = "teste-migracao.db"
        val arquivo = app.getDatabasePath(nome)
        arquivo.parentFile?.mkdirs()
        val esquema = File("schemas/br.edu.unipe.meubolso.dados.Banco/1.json").readText()
        val entidades = JSONObject(esquema).getJSONObject("database").getJSONArray("entities")
        SQLiteDatabase.openOrCreateDatabase(arquivo, null).use { antigo ->
            for (i in 0 until entidades.length()) {
                val e = entidades.getJSONObject(i)
                val tabela = e.getString("tableName")
                fun executar(sql: String) = antigo.execSQL(sql.replace("\${TABLE_NAME}", tabela))
                executar(e.getString("createSql"))
                val indices = e.getJSONArray("indices")
                for (j in 0 until indices.length()) executar(indices.getJSONObject(j).getString("createSql"))
            }
            antigo.execSQL("INSERT INTO Movimento VALUES ('antigo','ana','Bolsa',30000,'ESTUDOS','RECEITA','2027-02-01')")
            antigo.version = 1
        }
        val novo = Room.databaseBuilder(app, Banco::class.java, nome).allowMainThreadQueries()
            .addMigrations(MIGRACAO_1_2).build()
        try {
            assertEquals(30000L, novo.financas().movimentos("ana").single().valor)
            assertEquals(5, novo.financas().categorias().size)
        } finally {
            novo.close()
        }
    }
}
