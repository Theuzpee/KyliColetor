import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, DataSource } from 'typeorm';
import { Supervisor } from './entities/supervisor.entity';
import { Operator } from './entities/operator.entity';
import { ErpSyncLog } from './entities/erp-sync-log.entity';
import { LoginDto } from './dto/login.dto';
import { LoginResponseDto } from './dto/login-response.dto';
import { ErpSyncDto } from './dto/erp-sync.dto';

@Injectable()
export class AuthService {
  constructor(
    private readonly jwtService: JwtService,
    @InjectRepository(Supervisor)
    private readonly supervisorRepository: Repository<Supervisor>,
    @InjectRepository(Operator)
    private readonly operatorRepository: Repository<Operator>,
    @InjectRepository(ErpSyncLog)
    private readonly erpSyncLogRepository: Repository<ErpSyncLog>,
    private readonly dataSource: DataSource,
  ) {}

  async login(loginDto: LoginDto): Promise<LoginResponseDto> {
    const { supervisorBarcode, operatorBarcode } = loginDto;

    const supervisor = await this.supervisorRepository.findOne({ where: { barcode: supervisorBarcode } });
    if (!supervisor) {
      throw new UnauthorizedException('Código do supervisor não reconhecido.');
    }
    if (!supervisor.active) {
      throw new UnauthorizedException('Supervisor inativo. Contate o RH.');
    }

    const operator = await this.operatorRepository.findOne({ where: { barcode: operatorBarcode } });
    if (!operator) {
      throw new UnauthorizedException('Código do colaborador não reconhecido.');
    }
    if (!operator.active) {
      throw new UnauthorizedException('Colaborador inativo. Contate o RH.');
    }

    const now = Math.floor(Date.now() / 1000);
    // JWT Expiration set to 8h
    const exp = now + 8 * 60 * 60;

    const payload = {
      sub: operator.id,
      operatorCode: operator.employeeCode,
      operatorName: operator.name,
      supervisorCode: supervisor.barcode,
      supervisorName: supervisor.name,
      shift: supervisor.shift,
      iat: now,
      exp: exp,
    };

    const token = this.jwtService.sign(payload);

    return {
      token,
      operatorName: operator.name,
      operatorCode: operator.employeeCode,
      supervisorName: supervisor.name,
      shift: supervisor.shift,
      expiresAt: new Date(exp * 1000).toISOString(),
    };
  }

  async syncFromErp(data: ErpSyncDto): Promise<ErpSyncLog> {
    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
      // 1. Process supervisors
      const supervisorBarcodes = data.supervisors.map(s => s.barcode);
      for (const sup of data.supervisors) {
        let existing = await queryRunner.manager.findOne(Supervisor, { where: { barcode: sup.barcode } });
        if (existing) {
          existing.name = sup.name;
          existing.shift = sup.shift;
          existing.active = true;
        } else {
          existing = queryRunner.manager.create(Supervisor, {
            barcode: sup.barcode,
            name: sup.name,
            shift: sup.shift,
            active: true,
          });
        }
        await queryRunner.manager.save(Supervisor, existing);
      }

      if (supervisorBarcodes.length > 0) {
        await queryRunner.manager.createQueryBuilder()
          .update(Supervisor)
          .set({ active: false })
          .where('barcode NOT IN (:...barcodes)', { barcodes: supervisorBarcodes })
          .execute();
      } else {
         await queryRunner.manager.createQueryBuilder().update(Supervisor).set({ active: false }).execute();
      }

      // 2. Process operators
      const operatorBarcodes = data.operators.map(o => o.barcode);
      for (const op of data.operators) {
        let existing = await queryRunner.manager.findOne(Operator, { where: { barcode: op.barcode } });
        if (existing) {
          existing.employeeCode = op.employeeCode;
          existing.name = op.name;
          existing.active = true;
        } else {
          existing = queryRunner.manager.create(Operator, {
            barcode: op.barcode,
            employeeCode: op.employeeCode,
            name: op.name,
            active: true,
          });
        }
        await queryRunner.manager.save(Operator, existing);
      }

      if (operatorBarcodes.length > 0) {
        await queryRunner.manager.createQueryBuilder()
          .update(Operator)
          .set({ active: false })
          .where('barcode NOT IN (:...barcodes)', { barcodes: operatorBarcodes })
          .execute();
      } else {
         await queryRunner.manager.createQueryBuilder().update(Operator).set({ active: false }).execute();
      }

      // 3. Save sync log
      const log = queryRunner.manager.create(ErpSyncLog, {
        totalSupervisors: data.supervisors.length,
        totalOperators: data.operators.length,
        status: 'SUCCESS',
      });
      const savedLog = await queryRunner.manager.save(ErpSyncLog, log);

      await queryRunner.commitTransaction();
      return savedLog;
    } catch (err) {
      await queryRunner.rollbackTransaction();
      
      const failedLog = this.erpSyncLogRepository.create({
        totalSupervisors: data.supervisors?.length || 0,
        totalOperators: data.operators?.length || 0,
        status: 'FAILED',
        errorMessage: err instanceof Error ? err.message : String(err),
      });
      await this.erpSyncLogRepository.save(failedLog);

      throw err;
    } finally {
      await queryRunner.release();
    }
  }

  validateToken(payload: any) {
    return {
      userId: payload.sub,
      operatorCode: payload.operatorCode,
      operatorName: payload.operatorName,
      supervisorCode: payload.supervisorCode,
      supervisorName: payload.supervisorName,
      shift: payload.shift,
    };
  }
}
