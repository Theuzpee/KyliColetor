# Configuração do Datalogic Memor 11

## Opção A — Keyboard Wedge (recomendado para começar)
Mais simples, não precisa de configuração no app.

No dispositivo:
  Datalogic Settings → Scanner & Decoder → Wedge
    → Input Selection: Keyboard Wedge
    → Terminator: Enter (\n)

Resultado: scanner preenche o campo focado automaticamente
e pressiona Enter ao terminar.

## Opção B — Intent Output (mais robusto para produção)
No dispositivo:
  Datalogic Settings → Scanner & Decoder → Wedge
    → Input Selection: Intent
    → Intent Action: com.datalogic.decode.action.SCAN_RESULT
    → Intent Category: android.intent.category.DEFAULT
    → Intent Delivery: Broadcast

Resultado: ScannerReceiver captura via BroadcastReceiver.

## Recomendação
- Desenvolvimento/teste: usar Opção A (Keyboard Wedge)
- Produção: migrar para Opção B (Intent) para mais controle
