import {
  IsString, IsNotEmpty, IsArray, ValidateNested,
  IsInt, Min, IsOptional, IsEnum
} from 'class-validator';
import { Type } from 'class-transformer';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class CreateBoxItemDto {
  @ApiProperty({ description: 'Referência da peça', example: 'REF-A' })
  @IsString()
  @IsNotEmpty()
  reference: string;

  @ApiProperty({ description: 'Cor da peça', example: 'AZUL' })
  @IsString()
  @IsNotEmpty()
  color: string;

  @ApiProperty({ description: 'Tamanho da peça', example: 'M' })
  @IsString()
  @IsNotEmpty()
  size: string;

  @ApiProperty({ description: 'Endereço físico no galpão', example: 'C37.09.6B' })
  @IsString()
  @IsNotEmpty()
  address: string;

  @ApiProperty({ description: 'Quantidade necessária para coletar', example: 3 })
  @IsInt()
  @Min(1)
  quantityRequired: number;
}

export class CreateBoxDto {
  @ApiProperty({ description: 'Código único da papeleta', example: 'PAP-NOVO-001' })
  @IsString()
  @IsNotEmpty()
  papeletaCode: string;

  @ApiProperty({ description: 'ID do pedido vinculado', example: 'PED-5000' })
  @IsString()
  @IsNotEmpty()
  orderId: string;

  @ApiPropertyOptional({ description: 'Nome do operador responsável', example: 'João Silva' })
  @IsOptional()
  @IsString()
  operator?: string;

  @ApiProperty({ type: [CreateBoxItemDto], description: 'Lista de itens a coletar' })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => CreateBoxItemDto)
  items: CreateBoxItemDto[];
}
