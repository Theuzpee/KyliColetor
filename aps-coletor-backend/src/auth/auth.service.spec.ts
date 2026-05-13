import { Test, TestingModule } from '@nestjs/testing';
import { AuthService } from './auth.service';
import { JwtService } from '@nestjs/jwt';
import { getRepositoryToken } from '@nestjs/typeorm';
import { Supervisor } from './entities/supervisor.entity';
import { Operator } from './entities/operator.entity';
import { ErpSyncLog } from './entities/erp-sync-log.entity';
import { DataSource } from 'typeorm';
import { UnauthorizedException } from '@nestjs/common';

describe('AuthService', () => {
  let service: AuthService;

  const mockSupervisorRepository = {
    findOne: jest.fn(),
  };

  const mockOperatorRepository = {
    findOne: jest.fn(),
  };

  const mockErpSyncLogRepository = {
    create: jest.fn(),
    save: jest.fn(),
  };

  const mockJwtService = {
    sign: jest.fn(() => 'mock-jwt-token'),
  };

  const mockDataSource = {
    createQueryRunner: jest.fn(),
  };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        AuthService,
        { provide: JwtService, useValue: mockJwtService },
        { provide: getRepositoryToken(Supervisor), useValue: mockSupervisorRepository },
        { provide: getRepositoryToken(Operator), useValue: mockOperatorRepository },
        { provide: getRepositoryToken(ErpSyncLog), useValue: mockErpSyncLogRepository },
        { provide: DataSource, useValue: mockDataSource },
      ],
    }).compile();

    service = module.get<AuthService>(AuthService);
    
    // Clear mocks between tests
    jest.clearAllMocks();
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  describe('login', () => {
    it('deve retornar um token e dados do operador se as credenciais forem válidas e ativas', async () => {
      mockSupervisorRepository.findOne.mockResolvedValue({
        id: 1, barcode: 'SUP-001', name: 'Super', shift: '1º TURNO', active: true,
      });
      mockOperatorRepository.findOne.mockResolvedValue({
        id: 1, barcode: 'OP-001', employeeCode: '1001', name: 'Operador 1', active: true,
      });

      const result = await service.login({ supervisorBarcode: 'SUP-001', operatorBarcode: 'OP-001' });

      expect(result.token).toBe('mock-jwt-token');
      expect(result.operatorName).toBe('Operador 1');
      expect(result.supervisorName).toBe('Super');
    });

    it('deve lançar UnauthorizedException se o supervisor não existir', async () => {
      mockSupervisorRepository.findOne.mockResolvedValue(null);

      await expect(service.login({ supervisorBarcode: 'SUP-INV', operatorBarcode: 'OP-001' }))
        .rejects
        .toThrow(UnauthorizedException);
    });

    it('deve lançar UnauthorizedException se o operador não existir', async () => {
      mockSupervisorRepository.findOne.mockResolvedValue({
        id: 1, barcode: 'SUP-001', name: 'Super', shift: '1º TURNO', active: true,
      });
      mockOperatorRepository.findOne.mockResolvedValue(null);

      await expect(service.login({ supervisorBarcode: 'SUP-001', operatorBarcode: 'OP-INV' }))
        .rejects
        .toThrow(UnauthorizedException);
    });

    it('deve lançar UnauthorizedException se o supervisor estiver inativo', async () => {
      mockSupervisorRepository.findOne.mockResolvedValue({
        id: 1, barcode: 'SUP-001', name: 'Super', shift: '1º TURNO', active: false, // inativo!
      });

      await expect(service.login({ supervisorBarcode: 'SUP-001', operatorBarcode: 'OP-001' }))
        .rejects
        .toThrow(UnauthorizedException);
    });
  });
});
