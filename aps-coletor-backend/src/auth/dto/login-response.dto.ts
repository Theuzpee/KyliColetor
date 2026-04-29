import { ApiProperty } from '@nestjs/swagger';

export class LoginResponseDto {
  @ApiProperty({ description: 'JWT com expiração de 8h' })
  token: string;

  @ApiProperty({ description: 'Nome do colaborador' })
  operatorName: string;

  @ApiProperty({ description: 'Código do ERP do colaborador' })
  operatorCode: string;

  @ApiProperty({ description: 'Nome do supervisor' })
  supervisorName: string;

  @ApiProperty({ description: 'Turno do supervisor', enum: ['TURNO_1', 'TURNO_2', 'TURNO_3'] })
  shift: string;

  @ApiProperty({ description: 'Data/hora de expiração do token (ISO8601)' })
  expiresAt: string;
}
