import { CanActivate, ExecutionContext, Injectable, UnauthorizedException } from '@nestjs/common';

@Injectable()
export class ApiKeyGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest();
    const apiKey = request.headers['x-api-key'];

    if (!apiKey) {
      throw new UnauthorizedException('API Key não fornecida.');
    }

    if (apiKey !== process.env.ERP_API_KEY) {
      throw new UnauthorizedException('API Key inválida.');
    }

    return true;
  }
}
