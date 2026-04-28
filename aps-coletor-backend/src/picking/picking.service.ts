import { Injectable, ConflictException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Box } from './entities/box.entity';
import { PickingItem } from './entities/picking-item.entity';
import { ScannedPiece } from './entities/scanned-piece.entity';
import { SyncBoxRequestDto } from './dto/sync-box.dto';
import { SyncBoxResponseDto } from './dto/sync-box-response.dto';

@Injectable()
export class PickingService {
  constructor(
    @InjectRepository(Box)
    private readonly boxRepository: Repository<Box>,
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

    // Salva box, itens e peças em transação única automaticamente graças ao cascade: true
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
}
