import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn } from 'typeorm';

@Entity('erp_sync_logs')
export class ErpSyncLog {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @CreateDateColumn({ name: 'synced_at', type: 'timestamp' })
  syncedAt: Date;

  @Column({ name: 'total_supervisors', type: 'int' })
  totalSupervisors: number;

  @Column({ name: 'total_operators', type: 'int' })
  totalOperators: number;

  @Column({ type: 'enum', enum: ['SUCCESS', 'PARTIAL', 'FAILED'] })
  status: 'SUCCESS' | 'PARTIAL' | 'FAILED';

  @Column({ name: 'error_message', type: 'varchar', nullable: true })
  errorMessage: string | null;

  @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
  createdAt: Date;
}
