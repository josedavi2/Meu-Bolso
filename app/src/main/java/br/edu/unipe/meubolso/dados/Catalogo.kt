package br.edu.unipe.meubolso.dados

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.edu.unipe.meubolso.ui.movimentacoes.Categoria
import java.util.Locale

@Entity
data class CategoriaLocal(@PrimaryKey val id: String, val nome: String, val icone: String, val ordem: Int)

internal fun preencherCategorias(db: SupportSQLiteDatabase) {
    Categoria.values().forEachIndexed { i, categoria ->
        db.execSQL("INSERT OR IGNORE INTO CategoriaLocal(id,nome,icone,ordem) VALUES(?,?,?,?)",
            arrayOf(categoria.name, categoria.rotulo, "ic_categoria_" + categoria.name.lowercase(Locale.ROOT), i))
    }
}

val CRIAR_CATALOGO = object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) = preencherCategorias(db)
}

val MIGRACAO_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE CategoriaLocal (id TEXT NOT NULL PRIMARY KEY, nome TEXT NOT NULL, icone TEXT NOT NULL, ordem INTEGER NOT NULL)")
        preencherCategorias(db)
        db.execSQL("ALTER TABLE Parcela ADD COLUMN ultimoAviso TEXT")
    }
}
