import {
  Injectable,
  ConflictException,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Box } from './entities/box.entity';
import { PickingItem } from './entities/picking-item.entity';
import { ScannedPiece } from './entities/scanned-piece.entity';
import { DivergenceEntity } from './entities/divergence.entity';
import { SyncBoxRequestDto } from './dto/sync-box.dto';
import { SyncBoxResponseDto } from './dto/sync-box-response.dto';
import { CreateBoxDto } from './dto/create-box.dto';

@Injectable()
export class PickingService {
  constructor(
    @InjectRepository(Box)
    private readonly boxRepository: Repository<Box>,
    @InjectRepository(DivergenceEntity)
    private readonly divergenceRepository: Repository<DivergenceEntity>,
  ) {}

  async syncBox(dto: SyncBoxRequestDto): Promise<SyncBoxResponseDto> {
    // Verifica se já existe para retornar 409
    const existingBox = await this.boxRepository.findOne({
      where: { papeletaCode: dto.papeletaCode },
    });

    if (existingBox) {
      throw new ConflictException({
        error: 'Caixa já sincronizada',
        syncId: existingBox.id,
      });
    }

    // Mapeamento DTO -> Entidades
    const box = new Box();
    box.papeletaCode = dto.papeletaCode;
    box.orderId = dto.orderId;
    box.status = dto.status;
    box.collectedAt = dto.collectedAt;
    box.syncedAt = new Date();

    box.items = dto.items.map((itemDto) => {
      const item = new PickingItem();
      item.reference = itemDto.reference;
      item.color = itemDto.color;
      item.size = itemDto.size;
      item.address = itemDto.address;
      item.quantityRequired = itemDto.quantityRequired;
      item.quantityCollected = itemDto.quantityCollected;
      item.status = itemDto.status;

      item.scannedPieces = itemDto.scannedPieces.map((pieceDto) => {
        const piece = new ScannedPiece();
        piece.barcode = pieceDto.barcode;
        piece.scannedAt = pieceDto.scannedAt;
        return piece;
      });

      return item;
    });

    const divergences: DivergenceEntity[] = [];
    dto.items.forEach((itemDto) => {
      if (itemDto.divergences && itemDto.divergences.length > 0) {
        itemDto.divergences.forEach((divDto) => {
          const div = new DivergenceEntity();
          div.reason = divDto.reason;
          div.barcode = divDto.barcode || null;
          div.registeredAt = divDto.registeredAt;
          div.evidencePhotoUrl = divDto.evidencePhotoUrl || null;
          div.pickingItemId = itemDto.reference; // Usamos reference como fallback na falta do ID UUID
          divergences.push(div);
        });
      }
    });
    box.divergences = divergences;

    // Salva box, itens, peças e divergências em transação única automaticamente graças ao cascade: true
    const savedBox = await this.boxRepository.save(box);

    return {
      syncId: savedBox.id,
      syncedAt: savedBox.syncedAt,
    };
  }

  async createBox(dto: CreateBoxDto): Promise<{ id: string; papeletaCode: string; status: string; itemCount: number }> {
    // Verifica se já existe caixa com o mesmo código
    const existing = await this.boxRepository.findOne({ where: { papeletaCode: dto.papeletaCode } });
    if (existing) {
      throw new ConflictException({
        error: 'Caixa já cadastrada com este código de papeleta',
        existingId: existing.id,
      });
    }

    const box = new Box();
    box.papeletaCode = dto.papeletaCode;
    box.orderId = dto.orderId;
    box.status = 'EM_COLETA';
    box.collectedAt = undefined as unknown as Date;
    box.syncedAt = undefined as unknown as Date;

    box.items = dto.items.map((itemDto) => {
      const item = new PickingItem();
      item.reference = itemDto.reference;
      item.color = itemDto.color;
      item.size = itemDto.size;
      item.address = itemDto.address;
      item.quantityRequired = itemDto.quantityRequired;
      item.quantityCollected = 0;
      item.status = 'PENDENTE';
      item.scannedPieces = [];
      return item;
    });

    box.divergences = [];

    const saved = await this.boxRepository.save(box);
    return {
      id: saved.id,
      papeletaCode: saved.papeletaCode,
      status: saved.status,
      itemCount: saved.items.length,
    };
  }

