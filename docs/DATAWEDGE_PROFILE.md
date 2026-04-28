# Perfil DataWedge — APS Coletor

## Configuração do Dispositivo (Datalogic Memor 11)

Para o aplicativo funcionar corretamente, configure o perfil no DataWedge com os seguintes parâmetros:

- **Profile Name:** APSColetorProfile (ou qualquer nome desejado)
- **Associated Apps:** `br.com.grupokyly.apscoletor`
- **Intent Output:** Habilitado
  - **Intent Action:** `br.com.grupokyly.apscoletor.SCAN`
  - **Intent Category:** `android.intent.category.DEFAULT`
  - **Intent Delivery:** `Broadcast Intent`
- **Keystroke Output:** Desabilitado (para evitar digitação fantasma)

## Observações de Hardware

Controle de gatilho físico via DataWedge Scanner API está previsto para implementação futura. Atualmente o controle é feito via lógica na ViewModel (bipagens fora do estado Collecting são ignoradas silenciosamente).
