import { IsString, IsNotEmpty } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class LoginDto {
  @ApiProperty({ description: 'Código bipado do supervisor', example: 'SUP-TURNO1' })
  @IsString()
  @IsNotEmpty()
  supervisorBarcode: string;

  @ApiProperty({ description: 'Código bipado do colaborador', example: 'OP-001' })
  @IsString()
  @IsNotEmpty()
  operatorBarcode: string;
}
