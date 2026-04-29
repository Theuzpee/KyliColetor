import request from 'supertest';
import { app, dataSource } from './setup';
import { Supervisor } from '../../src/auth/entities/supervisor.entity';
import { Operator } from '../../src/auth/entities/operator.entity';

describe('AuthController (e2e)', () => {
  beforeAll(async () => {
    // Seed para os testes de auth
    const supervisorRepo = dataSource.getRepository(Supervisor);
    const operatorRepo = dataSource.getRepository(Operator);

    await supervisorRepo.save({ barcode: 'SUP-TURNO1', name: 'Sup 1', shift: 'TURNO_1', active: true });
    await supervisorRepo.save({ barcode: 'SUP-INVALIDO', name: 'Sup Inválido', shift: 'TURNO_1', active: false });

    await operatorRepo.save({ barcode: 'OP-001', employeeCode: 'EMP001', name: 'Operador 1', active: true });
    await operatorRepo.save({ barcode: 'OP-002', employeeCode: 'EMP002', name: 'Operador 2', active: false });
  });

  afterAll(async () => {
    await dataSource.query('TRUNCATE TABLE supervisors, operators, erp_sync_logs CASCADE');
  });

  describe('/api/auth/login (POST)', () => {
    it('TESTE 1 — Login com sucesso retorna JWT', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/auth/login')
        .send({ supervisorBarcode: 'SUP-TURNO1', operatorBarcode: 'OP-001' })
        .expect(201);

      expect(response.body.token).toBeDefined();
      expect(response.body.shift).toBe('TURNO_1');
    });

    it('TESTE 2 — Login com supervisor inválido retorna 401', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/auth/login')
        .send({ supervisorBarcode: 'SUP-INVALIDO', operatorBarcode: 'OP-001' })
        .expect(401);

      expect(response.body.message.toLowerCase()).toContain('supervisor');
    });

    it('TESTE 3 — Login com operador inativo retorna 401', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/auth/login')
        .send({ supervisorBarcode: 'SUP-TURNO1', operatorBarcode: 'OP-002' })
        .expect(401);

      expect(response.body.message.toLowerCase()).toContain('inativo');
    });

    it('TESTE 4 — Rate limit bloqueia após 10 tentativas', async () => {
      for (let i = 0; i < 10; i++) {
        await request(app.getHttpServer())
          .post('/api/auth/login')
          .send({ supervisorBarcode: 'SUP-TURNO1', operatorBarcode: 'OP-001' })
          .expect(201);
      }

      const response = await request(app.getHttpServer())
        .post('/api/auth/login')
        .send({ supervisorBarcode: 'SUP-TURNO1', operatorBarcode: 'OP-001' })
        .expect(429);
    });
  });

  describe('/api/auth/erp-sync (POST)', () => {
    it('TESTE 5 — ERP sync com API Key válida retorna sucesso', async () => {
      const response = await request(app.getHttpServer())
        .post('/api/auth/erp-sync')
        .set('X-Api-Key', process.env.ERP_API_KEY || '')
        .send({
          supervisors: [{ barcode: 'SUP-NEW', name: 'Sup New', shift: 'TURNO_2' }],
          operators: [{ barcode: 'OP-NEW', employeeCode: 'EMPNEW', name: 'Op New' }]
        })
        .expect(201);

      expect(response.body.status).toBe('SUCCESS');
    });

    it('TESTE 6 — ERP sync sem API Key retorna 401', async () => {
      await request(app.getHttpServer())
        .post('/api/auth/erp-sync')
        .send({ supervisors: [], operators: [] })
        .expect(401);
    });
  });

  describe('/api/auth/me (GET)', () => {
    let token: string;

    beforeAll(async () => {
      const response = await request(app.getHttpServer())
        .post('/api/auth/login')
        .send({ supervisorBarcode: 'SUP-TURNO1', operatorBarcode: 'OP-001' });
      token = response.body.token;
    });

    it('TESTE 7 — GET /api/auth/me com token válido retorna perfil', async () => {
      const response = await request(app.getHttpServer())
        .get('/api/auth/me')
        .set('Authorization', `Bearer ${token}`)
        .expect(200);

      expect(response.body.operatorCode).toBeDefined();
    });

    it('TESTE 8 — GET /api/auth/me sem token retorna 401', async () => {
      await request(app.getHttpServer())
        .get('/api/auth/me')
        .expect(401);
    });
  });
});
