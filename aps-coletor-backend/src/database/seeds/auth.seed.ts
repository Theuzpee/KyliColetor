import { NestFactory } from '@nestjs/core';
import { AppModule } from '../../app.module';
import { Supervisor } from '../../auth/entities/supervisor.entity';
import { Operator } from '../../auth/entities/operator.entity';
import { DataSource } from 'typeorm';

async function bootstrap() {
  const app = await NestFactory.createApplicationContext(AppModule);
  const dataSource = app.get(DataSource);

  const supervisorRepo = dataSource.getRepository(Supervisor);
  const operatorRepo = dataSource.getRepository(Operator);

  console.log('🌱 Iniciando o seed de Auth...');

  const supervisors = [
    { barcode: 'SUP-TURNO1', name: 'Supervisor Turno 1', shift: 'TURNO_1' as const },
    { barcode: 'SUP-TURNO2', name: 'Supervisor Turno 2', shift: 'TURNO_2' as const },
    { barcode: 'SUP-TURNO3', name: 'Supervisor Turno 3', shift: 'TURNO_3' as const },
  ];

  for (const sup of supervisors) {
    const exists = await supervisorRepo.findOne({ where: { barcode: sup.barcode } });
    if (!exists) {
      await supervisorRepo.save(supervisorRepo.create(sup));
      console.log(`✅ Supervisor inserido: ${sup.name}`);
    } else {
      console.log(`⚡ Supervisor já existe: ${sup.name}`);
    }
  }

  const operators = [
    { barcode: 'OP-001', employeeCode: 'EMP001', name: 'João Silva' },
    { barcode: 'OP-002', employeeCode: 'EMP002', name: 'Maria Santos' },
    { barcode: 'OP-003', employeeCode: 'EMP003', name: 'Pedro Oliveira' },
  ];

  for (const op of operators) {
    const exists = await operatorRepo.findOne({ where: { barcode: op.barcode } });
    if (!exists) {
      await operatorRepo.save(operatorRepo.create(op));
      console.log(`✅ Operador inserido: ${op.name}`);
    } else {
      console.log(`⚡ Operador já existe: ${op.name}`);
    }
  }

  console.log('✅ Seed finalizado com sucesso!');
  await app.close();
}

bootstrap().catch(err => {
  console.error('❌ Erro no seed:', err);
  process.exit(1);
});
