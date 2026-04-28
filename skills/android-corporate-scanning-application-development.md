---
name: android-corporate-scanning-app
description: Diretrizes para o desenvolvimento de aplicativos Android nativos corporativos para escaneamento intensivo de produtos no controle de inventário.
---

# Desenvolvimento de Aplicativos de Escaneamento Corporativo Android

## 🎯 Objetivo
Desenvolver aplicativos Android corporativos de alto desempenho, resilientes a falhas de conexão e ergonomicamente otimizados para coleta intensiva de dados e escaneamento de inventário.

## 📋 Gatilho / Ativação
**Anunciar no início:** "Estou utilizando a skill de Desenvolvimento de Apps Corporativos de Escaneamento Android para desenvolver esta funcionalidade de inventário corporativo."

## 🏗️ Arquitetura & Stack
- **Dispositivos Alvo:** Dispositivos profissionais de coleta de dados (Zebra/Honeywell)
- **Linguagem:** Kotlin
- **UI:** Jetpack Compose
- **Arquitetura:** MVVM e Clean Architecture
- **Dados Locais:** Room Database (Offline-first)
- **Concorrência:** Kotlin Coroutines e StateFlow/SharedFlow
- **Rede:** Retrofit + OkHttp
- **Hardware:** Broadcast Receivers (DataWedge - latência zero, SEM câmera)
- **Backend:** API RESTful (NestJS/Node.js) com PostgreSQL

## 📜 Diretrizes Principais (CRÍTICO)

### 1. Design Offline-First
- Os aplicativos DEVEM suportar o escaneamento de centenas de itens sem conectividade de rede.
- Implementar uma fila de sincronização em segundo plano confiável.
- Enfileirar operações quando offline e sincronizar imediatamente quando a rede for restaurada.

### 2. Sistema de Feedback Sensorial
- Implementar feedback imediato e perceptível para cada escaneamento.
- Fornecer feedback sonoro e tátil distinto para escaneamentos bem-sucedidos vs. tentativas falhas.
- Garantir a confiança do usuário através de pistas físicas e visuais sem a necessidade de olhar constantemente para a tela.

### 3. Testabilidade
- Escrever código pensando na testabilidade desde o primeiro dia.
- Usar interfaces e injeção de dependência (Hilt/Dagger).
- Exigir testes unitários para ViewModels e UseCases (JUnit + MockK).
- Estruturar a aplicação para futuros testes E2E com Appium.

### 4. Tratamento de Erros Robusto
- NUNCA mostrar exceções técnicas brutas ao operador.
- Fornecer mensagens claras e acionáveis para erros de rede, falhas de sincronização ou códigos inválidos.
- Oferecer sugestões de ações corretivas imediatas dentro da UI.

### 5. Fluxo de Trabalho de Implementação
Para cada nova funcionalidade ou tela:
1. Fornecer código Kotlin seguindo estritamente MVVM.
2. Construir a UI exclusivamente com Jetpack Compose.
3. Explicar decisões arquiteturais e posicionamento de pastas.
4. Sugerir o caso de teste unitário principal para as regras de negócio.

## 📁 Convenção de Estrutura de Pastas
```
app/
├── data/               # Camada de dados (repositórios, fontes de dados, local/remoto)
├── domain/            # Lógica de negócio (modelos, casos de uso, interfaces de repositório)
├── presentation/      # Camada de UI (di, componentes compose principais, viewmodels, navegação)
└── util/              # Classes utilitárias e extensões
```

## 🚫 Anti-Patterns (NUNCA FAÇA ISSO)
- Não use a câmera do dispositivo para escaneamento; use sempre o scanner de hardware via DataWedge.
- Não trate a capacidade offline como algo secundário; ela deve ser o estado padrão.
- Não bloqueie a thread principal com operações de banco de dados ou rede.

## ✅ Checklist de Verificação
- [ ] A arquitetura é limpa e defensível para uma revisão executiva?
- [ ] As integrações de hardware estão usando Broadcast Receivers?
- [ ] A fila offline está implementada para a funcionalidade?
- [ ] Feedbacks sensoriais (tátil/sonoro) são acionados nas ações?