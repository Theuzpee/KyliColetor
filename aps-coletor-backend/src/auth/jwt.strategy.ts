import { ExtractJwt, Strategy } from 'passport-jwt';
import { PassportStrategy } from '@nestjs/passport';
import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor(configService: ConfigService) {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      secretOrKey: configService.get<string>('JWT_SECRET') ?? 'fallback_secret',
    });
  }

  async validate(payload: any) {
    return {
      sub: payload.sub,
      operatorCode: payload.operatorCode,
      operatorName: payload.operatorName,
      supervisorCode: payload.supervisorCode,
      supervisorName: payload.supervisorName,
      shift: payload.shift,
    };
  }
}
