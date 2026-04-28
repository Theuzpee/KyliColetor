import { Controller, Post, Body, UnauthorizedException } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBody } from '@nestjs/swagger';
import { AuthService } from './auth.service';

@ApiTags('Autenticação')
@Controller('api/auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('login')
  @ApiOperation({ summary: 'Login de Operador/Supervisor' })
  @ApiBody({ schema: { example: { supervisorCode: '123', operatorCode: '456' } } })
  @ApiResponse({ status: 201, description: 'Retorna o Token JWT' })
  @ApiResponse({ status: 401, description: 'Credenciais inválidas' })
  async login(@Body() body: { supervisorCode: string; operatorCode: string }) {
    try {
      return await this.authService.login(body.supervisorCode, body.operatorCode);
    } catch (error) {
      throw new UnauthorizedException('Credenciais inválidas');
    }
  }
}
