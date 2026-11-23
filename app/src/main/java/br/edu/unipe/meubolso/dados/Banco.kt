package br.edu.unipe.meubolso.dados

import android.app.Application
import androidx.room.*
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import br.edu.unipe.meubolso.ui.movimentacoes.*

@Entity(indices = [Index(value = ["email"], unique = true)])
data class Usuario(@PrimaryKey val id: String, val nome: String, val email: String, val salt: ByteArray, val hash: ByteArray)

@Entity
data class Movimento(@PrimaryKey val id: String, val perfil: String, val descricao: String, val valor: Long,
    val categoria: String, val tipo: String, val data: String) {
    fun formulario() = RascunhoMovimentacao(descricao, valor, TipoMovimentacao.valueOf(tipo), Categoria.valueOf(categoria), LocalDate.parse(data))
    companion object {
        fun de(id: String, perfil: String, dados: RascunhoMovimentacao) = Movimento(id, perfil, dados.descricao,
            dados.centavos, dados.categoria.name, dados.tipo.name, dados.data.toString())
    }
}

@Entity
data class ContaFixa(@PrimaryKey val id: String, val perfil: String, val descricao: String, val valor: Long,
    val categoria: String, val dia: Int, val inicio: String)

@Entity(primaryKeys = ["conta", "mes"], foreignKeys = [ForeignKey(entity = Movimento::class,
    parentColumns = ["id"], childColumns = ["movimento"], onDelete = ForeignKey.SET_NULL)],
    indices = [Index(value = ["movimento"], unique = true)])
data class Parcela(val conta: String, val mes: String, val vencimento: String, val valor: Long,
    val categoria: String, val movimento: String? = null, val ultimoAviso: String? = null) {
    fun status(hoje: LocalDate) = when {
        movimento != null -> "Pago"
        LocalDate.parse(vencimento) < hoje -> "Vencido"
        else -> "Pendente"
    }
}

@Dao
abstract class Financas {
    @Query("SELECT * FROM CategoriaLocal ORDER BY ordem") abstract fun categorias(): List<CategoriaLocal>

    @Query("""SELECT p.* FROM Parcela p JOIN ContaFixa c ON c.id=p.conta
        WHERE c.perfil=:perfil AND p.vencimento=:hoje AND p.movimento IS NULL
        AND (p.ultimoAviso IS NULL OR p.ultimoAviso != :hoje)""")
    abstract fun aAvisar(perfil: String, hoje: String): List<Parcela>

    @Query("SELECT EXISTS(SELECT 1 FROM Parcela JOIN Movimento ON Movimento.id=Parcela.movimento WHERE Movimento.perfil=:perfil AND Movimento.id=:id)")
    abstract fun pagamentoVinculado(perfil: String, id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE) abstract fun cadastrar(usuario: Usuario): Long
    @Query("SELECT * FROM Usuario WHERE email = :email") abstract fun usuario(email: String): Usuario?
    @Insert abstract fun inserir(movimento: Movimento)
    @Update abstract fun atualizar(movimento: Movimento)
    @Query("SELECT * FROM Movimento WHERE perfil = :perfil") abstract fun movimentos(perfil: String): List<Movimento>
    @Query("DELETE FROM Movimento WHERE id = :id AND perfil = :perfil") abstract fun excluir(id: String, perfil: String)
    @Insert abstract fun criar(conta: ContaFixa)
    @Update abstract fun editar(conta: ContaFixa)
    @Query("SELECT * FROM ContaFixa WHERE perfil = :perfil ORDER BY dia, descricao") abstract fun contas(perfil: String): List<ContaFixa>
    @Insert(onConflict = OnConflictStrategy.IGNORE) abstract fun gerar(parcela: Parcela)
    @Update abstract fun atualizar(parcela: Parcela)
    @Query("SELECT * FROM Parcela WHERE conta = :id ORDER BY mes DESC") abstract fun parcelas(id: String): List<Parcela>
    @Transaction open fun preparar(perfil: String, mes: YearMonth) {
        for (conta in contas(perfil).filter { it.inicio <= mes.toString() }) {
            gerar(Parcela(conta.id, mes.toString(), mes.atDay(minOf(conta.dia, mes.lengthOfMonth())).toString(), conta.valor, conta.categoria))
        }
    }
    @Transaction open fun salvarConta(conta: ContaFixa, mes: YearMonth, nova: Boolean) {
        if (nova) criar(conta) else editar(conta)
        preparar(conta.perfil, mes)
        for (p in parcelas(conta.id).filter { it.mes >= mes.toString() && it.movimento == null }) {
            val periodo = YearMonth.parse(p.mes)
            atualizar(p.copy(valor = conta.valor, categoria = conta.categoria,
                vencimento = periodo.atDay(minOf(conta.dia, periodo.lengthOfMonth())).toString()))
        }
    }

    @Transaction open fun pagar(conta: ContaFixa, mes: String, data: LocalDate) {
        val parcela = parcelas(conta.id).first { it.mes == mes }
        if (parcela.movimento != null) return
        val id = UUID.randomUUID().toString()
        inserir(Movimento(id, conta.perfil, conta.descricao, parcela.valor, parcela.categoria, "DESPESA", data.toString()))
        atualizar(parcela.copy(movimento = id))
    }
}

@Database(entities = [Usuario::class, Movimento::class, ContaFixa::class, Parcela::class, CategoriaLocal::class], version = 2)
abstract class Banco : RoomDatabase() { abstract fun financas(): Financas }

class MeuBolso : Application() {
    override fun onCreate() {
        super.onCreate()
        val modo = getSharedPreferences("aparencia", MODE_PRIVATE).getInt("modo",
            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(modo)
        app = this
    }
    val banco by lazy {
        Room.databaseBuilder(this, Banco::class.java, "financas.db")
            .addMigrations(MIGRACAO_1_2).addCallback(CRIAR_CATALOGO)
            .allowMainThreadQueries().build()
    }
    val dao get() = banco.financas()
    private val sessao by lazy { getSharedPreferences("sessao", MODE_PRIVATE) }
    var perfil: String
        get() = sessao.getString("perfil", "").orEmpty()
        set(valor) { sessao.edit().putString("perfil", valor).apply() }
    companion object { lateinit var app: MeuBolso }
}
