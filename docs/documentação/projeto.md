# Documentação do Projeto KylyColetor (APS Coletor)

## 1. Visão Geral do Projeto

O **Kyly Coletor (APS Coletor)** é um sistema completo (Aplicativo Android + API Backend) desenvolvido para o Grupo Kyly. O foco principal é a **bipagem intensiva de produtos para controle de inventário e logística em operações de grande volume** no galpão de expedição. 

O sistema foi concebido com uma premissa rigorosa de **offline-first**, garantindo a continuidade da operação mesmo na ausência de rede (comum em grandes galpões), sincronizando os dados em background.

---

## 2. Domínio de Negócio

*   **Empresa:** Grupo Kyly (indústria têxtil, expedição de pedidos).
*   **Processo:** Picking — coleta de peças de vestuário nos endereços físicos do galpão para a montagem de pedidos.
*   **Operadores:** Colaboradores de chão de fábrica que utilizam coletores de dados.
*   **Dispositivo Alvo:** Datalogic Memor 11 (dispositivo Android corporativo com scanner laser integrado via DataWedge). Não utiliza a câmera do dispositivo.

---

## 3. Fluxo Operacional (POP-EXP-009)

O fluxo de coleta segue etapas rigorosas para garantir a rastreabilidade e evitar erros:

1.  **Login:** Bipar código de barras do supervisor (por turno) e digitar código do colaborador.
2.  **Seleção de Módulo:** Acessar "1. Montar Pedido".
3.  **Abertura de Caixa:** Bipar papeleta da caixa (1 a 2 caixas por carrinho). Se a caixa já foi iniciada, o sistema retoma o progresso.
4.  **Localização:** O coletor indica o endereço de forma visual (Ex: `C37.09.6B`), junto com a referência, cor, tamanho e quantidade.
5.  **Bipagem:** Leitura da etiqueta única da peça. A validação é feita item a item.
6.  **Exceções e Divergências:**
    *   *Peça em falta:* "RETIRAR ITEM EM FALTA" (sem buscar endereço alternativo).
    *   *Defeitos:* Produto sujo/amassado/descasado vai para o cestinho de divergências.
    *   *SKU Incorreto ou Sem Saldo:* Rejeição com alerta visual e sonoro, exigindo confirmação do operador.
7.  **Encerramento:** 
    *   Caixa completa: Mensagem de finalização e bipagem da papeleta novamente.
    *   Caixa parcial/Multi-andar: Salvas com status correspondentes para posterior conclusão.
8.  **Tarjas Visuais:** Identificam o tipo de pedido (Azul = Exportação, Verde/Rosa = Tag, Amarelo = Multi).

---

## 4. Comportamento e Feedback Sensorial

A confiança do operador baseia-se fortemente no feedback rápido e claro para cada leitura, sem precisar olhar para a tela continuamente.

*   ✅ **Peça OK (SKU Parcial):** LED verde + 1 bipe simples.
*   ✅ **Peça OK (SKU Completa):** LED verde + 2 bipes simples + atualiza endereço.
*   ❌ **Erro (SKU inválida ou sem saldo):** LED vermelho + bipe contínuo 2s + mensagem em tela.
*   🏁 **Finalizações:** Sinais sonoros distintos para Caixa Finalizada e Caixa Parcial.

> **Importante:** O laser (via DataWedge) é desligado imediatamente após cada leitura e reativado apenas por interação da UI.

---

## 5. Stack Tecnológico e Arquitetura

O projeto é um monorepo que contempla tanto o Backend quanto o App Mobile.

### 5.1. Aplicativo Android (`/app`)
*   **Linguagem:** Kotlin.
*   **UI:** Jetpack Compose + Material 3.
*   **Arquitetura:** Clean Architecture + MVVM. Separação estrita em camadas: `data`, `domain`, `presentation`, `util`.
*   **Injeção de Dependência:** Hilt.
*   **Persistência (Offline-first):** Room Database.
*   **Tarefas em Background:** WorkManager e Kotlin Coroutines (StateFlow/SharedFlow).
*   **Rede:** Retrofit + OkHttp.
*   **Integração de Hardware:** Broadcast Receivers (para DataWedge).
*   **Testes:** JUnit 5, MockK, Coroutines Test, Compose Test Rule.

### 5.2. Backend (`/aps-coletor-backend`)
*   **Linguagem/Framework:** TypeScript com NestJS (v11).
*   **Banco de Dados:** PostgreSQL com TypeORM.
*   **Segurança/Autenticação:** JWT (Passport), Helmet, Throttler (Rate Limiting).
*   **Documentação:** Swagger (OpenAPI).
*   **Testes:** Jest e Supertest (Unitários e E2E).

---

## 6. Diretrizes de Desenvolvimento (Skills)

Para manter o alto padrão técnico do projeto, as seguintes diretrizes são estritamente observadas:

1.  **Offline-first:** Não se trata exceção de rede; trabalhar offline é o comportamento padrão, com filas de sincronização garantindo a integridade dos dados.
2.  **Acesso ao Scanner:** **NUNCA** usar a câmera. O escaneamento é sempre via hardware embarcado (DataWedge) para latência zero e eficiência energética.
3.  **Clean Architecture:**
    *   **Domain:** Regras puras, sem conhecimento de banco ou rede.
    *   **Data:** Fontes de dados locais e remotas (Room/Retrofit).
    *   **Presentation:** Apenas lógica de visualização, sem regras de negócio.
4.  **Tratamento de Erros:** Exceções brutas nunca chegam ao operador. O aplicativo apresenta mensagens acionáveis e em português.
5.  **Revisão de Código Contextual:** O código é avaliado quanto ao respeito aos limites arquiteturais, uso correto do hardware e cumprimento das regras de negócio.

---

## 7. Estratégia de Testes

*   **Fakes vs Mocks:** Preferência por uso de repositórios/DAOs fake e banco in-memory (Room) ao invés de excesso de mocks baseados em reflexão.
*   **Cobertura em Profundidade:** Todos os fluxos operacionais de sucesso e falha (falta de saldo, SKU errado, offline) devem ser cobertos na camada de domínio.
*   **Coroutines e Fluxos:** Validados usando `runTest` e Dispatchers específicos de teste.
*   **Testes de Integração de UI:** O Compose UI deve testar a correta alteração de estados da tela dependendo do StateFlow da ViewModel.
