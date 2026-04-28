import { ApiProperty } from '@nestjs/swagger';

export class SyncBoxResponseDto {
  @ApiProperty({ description: 'UUID gerado no servidor', example: '123e4567-e89b-12d3-a456-426614174000' })
  syncId: string;

  @ApiProperty({ description: 'Data e hora da sincronização bem sucedida' })
  syncedAt: Date;
}
