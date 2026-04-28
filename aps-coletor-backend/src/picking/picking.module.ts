import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PickingService } from './picking.service';
import { PickingController } from './picking.controller';
import { Box } from './entities/box.entity';
import { PickingItem } from './entities/picking-item.entity';
import { ScannedPiece } from './entities/scanned-piece.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Box, PickingItem, ScannedPiece])],
  controllers: [PickingController],
  providers: [PickingService],
})
export class PickingModule {}
