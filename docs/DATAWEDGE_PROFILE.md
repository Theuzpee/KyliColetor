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

## Confirmação de Endereço Físico

O sistema de coleta utiliza um fluxo em duas etapas:
1. O operador bipa a etiqueta física do corredor/prateleira (endereço).
2. Após o sistema validar o endereço com sucesso, o scanner é liberado para bipar a peça.

**Atenção para testes de homologação:**
O formato exato da etiqueta física impressa no galpão Kyly ainda precisa ser validado. Para suportar esta incerteza, o aplicativo implementa 3 estratégias de validação flexíveis:
- **Correspondência exata:** Código bipado é idêntico ao esperado.
- **Contém o endereço:** Código bipado é uma string maior que contém o endereço (ex: `KYLY-C37.09-01`).
- **Endereço contém o código:** O esperado contém o código lido (validação parcial).

**Ação requerida:** O técnico de implantação deve testar os 3 formatos físicos disponíveis no galpão diretamente no dispositivo e informar à equipe de desenvolvimento qual padrão foi adotado, permitindo o ajuste fino do `AddressValidatorImpl` no código, se necessário.
