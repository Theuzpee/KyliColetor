import { Entity, PrimaryGeneratedColumn, Column, ManyToOne, JoinColumn, OneToMany } from 'typeorm';
import { Box } from './box.entity';
import { ScannedPiece } from './scanned-piece.entity';

@Entity('picking_items')
export class PickingItem {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => Box, box => box.items, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'box_id' })
  box: Box;

  @Column()
  reference: string;

  @Column()
  color: string;

  @Column()
  size: string;

  @Column()
  address: string;

  @Column({ name: 'quantity_required', type: 'int' })
  quantityRequired: number;

  @Column({ name: 'quantity_collected', type: 'int' })
  quantityCollected: number;

  @Column({ type: 'enum', enum: ['COMPLETO', 'FALTA', 'PENDENTE'] })
  status: string;

  @OneToMany(() => ScannedPiece, piece => piece.pickingItem, { cascade: true })
  scannedPieces: ScannedPiece[];
}
