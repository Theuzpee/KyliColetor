import request from 'supertest';
import { app, dataSource } from './setup';
import { Supervisor } from '../../src/auth/entities/supervisor.entity';
import { Operator } from '../../src/auth/entities/operator.entity';
import { SkipReason } from '../../src/picking/enums/skip-reason.enum';

describe('PickingController (e2e)', () => {
  let token: string;

  beforeAll(async () => {
    const supervisorRepo = dataSource.getRepository(Supervisor);
    const operatorRepo = dataSource.getRepository(Operator);

    await supervisorRepo.save({ barcode: 'SUP-TURNO1', name: 'Sup 1', shift: 'TURNO_1', active: true });
    await operatorRepo.save({ barcode: 'OP-001', employeeCode: 'EMP001', name: 'Operador 1', active: true });

    const response = await request(app.getHttpServer())
      .post('/api/auth/login')
      .send({ supervisorBarcode: 'SUP-TURNO1', operatorBarcode: 'OP-001' });

    token = response.body.token;
  });

  afterAll(async () => {
    await dataSource.query('TRUNCATE TABLE supervisors, operators, boxes, picking_items, scanned_pieces, divergences CASCADE');
  });

  describe('/api/picking/sync-box (POST)', () => {
    it('TESTE 1 — Sync de caixa nova retorna 201 com syncId', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/picking/sync-box')
        .set('Authorization', `Bearer ${token}`)
        .send({
          papeletaCode: 'PAP-SYNC-001',
          orderId: 'PED-100',
          status: 'FINALIZADA',
          collectedAt: new Date().toISOString(),
          items: [
            {
              reference: 'REF-1',
              color: 'AZUL',
              size: 'M',
              address: 'A01',
              quantityRequired: 5,
              quantityCollected: 5,
              status: 'COMPLETO',
              scannedPieces: [
                { barcode: 'PC-1', scannedAt: new Date().toISOString() }
              ],
              divergences: []
            }
          ]
        })
        .expect(201);

      expect(response.body.syncId).toBeDefined();
      expect(response.body.syncedAt).toBeDefined();
    });

    it('TESTE 2 — Sync de caixa duplicada retorna 409 (idempotência)', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/picking/sync-box')
        .set('Authorization', `Bearer ${token}`)
        .send({
          papeletaCode: 'PAP-SYNC-001', // Mesmo de cima
          orderId: 'PED-100',
          status: 'FINALIZADA',
          collectedAt: new Date().toISOString(),
          items: []
        })
        .expect(409);

      expect(response.body.error).toContain('já sincronizada');
    });

    it('TESTE 3 — Sync sem JWT retorna 401', async () => {
      await request(app.getHttpServer())
        .post('/api/picking/sync-box')
        .send({
          papeletaCode: 'PAP-TEST-401',
          orderId: 'PED-100',
          status: 'FINALIZADA',
          collectedAt: new Date().toISOString(),
          items: []
        })
        .expect(401);
    });

    it('TESTE 4 — Sync com payload inválido retorna 400', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/picking/sync-box')
        .set('Authorization', `Bearer ${token}`)
        .send({
          papeletaCode: 'PAP-TEST-400',
          // Sem orderId
          status: 'FINALIZADA',
          collectedAt: new Date().toISOString(),
          items: []
        })
        .expect(400);

      expect(Array.isArray(response.body.message)).toBe(true);
    });

    it('TESTE 5 — Sync de caixa com divergências salva corretamente', async () => {
      await request(app.getHttpServer())
        .post('/api/picking/sync-box')
        .set('Authorization', `Bearer ${token}`)
        .send({
          papeletaCode: 'PAP-DIV-001',
          orderId: 'PED-101',
          status: 'PARCIAL',
          collectedAt: new Date().toISOString(),
          items: [
            {
              reference: 'REF-DIV',
              color: 'PRETO',
              size: 'P',
              address: 'B02',
              quantityRequired: 5,
              quantityCollected: 4,
              status: 'FALTA',
              scannedPieces: [],
              divergences: [
                {
                  reason: SkipReason.DESABASTECIDO,
                  registeredAt: new Date().toISOString()
                }
              ]
            }
          ]
        })
        .expect(201);

      const response = await request(app.getHttpServer())
        .get('/api/picking/boxes/PAP-DIV-001/divergences')
        .set('Authorization', `Bearer ${token}`)
        .expect(200);

      expect(Array.isArray(response.body)).toBe(true);
      expect(response.body[0].reason).toBe(SkipReason.DESABASTECIDO);
    });
  });

  describe('/api/picking/boxes/:papeletaCode (GET)', () => {
    it('TESTE 6 — GET retorna exists: true', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/picking/boxes/PAP-SYNC-001')
        .set('Authorization', `Bearer ${token}`)
        .expect(200);

      expect(response.body.exists).toBe(true);
    });

    it('TESTE 7 — GET inexistente retorna 404', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/picking/boxes/PAP-NAO-EXISTE')
        .set('Authorization', `Bearer ${token}`)
        .expect(404);

      expect(response.body.exists).toBe(false);
    });
  });
});
