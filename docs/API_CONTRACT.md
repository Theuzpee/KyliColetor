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
          "barcode": "string",
          "scannedAt": "2026-04-27T14:28:00Z"
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

## 3. Autenticação (Para Futuro)

Autentica o supervisor e o operador do coletor, gerando um token JWT.

`POST /api/auth/login`

**Body:**
```json
{
  "supervisorCode": "string",
  "operatorCode": "string"
}
```

**Responses:**
- `200 OK`: Login bem-sucedido.
  ```json
  { "token": "jwt-string", "operatorName": "string" }
  ```
