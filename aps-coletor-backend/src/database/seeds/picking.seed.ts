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

  // CENÁRIO 7 — Caixa feminina linha verão (pendente, pronta para coleta)
  await saveBoxIfNotExists(
    { papeletaCode: 'PAP-PENDENTE-002', orderId: 'PED-2001', status: 'EM_COLETA' },
    [
      { reference: 'FEM-001', color: 'ROSA', size: 'PP', address: 'C10', quantityRequired: 4, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'FEM-001', color: 'ROSA', size: 'P',  address: 'C10', quantityRequired: 6, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'FEM-001', color: 'ROSA', size: 'M',  address: 'C10', quantityRequired: 8, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'FEM-001', color: 'ROSA', size: 'G',  address: 'C11', quantityRequired: 6, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'FEM-002', color: 'BRANCO', size: 'P', address: 'C12', quantityRequired: 5, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'FEM-002', color: 'BRANCO', size: 'M', address: 'C12', quantityRequired: 5, quantityCollected: 0, status: 'PENDENTE' },
    ],
    []
  );

  // CENÁRIO 8 — Caixa masculina linha esporte (pendente)
  await saveBoxIfNotExists(
    { papeletaCode: 'PAP-PENDENTE-003', orderId: 'PED-2002', status: 'EM_COLETA' },
    [
      { reference: 'MASC-010', color: 'AZUL MARINHO', size: 'M',  address: 'D05', quantityRequired: 10, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'MASC-010', color: 'AZUL MARINHO', size: 'G',  address: 'D05', quantityRequired: 10, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'MASC-010', color: 'AZUL MARINHO', size: 'GG', address: 'D06', quantityRequired: 5,  quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'MASC-011', color: 'CINZA',        size: 'M',  address: 'D07', quantityRequired: 8,  quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'MASC-011', color: 'CINZA',        size: 'G',  address: 'D07', quantityRequired: 8,  quantityCollected: 0, status: 'PENDENTE' },
    ],
    []
  );

  // CENÁRIO 9 — Caixa infantil (pendente)
  await saveBoxIfNotExists(
    { papeletaCode: 'PAP-PENDENTE-004', orderId: 'PED-2003', status: 'EM_COLETA' },
    [
      { reference: 'INF-050', color: 'AMARELO', size: '2',  address: 'E01', quantityRequired: 3, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'INF-050', color: 'AMARELO', size: '4',  address: 'E01', quantityRequired: 3, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'INF-050', color: 'AMARELO', size: '6',  address: 'E02', quantityRequired: 3, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'INF-051', color: 'VERDE',   size: '4',  address: 'E03', quantityRequired: 4, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'INF-051', color: 'VERDE',   size: '6',  address: 'E03', quantityRequired: 4, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'INF-051', color: 'VERDE',   size: '8',  address: 'E04', quantityRequired: 2, quantityCollected: 0, status: 'PENDENTE' },
    ],
    []
  );

  // CENÁRIO 10 — Caixa mista urgente (pendente, múltiplos endereços)
  await saveBoxIfNotExists(
    { papeletaCode: 'PAP-URGENTE-001', orderId: 'PED-9001', status: 'EM_COLETA' },
    [
      { reference: 'URG-100', color: 'VERMELHO', size: 'P',  address: 'F01', quantityRequired: 2, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'URG-100', color: 'VERMELHO', size: 'M',  address: 'F01', quantityRequired: 3, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'URG-101', color: 'LARANJA',  size: 'M',  address: 'F02', quantityRequired: 5, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'URG-102', color: 'ROXO',     size: 'G',  address: 'G01', quantityRequired: 4, quantityCollected: 0, status: 'PENDENTE' },
      { reference: 'URG-102', color: 'ROXO',     size: 'GG', address: 'G01', quantityRequired: 2, quantityCollected: 0, status: 'PENDENTE' },
    ],
    []
  );

  // CENÁRIO 11 — Caixa parcialmente coletada (em andamento)
  await saveBoxIfNotExists(
    { papeletaCode: 'PAP-ANDAMENTO-001', orderId: 'PED-3001', status: 'EM_COLETA' },
    [
      { reference: 'AND-200', color: 'BEGE', size: 'P', address: 'H10', quantityRequired: 5, quantityCollected: 3, status: 'PENDENTE' },
      { reference: 'AND-200', color: 'BEGE', size: 'M', address: 'H10', quantityRequired: 5, quantityCollected: 5, status: 'COMPLETO' },
      { reference: 'AND-201', color: 'MARROM', size: 'G', address: 'H11', quantityRequired: 4, quantityCollected: 0, status: 'PENDENTE' },
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
