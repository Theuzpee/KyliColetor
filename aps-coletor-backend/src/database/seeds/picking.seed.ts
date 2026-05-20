import { NestFactory } from '@nestjs/core';
import { AppModule } from '../../app.module';
import { DataSource } from 'typeorm';
import { Box } from '../../picking/entities/box.entity';
import { PickingItem } from '../../picking/entities/picking-item.entity';
import { DivergenceEntity } from '../../picking/entities/divergence.entity';
import { SkipReason } from '../../picking/enums/skip-reason.enum';

async function bootstrap() {
  const app = await NestFactory.createApplicationContext(AppModule);
  const dataSource = app.get(DataSource);

  const boxRepo = dataSource.getRepository(Box);
  const itemRepo = dataSource.getRepository(PickingItem);
  const divRepo = dataSource.getRepository(DivergenceEntity);

  console.log('📦 Iniciando o seed de Picking...');

  const dateNow = new Date();

  // Função auxiliar para evitar duplicidade no seed
  async function saveBoxIfNotExists(boxData: Partial<Box>, itemsData: Partial<PickingItem>[], divergencesData: Partial<DivergenceEntity>[]) {
    const exists = await boxRepo.findOne({ where: { papeletaCode: boxData.papeletaCode } });
    if (!exists) {
      const box = boxRepo.create({
        ...boxData,
        syncedAt: boxData.syncedAt || dateNow,
        collectedAt: dateNow,
      });
      box.items = itemsData.map(i => itemRepo.create(i));
      box.divergences = divergencesData.map(d => divRepo.create(d));
      await boxRepo.save(box);
      console.log(`✅ Caixa inserida: ${boxData.papeletaCode}`);
    } else {
      console.log(`⚡ Caixa já existe: ${boxData.papeletaCode}`);
    }
  }

  // CENÁRIO 1 — Caixa 100% finalizada (caminho feliz)
  await saveBoxIfNotExists(
    { papeletaCode: 'PAP-FINALIZADA-001', orderId: 'PED-1001', status: 'FINALIZADA' },
    [
      { reference: 'REF-1A', color: 'AZUL', size: 'P', address: 'A01', quantityRequired: 5, quantityCollected: 5, status: 'COMPLETO' },
      { reference: 'REF-1B', color: 'AZUL', size: 'M', address: 'A01', quantityRequired: 5, quantityCollected: 5, status: 'COMPLETO' },
      { reference: 'REF-1C', color: 'AZUL', size: 'G', address: 'A01', quantityRequired: 5, quantityCollected: 5, status: 'COMPLETO' },
    ],
    []
  );

  // CENÁRIO 2 — Caixa com picking parcial (desabastecimento)
  await saveBoxIfNotExists(
    { papeletaCode: 'PAP-PARCIAL-001', orderId: 'PED-1002', status: 'PARCIAL' },
    [
      { reference: 'REF-2A', color: 'PRETO', size: 'M', address: 'B02', quantityRequired: 10, quantityCollected: 10, status: 'COMPLETO' },
      { reference: 'REF-2B', color: 'PRETO', size: 'G', address: 'B02', quantityRequired: 10, quantityCollected: 10, status: 'COMPLETO' },
      { reference: 'REF-FALTA', color: 'BRANCO', size: 'M', address: 'B03', quantityRequired: 5, quantityCollected: 0, status: 'FALTA' },
    ],
    [
      { reason: SkipReason.DESABASTECIDO, pickingItemId: 'REF-FALTA', registeredAt: dateNow }
    ]
  );

  // CENÁRIO 3 — Caixa com divergências de defeito
  await saveBoxIfNotExists(
    { papeletaCode: 'PAP-DEFEITO-001', orderId: 'PED-1003', status: 'PARCIAL' },
    [
      { reference: 'REF-3A', color: 'VERDE', size: 'P', address: 'C04', quantityRequired: 2, quantityCollected: 2, status: 'COMPLETO' },
      { reference: 'REF-3B', color: 'VERDE', size: 'M', address: 'C04', quantityRequired: 2, quantityCollected: 2, status: 'COMPLETO' },
      { reference: 'REF-DEF', color: 'VERDE', size: 'G', address: 'C04', quantityRequired: 4, quantityCollected: 2, status: 'FALTA' },
    ],
    [
      { reason: SkipReason.SUJA, pickingItemId: 'REF-DEF', barcode: 'PECA-SUJA-001', registeredAt: dateNow },
      { reason: SkipReason.TAG_ERRADO, pickingItemId: 'REF-DEF', barcode: null, registeredAt: dateNow }
    ]
  );

  // CENÁRIO 4 — Caixa multi-andar (picking parcial intencional)
  await saveBoxIfNotExists(
    { papeletaCode: 'PAP-MULTI-001', orderId: 'PED-1004', status: 'MULTI_ANDAR' },
    [
      { reference: 'REF-4A', color: 'ROSA', size: 'M', address: 'D01-ANDAR-A', quantityRequired: 3, quantityCollected: 3, status: 'COMPLETO' },
      { reference: 'REF-4B', color: 'ROSA', size: 'G', address: 'D01-ANDAR-A', quantityRequired: 3, quantityCollected: 3, status: 'COMPLETO' },
      { reference: 'REF-4C', color: 'AMARELO', size: 'M', address: 'Z99-ANDAR-B', quantityRequired: 5, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'REF-4D', color: 'AMARELO', size: 'G', address: 'Z99-ANDAR-B', quantityRequired: 5, quantityCollected: 0, status: 'PENDENTE' },
    ],
    []
  );

  // CENÁRIO 5 — Caixa já sincronizada (para testar idempotência 409)
  await saveBoxIfNotExists(
    { papeletaCode: 'PAP-SYNC-001', orderId: 'PED-1005', status: 'FINALIZADA', syncedAt: new Date(Date.now() - 3600000) },
    [
      { reference: 'REF-5A', color: 'CINZA', size: 'M', address: 'E05', quantityRequired: 1, quantityCollected: 1, status: 'COMPLETO' }
    ],
    []
  );

  // CENÁRIO 6 — Caixa 100% pendente (Início do fluxo limpo para teste no app)
  await saveBoxIfNotExists(
    { papeletaCode: 'PAP-PENDENTE-001', orderId: 'PED-1006', status: 'EM_COLETA' },
    [
      { reference: 'REF-6A', color: 'PRETO', size: 'P', address: 'A02', quantityRequired: 3, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'REF-6B', color: 'PRETO', size: 'M', address: 'A02', quantityRequired: 2, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'REF-6C', color: 'PRETO', size: 'G', address: 'B01', quantityRequired: 1, quantityCollected: 0, status: 'PENDENTE' }
    ],
    []
  );

  console.log('✅ Seed de Picking finalizado com sucesso!');
  await app.close();
}

bootstrap().catch(err => {
  console.error('❌ Erro no seed de picking:', err);
  process.exit(1);
});
