import { Injectable } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import { ExtractJwt, Strategy } from 'passport-jwt';

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor() {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      secretOrKey: process.env.JWT_PUBLIC_KEY?.replace(/\\n/g, '\n'),
      algorithms: ['RS256'],
    });
  }
  async validate(payload: { sub: string; email: string; roles: string[] }) {
    return payload; // attach to req.user
  }
}