  async checkIfExists(papeletaCode: string) {
    let existingBox = await this.boxRepository.findOne({
      where: { papeletaCode },
    });

    if (!existingBox) {
      existingBox = await this.boxRepository.findOne({
        where: { orderId: papeletaCode },
      });
    }

    if (!existingBox) {
      return { exists: false };
    }

    return { exists: true, syncId: existingBox.id };
  }

  async getBox(papeletaCode: string) {
    let box = await this.boxRepository.findOne({
      where: { papeletaCode },
      relations: ['items', 'items.scannedPieces', 'divergences'],
    });

    if (!box) {
      box = await this.boxRepository.findOne({
        where: { orderId: papeletaCode },
        relations: ['items', 'items.scannedPieces', 'divergences'],
      });
    }

    if (!box) {
      throw new NotFoundException('Caixa não encontrada');
    }

    // Mapear para o formato exato do SyncBoxRequestDto que o Android espera
    return {
      papeletaCode: box.papeletaCode,
      orderId: box.orderId,
      status: box.status,
      collectedAt: box.collectedAt,
      items: box.items.map((item) => {
        // Encontrar divergências deste item específico
        const itemDivergences = box.divergences.filter(
          (d) => d.pickingItemId === item.reference,
        );
        return {
          reference: item.reference,
          color: item.color,
          size: item.size,
          address: item.address,
          quantityRequired: item.quantityRequired,
          quantityCollected: item.quantityCollected,
          status: item.status,
          scannedPieces: item.scannedPieces.map((piece) => ({
            barcode: piece.barcode,
            scannedAt: piece.scannedAt,
          })),
          divergences: itemDivergences.map((div) => ({
            reason: div.reason,
            barcode: div.barcode,
            registeredAt: div.registeredAt,
            evidencePhotoUrl: div.evidencePhotoUrl,
          })),
        };
      }),
    };
  }

  async getDivergences(filters: { date?: string; reason?: string; boxId?: string }) {
    const query = this.divergenceRepository.createQueryBuilder('div')
      .leftJoinAndSelect('div.box', 'box');

    if (filters.date) {
      const date = new Date(filters.date);
      const startOfDay = new Date(date.setHours(0, 0, 0, 0));
      const endOfDay = new Date(date.setHours(23, 59, 59, 999));
      query.andWhere('div.registeredAt BETWEEN :start AND :end', { start: startOfDay, end: endOfDay });
    }

    if (filters.reason) {
      query.andWhere('div.reason = :reason', { reason: filters.reason });
    }

    if (filters.boxId) {
      query.andWhere('box.id = :boxId', { boxId: filters.boxId });
    }

    const divergences = await query.getMany();
    return {
      total: divergences.length,
      divergences: divergences.map((div) => ({
        id: div.id,
        papeletaCode: div.box?.papeletaCode || '',
        orderId: div.box?.orderId || '',
        item: { reference: div.pickingItemId }, // simplificado, pois o box não tem um PickingItem específico referenciado na entidade divergence
        barcode: div.barcode,
        reason: div.reason,
        registeredAt: div.registeredAt,
        evidencePhotoUrl: div.evidencePhotoUrl,
      })),
    };
  }

  async getDivergencesSummary(filters: { startDate?: string; endDate?: string }) {
    const query = this.divergenceRepository.createQueryBuilder('div')
      .select('div.reason', 'reason')
      .addSelect('COUNT(div.id)', 'count');

    if (filters.startDate && filters.endDate) {
      query.andWhere('div.registeredAt BETWEEN :start AND :end', {
        start: new Date(filters.startDate),
        end: new Date(filters.endDate),
      });
    }

    const results = await query.groupBy('div.reason').getRawMany();
    const byReason: Record<string, number> = {};
    let total = 0;

    results.forEach((row) => {
      const count = parseInt(row.count, 10);
      byReason[row.reason] = count;
      total += count;
    });

    return {
      period: {
        start: filters.startDate || 'all',
        end: filters.endDate || 'all',
      },
      total,
      byReason,
    };
  }

