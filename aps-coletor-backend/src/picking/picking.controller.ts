import { Controller, Post, Get, Body, Param, Query, NotFoundException, UseGuards, Sse, MessageEvent } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth } from '@nestjs/swagger';
import { Observable, timer } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { PickingService } from './picking.service';
import { SyncBoxRequestDto } from './dto/sync-box.dto';
import { SyncBoxResponseDto } from './dto/sync-box-response.dto';
import { DivergencesQueryDto, DivergencesSummaryQueryDto } from './dto/divergences-query.dto';
import { CreateBoxDto } from './dto/create-box.dto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';

@ApiTags('Picking (Sincronização)')
@Controller('api/picking')
export class PickingController {
  constructor(private readonly pickingService: PickingService) {}

  // ── Rotas protegidas por JWT (apenas app coletor autenticado) ──────────────

  @Post('sync-box')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Sincronizar uma caixa fechada ou parcial do coletor' })
  @ApiResponse({ status: 201, description: 'Caixa sincronizada com sucesso', type: SyncBoxResponseDto })
  @ApiResponse({ status: 409, description: 'Caixa já sincronizada' })
  async syncBox(@Body() syncBoxRequestDto: SyncBoxRequestDto) {
    return this.pickingService.syncBox(syncBoxRequestDto);
  }

  @Post('boxes')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Cadastrar nova caixa pendente para coleta (via app ou supervisor)' })
  @ApiResponse({ status: 201, description: 'Caixa criada com sucesso com todos os itens como PENDENTE' })
  @ApiResponse({ status: 409, description: 'Já existe caixa com este código de papeleta' })
  async createBox(@Body() createBoxDto: CreateBoxDto) {
    return this.pickingService.createBox(createBoxDto);
  }

  // ── Rotas públicas (dashboard web — sem autenticação) ──────────────────────

  @Get('health')
  @ApiOperation({ summary: 'Verifica a disponibilidade da API para sincronização' })
  @ApiResponse({ status: 200, description: 'API online' })
  async checkHealth() {
    return { status: 'ok', timestamp: new Date().toISOString() };
  }

  @Get('telemetry')
  @ApiOperation({ summary: 'Painel completo de telemetria operacional (Dashboard)' })
  @ApiResponse({ status: 200, description: 'Dados em tempo real do chão de fábrica' })
  async getTelemetry() {
    return this.pickingService.getTelemetry();
  }

  @Sse('telemetry/stream')
  @ApiOperation({ summary: 'Stream em tempo real da telemetria (SSE)' })
  telemetryStream(): Observable<MessageEvent> {
    return timer(0, 5000).pipe(
      switchMap(async () => {
        const data = await this.pickingService.getTelemetry();
        return { data } as MessageEvent;
      })
    );
  }

  @Get('divergences/summary')
  @ApiOperation({ summary: 'Resumo gerencial de divergências por motivo' })
  @ApiResponse({ status: 200, description: 'Resumo gerencial retornado com sucesso' })
  async getDivergencesSummary(@Query() query: DivergencesSummaryQueryDto) {
    return this.pickingService.getDivergencesSummary(query);
  }

  @Get('divergences')
  @ApiOperation({ summary: 'Lista todas as divergências com filtros opcionais' })
  @ApiResponse({ status: 200, description: 'Lista de divergências retornada com sucesso' })
  async getDivergences(@Query() query: DivergencesQueryDto) {
    return this.pickingService.getDivergences(query);
  }

  // ── Rotas protegidas por JWT (leitura de caixas via app coletor) ───────────

  @Get('boxes/:papeletaCode')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
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

  @Get('boxes/:papeletaCode/full')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Baixar dados completos de uma caixa para iniciar a coleta' })
  @ApiResponse({ status: 200, description: 'Dados da caixa' })
  @ApiResponse({ status: 404, description: 'Caixa não encontrada' })
  async getBox(@Param('papeletaCode') papeletaCode: string) {
    return this.pickingService.getBox(papeletaCode);
  }

  @Get('boxes/:papeletaCode/divergences')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Lista divergências de uma caixa específica' })
  @ApiResponse({ status: 200, description: 'Divergências da caixa retornadas' })
  @ApiResponse({ status: 404, description: 'Caixa não encontrada ou sem divergências' })
  async getDivergencesByBox(@Param('papeletaCode') papeletaCode: string) {
    const divergences = await this.pickingService.getDivergencesByBox(papeletaCode);
    if (!divergences) {
      throw new NotFoundException({ message: `Caixa com papeleta ${papeletaCode} não encontrada` });
    }
    return divergences;
  }
}
