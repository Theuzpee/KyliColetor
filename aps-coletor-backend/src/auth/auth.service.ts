import { Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';

@Injectable()
export class AuthService {
  constructor(private readonly jwtService: JwtService) {}

  async login(supervisorCode: string, operatorCode: string) {
    // Por enquanto, aceita qualquer código válido.
    // Futuramente, validar contra banco de dados.
    if (!supervisorCode || !operatorCode) {
      throw new Error('Códigos inválidos');
    }

    const payload = { operator: operatorCode, sub: supervisorCode };
    return {
      token: this.jwtService.sign(payload),
      operatorName: `Operador ${operatorCode}`,
    };
  }
}
