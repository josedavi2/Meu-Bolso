# Meu Bolso

Aplicativo Android de controle financeiro pessoal para estudantes. Cada perfil mantém suas movimentações e contas fixas no aparelho.

## Executar

Abra a pasta que contém `settings.gradle.kts` no Android Studio. Configure JDK 17, Android SDK Platform 35 e Build Tools 34.0.0. Execute o módulo `app` em um aparelho ou emulador Android 8.0 ou superior. Para gerar o APK de depuração e rodar os testes no Windows, execute `gradlew.bat :app:assembleDebug :app:testDebugUnitTest`.

## Fluxo do aplicativo

Crie um perfil, acesse o painel e use **+ Movimentação** para registrar receitas e despesas. O histórico permite filtrar, editar e excluir lançamentos. O painel apresenta os totais do período selecionado e o cofrinho do mês. Os relatórios mostram as despesas por categoria e a comparação mensal.

Em **Contas fixas**, selecione o mês e cadastre a descrição, o valor, a categoria e o dia do vencimento. Abra a conta para editar a recorrência, consultar as parcelas, registrar a data de pagamento ou desfazer a confirmação.

## Equipe e planejamento

- Vitória Luclécia — planejamento, Splash, acesso local, painel, cofrinho e contas fixas.
- José Davi — formulário de movimentações, categorias, histórico, filtros, relatórios e gráficos.
- Responsabilidades compartilhadas — dados locais, pagamentos, notificações, testes e apresentação. Os lembretes diários continuam pendentes.

[Plano de projeto da equipe](docs/plano-de-projeto.pdf)
