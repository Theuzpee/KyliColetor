# Contrato de API (Backend NestJS)

O aplicativo APS Coletor operará no modo Offline-First. Abaixo estão os endpoints que o backend NestJS precisará disponibilizar para receber e gerenciar a sincronização do picking do aplicativo Android.

## 1. Sincronização de Caixa

Responsável por receber os dados completos de uma caixa (FINALIZADA ou PARCIAL) e armazená-los no servidor.

`POST /api/picking/sync-box`

**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer <token>` (Futuro)

**Body:**
```json
{
  "papeletaCode": "string",
  "orderId": "string",
  "status": "FINALIZADA | PARCIAL | MULTI_ANDAR",
  "collectedAt": "2026-04-27T14:30:00Z",
  "items": [
    {
      "reference": "string",
      "color": "string",
      "size": "string",
      "address": "string",
      "quantityRequired": 3,
      "quantityCollected": 3,
      "status": "COMPLETO | FALTA | PENDENTE",
      "scannedPieces": [
        {
          "barcode": "1000079",
          "scannedAt": "2026-04-26T14:35:00Z"
        }
      ],
      "divergences": [
        {
          "reason": "DESABASTECIDO",
          "barcode": null,
          "registeredAt": "2026-04-26T14:36:00Z"
        }
      ]
    }
  ]
}
```

**Responses:**
- `200 OK`: Sincronização realizada com sucesso.
  ```json
  { "syncId": "uuid-string", "syncedAt": "2026-04-27T14:31:00Z" }
  ```
- `400 Bad Request`: Erro de validação.
  ```json
  { "error": "Invalid data", "details": "string" }
  ```
- `409 Conflict`: Caixa já sincronizada.
  ```json
  { "error": "Caixa já sincronizada", "syncId": "uuid-string" }
  ```

### 1.1 Comportamento MULTI_ANDAR
- Uma caixa com status `MULTI_ANDAR` pode ser sincronizada parcialmente com o servidor para garantir que o progresso não seja perdido.
- Ao reabrir uma caixa, o Android chama `GET /api/picking/boxes/{papeletaCode}` para verificar o estado atual antes de abrir localmente.
- A finalização real da caixa só ocorre quando o status muda para `FINALIZADA` ou `PARCIAL`. Apenas nestes status a propriedade `syncedAt` é preenchida e fechada.

---

## 2. Verificação de Duplicidade

Usado para validar antecipadamente se uma papeleta já foi processada (evitando reprocessamento caso o app apague o banco).

`GET /api/picking/boxes/{papeletaCode}`

**Responses:**
- `200 OK`: Caixa já sincronizada.
  ```json
  { "exists": true, "syncId": "uuid-string" }
  ```
- `404 Not Found`: Caixa não sincronizada ainda.
  ```json
  { "exists": false }
  ```

---

## 3. Consulta de Divergências e Relatórios

### 3.1 Lista de Divergências
Permite consultar todas as divergências, com possibilidade de filtros.

`GET /api/picking/divergences`

**Query Params:**
- `date`: `string (ISO8601)` - (opcional) Filtra por data de registro
- `reason`: `SkipReason` - (opcional) Filtra por motivo
- `boxId`: `string` - (opcional) Filtra por caixa UUID

**Response:**
- `200 OK`:
  ```json
  {
    "total": 25,
    "divergences": [
      {
        "id": "uuid-string",
        "papeletaCode": "PAP123",
        "orderId": "PED-999",
        "item": {
          "reference": "1000079"
        },
        "barcode": "string | null",
        "reason": "SUJA",
        "registeredAt": "2026-04-26T14:36:00Z"
      }
    ]
  }
  ```

### 3.2 Resumo Gerencial de Divergências
Retorna a contagem agrupada por `reason`.

`GET /api/picking/divergences/summary`

**Query Params:**
- `startDate`: `string (ISO8601)` - (opcional) Data de início
- `endDate`: `string (ISO8601)` - (opcional) Data de fim

**Response:**
- `200 OK`:
  ```json
  {
    "period": { "start": "2026-04-01T00:00:00Z", "end": "2026-04-30T23:59:59Z" },
    "total": 45,
    "byReason": {
      "DESABASTECIDO": 20,
      "SUJA": 8,
      "AMASSADA": 5,
      "DESEMBALADA": 3,
      "DESCASCADA": 2,
      "TAG_ERRADO": 4,
      "NAO_LE_CODIGO": 3
    }
  }
  ```

### 3.3 Divergências de uma Caixa Específica
Lista as divergências pertencentes a uma caixa.

`GET /api/picking/boxes/{papeletaCode}/divergences`

**Responses:**
- `200 OK`: Retorna array com as divergências.
- `404 Not Found`:
  ```json
  { "message": "Caixa com papeleta PAP123 não encontrada" }
  ```

---

## 4. Autenticação e Sincronização de ERP

A autenticação garante que o coletor opere em nome de um Colaborador validado por um Supervisor de turno. 
Os dados de login provêm do ERP e são sincronizados via API Key.

### 4.1 Sincronizar Usuários do ERP (Backend-to-Backend)

Atualiza o banco local com os supervisores e operadores do ERP. Usuários que não forem enviados no payload serão inativados localmente.

`POST /api/auth/erp-sync`

**Headers:**
- `X-Api-Key: <ERP_API_KEY>`

**Body:**
```json
{
  "supervisors": [
    { "barcode": "SUP-01", "name": "João Supervisor", "shift": "TURNO_1" }
  ],
  "operators": [
    { "barcode": "OP-001", "employeeCode": "EMP999", "name": "Maria Operadora" }
  ]
}
```

**Responses:**
- `201 Created`: Sincronização com sucesso. Retorna log de sync.
- `401 Unauthorized`: API Key inválida.

### 4.2 Login no Coletor

Gera um token JWT com base na leitura do crachá do supervisor e do operador. Rate Limit: 10 tentativas/minuto.

`POST /api/auth/login`

**Body:**
```json
{
  "supervisorBarcode": "SUP-01",
  "operatorBarcode": "OP-001"
}
```

**Responses:**
- `201 Created`: Login bem-sucedido.
  ```json
  {
    "token": "jwt-string",
    "operatorName": "Maria Operadora",
    "operatorCode": "EMP999",
    "supervisorName": "João Supervisor",
    "shift": "TURNO_1",
    "expiresAt": "2026-04-28T16:00:00Z"
  }
  ```
- `401 Unauthorized`: Códigos inválidos ou inativos.
- `429 Too Many Requests`: Rate Limit excedido.

### 4.3 Dados do Operador Logado

Verifica se o token ainda é válido e retorna o payload.

`GET /api/auth/me`

**Headers:**
- `Authorization: Bearer <token>`

**Responses:**
- `200 OK`: 
  ```json
  {
    "userId": "uuid-string",
    "operatorCode": "EMP999",
    "operatorName": "Maria Operadora",
    "supervisorCode": "SUP-01",
    "supervisorName": "João Supervisor",
    "shift": "TURNO_1"
  }
  ```
- `401 Unauthorized`: Token expirado ou inválido.
