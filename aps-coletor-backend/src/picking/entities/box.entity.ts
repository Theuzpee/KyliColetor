import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, OneToMany } from 'typeorm';
import { PickingItem } from './picking-item.entity';
import { DivergenceEntity } from './divergence.entity';

@Entity('boxes')
export class Box {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'papeleta_code', unique: true })
  papeletaCode: string;

  @Column({ name: 'order_id' })
  orderId: string;

  @Column({ type: 'enum', enum: ['FINALIZADA', 'PARCIAL', 'MULTI_ANDAR', 'EM_COLETA'] })
  status: string;

  @Column({ name: 'collected_at', type: 'timestamp' })
  collectedAt: Date;

  @Column({ name: 'synced_at', type: 'timestamp', nullable: true })
  syncedAt: Date;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @OneToMany(() => PickingItem, item => item.box, { cascade: true })
  items: PickingItem[];

  @OneToMany(() => DivergenceEntity, div => div.box, { cascade: true })
  divergences: DivergenceEntity[];
}
