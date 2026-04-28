import { Entity, PrimaryGeneratedColumn, Column, ManyToOne, JoinColumn } from 'typeorm';
import { PickingItem } from './picking-item.entity';

@Entity('scanned_pieces')
export class ScannedPiece {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => PickingItem, item => item.scannedPieces, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'picking_item_id' })
  pickingItem: PickingItem;

  @Column()
  barcode: string;

  @Column({ name: 'scanned_at', type: 'timestamp' })
  scannedAt: Date;
}
