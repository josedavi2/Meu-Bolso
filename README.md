# Meu Bolso

O **Meu Bolso** é um aplicativo Android de controle financeiro pessoal para estudantes. Ele ajuda a responder duas perguntas diferentes: **quanto dinheiro sobrou neste mês?** e **quanto vai sobrar depois que eu pagar as contas em aberto?** O usuário registra receitas e despesas, acompanha vencimentos e consulta seus hábitos de consumo por mês, ano e categoria.

O app funciona offline. Cada perfil tem seus próprios lançamentos no aparelho. O projeto foi desenvolvido por Vitória Luclécia e José Davi na disciplina de Desenvolvimento Mobile do UNIPÊ.

## Do lançamento ao relatório

| Onde | O que o usuário pode fazer |
| --- | --- |
| **Painel** | Conferir saldo atual e previsto, receitas, despesas pagas, contas em aberto e o cofrinho que indica a proporção da receita mensal ainda disponível. |
| **Lançar** | Informar descrição, valor, tipo, categoria e data de uma receita ou despesa. |
| **Histórico** | Combinar filtros de tipo, categoria, mês e ano. Tocar em um lançamento avulso para editar ou excluir com confirmação. |
| **Contas fixas** | Cadastrar uma despesa recorrente, consultar parcelas mensais, registrar a data de pagamento e desfazer um pagamento. |
| **Relatórios** | Consultar os totais do período e a categoria de maior gasto. O gráfico de pizza distribui as despesas por categoria. A visão anual compara receitas e despesas dos 12 meses em barras. |

O cabeçalho do painel permite escolher o tema claro, escuro ou do sistema. O aplicativo guarda essa preferência entre as sessões.

### Por que existem dois saldos?

Imagine uma bolsa de **R$ 1.000,00**, uma compra de mercado de **R$ 750,00** e uma conta de internet de **R$ 90,00**, todas no mesmo mês.

| Ação | Saldo atual | Saldo previsto |
| --- | ---: | ---: |
| Registrar a bolsa e a compra | R$ 250,00 | R$ 250,00 |
| Cadastrar a internet e deixar a parcela pendente | R$ 250,00 | R$ 160,00 |
| Confirmar **Já paguei** com a data de hoje | R$ 160,00 | R$ 160,00 |

A **conta fixa** define a recorrência. A **parcela** representa o vencimento de um mês. Ao confirmar o pagamento, o app cria uma despesa e vincula seu identificador à parcela na mesma transação do banco. Isso impede que a mesma parcela gere duas despesas. Se a conta de novembro for paga em dezembro, a despesa aparece no relatório de dezembro.

## Como foi implementado

A interface usa **Kotlin, Activities e layouts XML**. O **View Binding** conecta os campos e botões às ações de cada tela. Para acompanhar uma movimentação no código, siga o mesmo valor em quatro pontos:

1. **Formulário.** `MovimentacaoActivity.confirmar()` valida os campos. A entrada `25,90` vira `2590` centavos, permitindo armazenar e somar dinheiro com precisão.
2. **Persistência.** `HistoricoSessao.adicionar()` identifica o lançamento e o associa ao perfil ativo. O DAO em `Banco.kt` grava o registro no **Room**, que usa **SQLite** no aparelho.
3. **Consulta.** `HistoricoActivity` aplica os filtros e apresenta os registros por data em uma **RecyclerView**. Duas compras com a mesma descrição podem ser editadas separadamente porque cada uma tem seu próprio identificador.
4. **Análise.** `ResumoFinanceiro.calcular()` agrupa os lançamentos por período e categoria. `RelatoriosActivity` apresenta o resultado com **MPAndroidChart**. Assim, duas compras de alimentação aparecem como duas linhas no histórico e uma única fatia no gráfico.

O login também é local. O banco guarda um hash da senha com salt individual, e as preferências do aplicativo identificam a sessão ativa. Para os lembretes, o **WorkManager** agenda uma verificação diária das parcelas que vencem no dia. No Android 13 ou superior, o usuário precisa permitir notificações. O horário da entrega depende do Android, e as pendências continuam acessíveis pelo painel.

[Código-fonte do aplicativo](app/src/main/java/br/edu/unipe/meubolso) · [Layouts das telas](app/src/main/res/layout)

## Executar no Android Studio

1. Clone o repositório com `git clone https://github.com/josedavi2/Meu-Bolso.git`. No Windows, use um caminho simples, como `C:\Projetos\Meu-Bolso`.
2. Abra a pasta que contém `settings.gradle.kts`. Use **JDK 17**, **Android SDK Platform 35** e **Build Tools 34.0.0**. Aguarde a sincronização inicial das dependências.
3. Escolha o módulo **app** e um celular ou emulador com **Android 8.0 (API 26)** ou superior. Clique em **Run ▶**, crie um perfil e registre a primeira movimentação.

### Testar e gerar o APK

Na raiz do projeto, execute o comando correspondente ao terminal:

| Terminal | Comando |
| --- | --- |
| Windows PowerShell | `.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest` |
| Windows Git Bash | `./gradlew.bat :app:assembleDebug :app:testDebugUnitTest` |
| Linux ou macOS | `bash gradlew :app:assembleDebug :app:testDebugUnitTest` |

O **APK de depuração** fica em `app/build/outputs/apk/debug/app-debug.apk`. O relatório de testes fica em `app/build/reports/tests/testDebugUnitTest/index.html`. A suíte cobre, entre outros casos, conversão de valores, filtros combinados, cálculos mensais e anuais, pagamento sem duplicidade, isolamento entre perfis e migração do banco.

## Equipe

| Integrante | Responsabilidades |
| --- | --- |
| **Vitória Luclécia da Conceição de Oliveira** | Planejamento, abertura do app, cadastro e login, painel, cofrinho e contas fixas. |
| **José Davi Maurício Oliveira** | Cadastro e edição de movimentações, categorias e ícones, histórico e filtros, relatórios e gráficos. |
| **Dupla** | Modelo de dados e integração com o Room, confirmação de pagamentos, lembretes locais, testes e apresentação. |

[Plano de projeto (PDF)](docs/plano-de-projeto.pdf)
