import { Test, TestingModule } from '@nestjs/testing';
import { PickingService } from './picking.service';
import { getRepositoryToken } from '@nestjs/typeorm';
import { Box } from './entities/box.entity';
import { DivergenceEntity } from './entities/divergence.entity';
import { ConflictException } from '@nestjs/common';
import { SyncBoxRequestDto } from './dto/sync-box.dto';

describe('PickingService', () => {
  let service: PickingService;

  const mockBoxRepository = {
    findOne: jest.fn(),
    save: jest.fn(),
  };

  const mockDivergenceRepository = {
    createQueryBuilder: jest.fn(),
    getMany: jest.fn(),
  };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        PickingService,
        { provide: getRepositoryToken(Box), useValue: mockBoxRepository },
        { provide: getRepositoryToken(DivergenceEntity), useValue: mockDivergenceRepository },
      ],
    }).compile();

    service = module.get<PickingService>(PickingService);
    jest.clearAllMocks();
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  describe('syncBox', () => {
    const mockRequestDto: SyncBoxRequestDto = {
      papeletaCode: 'PAP-001',
      orderId: 'PED-123',
      status: 'FINALIZADA',
      collectedAt: new Date().toISOString(),
      items: [],
    };

    it('deve retornar 409 Conflict se a caixa já foi sincronizada (idempotência)', async () => {
      // Mock para dizer que a caixa já foi encontrada
      mockBoxRepository.findOne.mockResolvedValue({ id: 'existing-id', papeletaCode: 'PAP-001' });

      await expect(service.syncBox(mockRequestDto))
        .rejects
        .toThrow(ConflictException);
      
      expect(mockBoxRepository.save).not.toHaveBeenCalled();
    });

    it('deve salvar a caixa se ela for nova (não existe)', async () => {
      // Mock que diz que a caixa NÃO existe
      mockBoxRepository.findOne.mockResolvedValue(null);
      
      const mockSavedBox = { id: 'new-id', syncedAt: new Date() };
      mockBoxRepository.save.mockResolvedValue(mockSavedBox);

      const result = await service.syncBox(mockRequestDto);

      expect(mockBoxRepository.save).toHaveBeenCalled();
      expect(result.syncId).toBe('new-id');
    });
  });
});
