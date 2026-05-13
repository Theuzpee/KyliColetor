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

  async checkIfExists(papeletaCode: string) {
    const existingBox = await this.boxRepository.findOne({
      where: { papeletaCode },
    });

    if (!existingBox) {
      return { exists: false };
    }

    return { exists: true, syncId: existingBox.id };
  }

  async getBox(papeletaCode: string) {
    const box = await this.boxRepository.findOne({
      where: { papeletaCode },
      relations: ['items', 'items.scannedPieces', 'divergences'],
    });

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
}
