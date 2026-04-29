import { IsString, IsNotEmpty, IsEnum, IsArray, ValidateNested, IsInt, Min, IsDate, IsOptional } from 'class-validator';
import { Type } from 'class-transformer';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { SkipReason } from '../enums/skip-reason.enum';

export class ScannedPieceDto {
  @ApiProperty({ description: 'Código de barras bipado', example: '1000079' })
  @IsString()
  @IsNotEmpty()
  barcode: string;

  @ApiProperty({ description: 'Momento exato da bipagem' })
  @IsDate()
  @Type(() => Date)
  scannedAt: Date;
}

export class DivergenceDto {
  @ApiProperty({ enum: SkipReason, description: 'Motivo da divergência' })
  @IsEnum(SkipReason)
  reason: SkipReason;

  @ApiPropertyOptional({ description: 'Código de barras, se houver', example: '1000079' })
  @IsOptional()
  @IsString()
  barcode?: string;

  @ApiProperty({ description: 'Data/Hora do registro' })
  @IsDate()
  @Type(() => Date)
  registeredAt: Date;
}

export class SyncBoxItemDto {
  @ApiProperty({ description: 'Referência da peça', example: 'REF-A' })
  @IsString()
  @IsNotEmpty()
  reference: string;

  @ApiProperty({ example: 'AZUL' })
  @IsString()
  @IsNotEmpty()
  color: string;

  @ApiProperty({ example: 'M' })
  @IsString()
  @IsNotEmpty()
  size: string;

  @ApiProperty({ description: 'Endereço físico no galpão', example: 'C37.09.6B' })
  @IsString()
  @IsNotEmpty()
  address: string;

  @ApiProperty({ description: 'Quantidade requerida para o item', example: 3 })
  @IsInt()
  @Min(1)
  quantityRequired: number;

  @ApiProperty({ description: 'Quantidade efetivamente coletada', example: 3 })
  @IsInt()
  @Min(0)
  quantityCollected: number;

  @ApiProperty({ enum: ['COMPLETO', 'FALTA', 'PENDENTE'], example: 'COMPLETO' })
  @IsEnum(['COMPLETO', 'FALTA', 'PENDENTE'])
  status: string;

  @ApiProperty({ type: [ScannedPieceDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => ScannedPieceDto)
  scannedPieces: ScannedPieceDto[];

  @ApiPropertyOptional({ type: [DivergenceDto], description: 'Divergências registradas' })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => DivergenceDto)
  divergences?: DivergenceDto[];
}

export class SyncBoxRequestDto {
  @ApiProperty({ description: 'Código único da papeleta', example: 'PAP123' })
  @IsString()
  @IsNotEmpty()
  papeletaCode: string;

  @ApiProperty({ description: 'ID do pedido', example: 'PED-999' })
  @IsString()
  @IsNotEmpty()
  orderId: string;

  @ApiProperty({ enum: ['FINALIZADA', 'PARCIAL', 'MULTI_ANDAR'], example: 'FINALIZADA' })
  @IsEnum(['FINALIZADA', 'PARCIAL', 'MULTI_ANDAR'])
  status: string;

  @ApiProperty({ description: 'Momento em que a caixa foi finalizada localmente' })
  @IsDate()
  @Type(() => Date)
  collectedAt: Date;

  @ApiProperty({ type: [SyncBoxItemDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => SyncBoxItemDto)
  items: SyncBoxItemDto[];
}
