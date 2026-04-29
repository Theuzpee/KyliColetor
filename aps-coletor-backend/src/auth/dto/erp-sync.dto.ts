import { IsString, IsNotEmpty, IsEnum, IsArray, ValidateNested } from 'class-validator';
import { Type } from 'class-transformer';
import { ApiProperty } from '@nestjs/swagger';

export class ErpSupervisorDto {
  @ApiProperty({ description: 'Código do crachá do supervisor' })
  @IsString()
  @IsNotEmpty()
  barcode: string;

  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  name: string;

  @ApiProperty({ enum: ['TURNO_1', 'TURNO_2', 'TURNO_3'] })
  @IsEnum(['TURNO_1', 'TURNO_2', 'TURNO_3'])
  shift: 'TURNO_1' | 'TURNO_2' | 'TURNO_3';
}

export class ErpOperatorDto {
  @ApiProperty({ description: 'Código do crachá do colaborador' })
  @IsString()
  @IsNotEmpty()
  barcode: string;

  @ApiProperty({ description: 'Código interno no ERP/RH' })
  @IsString()
  @IsNotEmpty()
  employeeCode: string;

  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  name: string;
}

export class ErpSyncDto {
  @ApiProperty({ type: [ErpSupervisorDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => ErpSupervisorDto)
  supervisors: ErpSupervisorDto[];

  @ApiProperty({ type: [ErpOperatorDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => ErpOperatorDto)
  operators: ErpOperatorDto[];
}