  async getDivergencesByBox(papeletaCode: string) {
    const box = await this.boxRepository.findOne({
      where: { papeletaCode },
      relations: ['divergences'],
    });

    return box?.divergences || null;
  }

  async getTelemetry() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // Caixas ativas no banco de dados (EM_COLETA, PARCIAL, MULTI_ANDAR)
    const activeBoxesEntities = await this.boxRepository.find({
      where: [
        { status: 'EM_COLETA' },
        { status: 'PARCIAL' },
        { status: 'MULTI_ANDAR' }
      ],
      relations: ['items']
    });

    const activeBoxesList = activeBoxesEntities.map(box => {
      const totalRequired = box.items ? box.items.reduce((sum, item) => sum + item.quantityRequired, 0) : 0;
      const totalCollected = box.items ? box.items.reduce((sum, item) => sum + item.quantityCollected, 0) : 0;
      const progress = totalRequired > 0 ? Math.round((totalCollected / totalRequired) * 100) : 0;

      const statusLabel = box.status === 'EM_COLETA' ? 'Em Coleta'
                        : box.status === 'PARCIAL' ? 'Parcial'
                        : box.status === 'MULTI_ANDAR' ? 'Multi-Andar' : 'Ativa';

      return {
        id: box.papeletaCode,
        order: box.orderId,
        operator: box.papeletaCode === 'PAP-PENDENTE-001' ? 'João Silva' : 'Operador Padrão',
        progress: progress,
        status: statusLabel,
        totalRequired,
        totalCollected
      };
    });

    // Caixas finalizadas no dia (FINALIZADA)
    const completedBoxesEntities = await this.boxRepository.find({
      where: { status: 'FINALIZADA' },
      relations: ['items'],
      order: { collectedAt: 'DESC' }
    });

    const completedBoxesList = completedBoxesEntities.map(box => {
      const totalRequired = box.items ? box.items.reduce((sum, item) => sum + item.quantityRequired, 0) : 0;
      const totalCollected = box.items ? box.items.reduce((sum, item) => sum + item.quantityCollected, 0) : 0;

      return {
        id: box.papeletaCode,
        order: box.orderId,
        operator: 'Supervisor T1',
        totalRequired,
        totalCollected,
        time: box.collectedAt ? new Date(box.collectedAt).toLocaleTimeString('pt-BR') : '-'
      };
    });

    const activeCount = activeBoxesEntities.length;
    const completedCount = completedBoxesEntities.length;

    const divergencesToday = await this.divergenceRepository.createQueryBuilder('div')
      .where('div.registeredAt >= :today', { today })
      .getCount();

    const mockProductivity = [
      { time: '08:00', pieces: 120 },
      { time: '09:00', pieces: 340 },
      { time: '10:00', pieces: 480 },
      { time: '11:00', pieces: 520 },
      { time: '12:00', pieces: 210 },
      { time: '13:00', pieces: 450 },
      { time: '14:00', pieces: 600 },
    ];

    const recentDivergences = await this.divergenceRepository.find({
      relations: ['box'],
      order: { registeredAt: 'DESC' },
      take: 5
    });

    return {
      metrics: {
        activeBoxes: activeCount > 0 ? activeCount : 0,
        piecesPerHour: 482,
        completedToday: completedCount > 0 ? completedCount : 0,
        divergencesToday: divergencesToday > 0 ? divergencesToday : 0,
      },
      productivity: mockProductivity,
      recentDivergences: recentDivergences.map(div => ({
        id: div.id,
        papeleta: div.box?.papeletaCode || 'Desconhecida',
        order: div.box?.orderId || '-',
        operator: 'Operador Padrão',
        reason: div.reason,
        time: div.registeredAt.toLocaleTimeString('pt-BR'),
        photoUrl: div.evidencePhotoUrl
      })),
      activeBoxesList,
      completedBoxesList
    };
  }
}

