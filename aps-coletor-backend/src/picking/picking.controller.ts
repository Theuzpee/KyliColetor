import { Controller, Post, Get, Body, Param, NotFoundException, UseGuards } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth } from '@nestjs/swagger';
import { PickingService } from './picking.service';
import { SyncBoxRequestDto } from './dto/sync-box.dto';
import { SyncBoxResponseDto } from './dto/sync-box-response.dto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';

@ApiTags('Picking (Sincronização)')
@ApiBearerAuth()
@Controller('api/picking')
@UseGuards(JwtAuthGuard)
export class PickingController {
  constructor(private readonly pickingService: PickingService) {}

  @Post('sync-box')
  @ApiOperation({ summary: 'Sincronizar uma caixa fechada ou parcial do coletor' })
  @ApiResponse({ status: 201, description: 'Caixa sincronizada com sucesso', type: SyncBoxResponseDto })
  @ApiResponse({ status: 409, description: 'Caixa já sincronizada' })
  async syncBox(@Body() syncBoxRequestDto: SyncBoxRequestDto) {
    return this.pickingService.syncBox(syncBoxRequestDto);
  }

  @Get('boxes/:papeletaCode')
  @ApiOperation({ summary: 'Verificar se uma papeleta já foi processada' })
  @ApiResponse({ status: 200, description: 'Status da papeleta' })
  @ApiResponse({ status: 404, description: 'Papeleta não encontrada' })
  async checkBoxExists(@Param('papeletaCode') papeletaCode: string) {
    const result = await this.pickingService.checkIfExists(papeletaCode);
    if (!result.exists) {
      throw new NotFoundException({ exists: false });
    }
    return result;
  }
}
