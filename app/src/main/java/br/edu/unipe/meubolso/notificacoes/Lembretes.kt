package br.edu.unipe.meubolso.notificacoes

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.work.*
import br.edu.unipe.meubolso.R
import br.edu.unipe.meubolso.dados.MeuBolso
import br.edu.unipe.meubolso.ui.contas.DetalhesContaActivity
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.TimeUnit

class VerificarVencimentos(contexto: Context, parametros: WorkerParameters) : Worker(contexto, parametros) {
    override fun doWork(): Result {
        Lembretes.verificar(applicationContext as MeuBolso, LocalDate.now())
        return Result.success()
    }
}

object Lembretes {
    private const val CANAL = "vencimentos"
    private const val ID = 1

    fun canalBloqueado(contexto: Context): Boolean =
        contexto.getSystemService(NotificationManager::class.java).getNotificationChannel(CANAL)?.importance == NotificationManager.IMPORTANCE_NONE

    fun ajustes(contexto: Context): Intent = if (canalBloqueado(contexto)) {
        Intent(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, contexto.packageName)
            .putExtra(android.provider.Settings.EXTRA_CHANNEL_ID, CANAL)
    } else {
        Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, contexto.packageName)
    }

    fun agendar(contexto: Context) {
        val trabalho = PeriodicWorkRequestBuilder<VerificarVencimentos>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(contexto).enqueueUniquePeriodicWork("vencimentos-diarios", ExistingPeriodicWorkPolicy.KEEP, trabalho)
    }

    fun conferirAgora(contexto: Context) {
        WorkManager.getInstance(contexto).enqueueUniqueWork("conferir-vencimentos",
            ExistingWorkPolicy.APPEND_OR_REPLACE, OneTimeWorkRequestBuilder<VerificarVencimentos>().build())
    }

    fun verificar(app: MeuBolso, hoje: LocalDate) {
        val perfil = app.perfil
        if (perfil.isEmpty()) return
        val sistema = app.getSystemService(NotificationManager::class.java)
        sistema.createNotificationChannel(NotificationChannel(CANAL, "Vencimentos de contas", NotificationManager.IMPORTANCE_DEFAULT))
        if (!sistema.areNotificationsEnabled() || sistema.getNotificationChannel(CANAL).importance == NotificationManager.IMPORTANCE_NONE) return
        if (Build.VERSION.SDK_INT >= 33 && app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

        app.banco.runInTransaction {
            app.dao.preparar(perfil, YearMonth.from(hoje))
            for (parcela in app.dao.aAvisar(perfil, hoje.toString())) {
                if (app.perfil != perfil) break
                val tag = "${parcela.conta}/${parcela.mes}"
                val destino = Intent(app, DetalhesContaActivity::class.java)
                    .setData(Uri.parse("meubolso://vencimento/$tag"))
                    .putExtra("id", parcela.conta).putExtra("mes", parcela.mes).putExtra("perfil_aviso", perfil)
                val abrir = PendingIntent.getActivity(app, 0, destino,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                val aviso = Notification.Builder(app, CANAL).setSmallIcon(R.drawable.ic_vencimento)
                    .setContentTitle("Conta vence hoje").setContentText("Toque para conferir a parcela ou registrar o pagamento.")
                    .setContentIntent(abrir).setAutoCancel(true).setOnlyAlertOnce(true).build()
                sistema.notify(tag, ID, aviso)
                app.dao.atualizar(parcela.copy(ultimoAviso = hoje.toString()))
            }
        }
    }

    fun retirar(contexto: Context, conta: String, mes: String) {
        contexto.getSystemService(NotificationManager::class.java).cancel("$conta/$mes", ID)
    }
}
