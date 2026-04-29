import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn } from 'typeorm';

@Entity('erp_sync_logs')
export class ErpSyncLog {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @CreateDateColumn({ name: 'synced_at' })
  syncedAt: Date;

  @Column({ name: 'total_supervisors' })
  totalSupervisors: number;

  @Column({ name: 'total_operators' })
  totalOperators: number;

  @Column({ type: 'enum', enum: ['SUCCESS', 'PARTIAL', 'FAILED'] })
  status: 'SUCCESS' | 'PARTIAL' | 'FAILED';

  @Column({ name: 'error_message', nullable: true })
  errorMessage: string | null;
}
