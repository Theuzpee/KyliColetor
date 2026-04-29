import { Controller, Post, Get, Body, UseGuards, Request } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth, ApiSecurity } from '@nestjs/swagger';
import { ThrottlerGuard } from '@nestjs/throttler';
import { AuthService } from './auth.service';
import { LoginDto } from './dto/login.dto';
import { LoginResponseDto } from './dto/login-response.dto';
import { ErpSyncDto } from './dto/erp-sync.dto';
import { JwtAuthGuard } from './jwt-auth.guard';
import { ApiKeyGuard } from './guards/api-key.guard';

@ApiTags('Autenticação')
@Controller('api/auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @UseGuards(ThrottlerGuard)
  @Post('login')
  @ApiOperation({ summary: 'Login de operador (com código de supervisor)' })
  @ApiResponse({ status: 201, description: 'Login bem-sucedido', type: LoginResponseDto })
  @ApiResponse({ status: 401, description: 'Códigos inválidos ou inativos' })
  @ApiResponse({ status: 429, description: 'Muitas tentativas (Rate Limit)' })
  async login(@Body() loginDto: LoginDto): Promise<LoginResponseDto> {
    return this.authService.login(loginDto);
  }

  @UseGuards(ApiKeyGuard)
  @ApiSecurity('x-api-key')
  @Post('erp-sync')
  @ApiOperation({ summary: 'Sincronizar cadastro do ERP (apenas backend-to-backend)' })
  @ApiResponse({ status: 201, description: 'Sincronização efetuada' })
  @ApiResponse({ status: 401, description: 'API Key inválida' })
  async erpSync(@Body() erpSyncDto: ErpSyncDto) {
    return this.authService.syncFromErp(erpSyncDto);
  }

  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @Get('me')
  @ApiOperation({ summary: 'Verificar token atual e retornar dados do operador' })
  @ApiResponse({ status: 200, description: 'Dados do token extraídos' })
  @ApiResponse({ status: 401, description: 'Token inválido/expirado' })
  getProfile(@Request() req: any) {
    return req.user;
  }
}
