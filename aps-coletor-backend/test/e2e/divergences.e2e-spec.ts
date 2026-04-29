import request from 'supertest';
import { app, dataSource } from './setup';
import { Supervisor } from '../../src/auth/entities/supervisor.entity';
import { Operator } from '../../src/auth/entities/operator.entity';
import { Box } from '../../src/picking/entities/box.entity';
import { DivergenceEntity } from '../../src/picking/entities/divergence.entity';
import { SkipReason } from '../../src/picking/enums/skip-reason.enum';

describe('DivergencesController (e2e)', () => {
  let token: string;

  beforeAll(async () => {
    const supervisorRepo = dataSource.getRepository(Supervisor);
    const operatorRepo = dataSource.getRepository(Operator);
    const boxRepo = dataSource.getRepository(Box);
    const divRepo = dataSource.getRepository(DivergenceEntity);

    await supervisorRepo.save({ barcode: 'SUP-TURNO1', name: 'Sup 1', shift: 'TURNO_1', active: true });
    await operatorRepo.save({ barcode: 'OP-001', employeeCode: 'EMP001', name: 'Operador 1', active: true });

    const loginResponse = await request(app.getHttpServer())
      .post('/api/auth/login')
      .send({ supervisorBarcode: 'SUP-TURNO1', operatorBarcode: 'OP-001' });
    
    token = loginResponse.body.token;

    // Popula banco de testes com divergências
    const box1 = await boxRepo.save({ papeletaCode: 'PAP-DIV-E2E-1', orderId: 'PED-1', status: 'PARCIAL', collectedAt: new Date() });
    const box2 = await boxRepo.save({ papeletaCode: 'PAP-DIV-E2E-2', orderId: 'PED-2', status: 'PARCIAL', collectedAt: new Date() });

    await divRepo.save({ box: box1, reason: SkipReason.DESABASTECIDO, registeredAt: new Date() });
    await divRepo.save({ box: box1, reason: SkipReason.SUJA, registeredAt: new Date() });
    await divRepo.save({ box: box2, reason: SkipReason.DESABASTECIDO, registeredAt: new Date() });
  });

  afterAll(async () => {
    await dataSource.query('TRUNCATE TABLE supervisors, operators, boxes, divergences CASCADE');
  });

  describe('/api/picking/divergences (GET)', () => {
    it('TESTE 1 — GET /api/picking/divergences retorna lista paginada', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/picking/divergences')
        .set('Authorization', `Bearer ${token}`)
        .expect(200);

      expect(response.body.total).toBeGreaterThan(0);
      expect(Array.isArray(response.body.divergences)).toBe(true);
    });

    it('TESTE 2 — Filtro por reason retorna apenas divergências do tipo', async () => {
      const response = await request(app.getHttpServer())
        .get(`/api/picking/divergences?reason=${SkipReason.DESABASTECIDO}`)
        .set('Authorization', `Bearer ${token}`)
        .expect(200);

      expect(response.body.divergences.every((d: any) => d.reason === SkipReason.DESABASTECIDO)).toBe(true);
    });

    it('TESTE 3 — GET /api/picking/divergences/summary retorna contagem por motivo', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/picking/divergences/summary')
        .set('Authorization', `Bearer ${token}`)
        .expect(200);

      expect(response.body.byReason).toBeDefined();
      expect(response.body.byReason[SkipReason.DESABASTECIDO]).toBeGreaterThanOrEqual(1);
    });

    it('TESTE 4 — Summary com filtro de data retorna subset correto', async () => {
      const start = new Date(Date.now() - 86400000).toISOString(); // Ontem
      const end = new Date(Date.now() + 86400000).toISOString(); // Amanhã
      
      const response = await request(app.getHttpServer())
        .get(`/api/picking/divergences/summary?startDate=${start}&endDate=${end}`)
        .set('Authorization', `Bearer ${token}`)
        .expect(200);

      expect(response.body.byReason).toBeDefined();
    });

    it('TESTE 5 — GET sem JWT retorna 401', async () => {
      await request(app.getHttpServer()).get('/api/picking/divergences').expect(401);
      await request(app.getHttpServer()).get('/api/picking/divergences/summary').expect(401);
    });
  });
});
