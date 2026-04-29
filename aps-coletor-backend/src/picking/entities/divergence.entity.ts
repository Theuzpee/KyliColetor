import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, ManyToOne, JoinColumn } from 'typeorm';
import { Box } from './box.entity';
import { SkipReason } from '../enums/skip-reason.enum';

@Entity('divergences')
export class DivergenceEntity {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ManyToOne(() => Box, box => box.divergences, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'box_id' })
  box: Box;

  // We save the ID or item reference. Since the Android payload doesn't easily expose pickingItemId as a stable UUID
  // in the sync logic (we only have item reference/color/size), we might just store the reference string or pickingItemId.
  // Wait, the prompt says "pickingItemId: uuid (referência ao item)" but Android's pickingItemId is Long.
  // Let's use a simple string for now to match the payload or keep it as string if it comes as string.
  @Column({ name: 'picking_item_id', type: 'varchar', nullable: true })
  pickingItemId: string;

  @Column({ type: 'varchar', nullable: true })
  barcode: string | null;

  @Column({ type: 'enum', enum: SkipReason })
  reason: SkipReason;

  @Column({ type: 'timestamp' })
  registeredAt: Date;

  @CreateDateColumn()
  createdAt: Date;
}
