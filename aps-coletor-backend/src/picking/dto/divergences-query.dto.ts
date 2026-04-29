import { IsOptional, IsString, IsISO8601, IsEnum } from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';
import { SkipReason } from '../enums/skip-reason.enum';

export class DivergencesQueryDto {
  @ApiPropertyOptional({ description: 'Data para filtro (ISO8601)' })
  @IsOptional()
  @IsISO8601()
  date?: string;

  @ApiPropertyOptional({ enum: SkipReason, description: 'Motivo da divergência' })
  @IsOptional()
  @IsEnum(SkipReason)
  reason?: SkipReason;

  @ApiPropertyOptional({ description: 'ID da caixa (Box)' })
  @IsOptional()
  @IsString()
  boxId?: string;
}

export class DivergencesSummaryQueryDto {
  @ApiPropertyOptional({ description: 'Data inicial para filtro (ISO8601)' })
  @IsOptional()
  @IsISO8601()
  startDate?: string;

  @ApiPropertyOptional({ description: 'Data final para filtro (ISO8601)' })
  @IsOptional()
  @IsISO8601()
  endDate?: string;
}
