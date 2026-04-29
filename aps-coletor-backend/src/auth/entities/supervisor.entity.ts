import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn } from 'typeorm';

@Entity('supervisors')
export class Supervisor {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ unique: true })
  barcode: string;

  @Column()
  name: string;

  @Column({ type: 'enum', enum: ['TURNO_1', 'TURNO_2', 'TURNO_3'] })
  shift: 'TURNO_1' | 'TURNO_2' | 'TURNO_3';

  @Column({ default: true })
  active: boolean;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;
}
