import { Injectable, UnauthorizedException } from '@nestjs/common';
import * as bcrypt from 'bcrypt';
import { UsersService } from '../users/users.service';
import { TokensService } from '../tokens/tokens.service';
import { signAccessToken, signRefreshToken, hashToken } from './utils/token.util';
import { Response } from 'express';

@Injectable()
export class AuthService {
  constructor(private users: UsersService, private tokens: TokensService) {}

  async register(email: string, password: string) {
    const hash = await bcrypt.hash(password, 12);
    await this.users.create({ email, passwordHash: hash, roles: ['USER'] });
  }

  async login(email: string, password: string) {
    const user = await this.users.findByEmail(email);
    if (!user) throw new UnauthorizedException('Invalid credentials');
    const ok = await bcrypt.compare(password, user.passwordHash);
    if (!ok) throw new UnauthorizedException('Invalid credentials');

    const payload = { sub: user.id, email: user.email, roles: user.roles };
    const accessToken = signAccessToken(payload);
    const refreshToken = signRefreshToken({ sub: user.id });

    await this.tokens.store(user.id, hashToken(refreshToken));
    return { accessToken, refreshToken };
  }

  async rotateRefresh(refreshToken?: string) {
    if (!refreshToken) throw new UnauthorizedException('No refresh token');
    const valid = await this.tokens.verify(hashToken(refreshToken));
    if (!valid) throw new UnauthorizedException('Invalid refresh token');

    // extract sub (user id) from provided refresh token
    const jwt = require('jsonwebtoken');
    const pub = process.env.JWT_PUBLIC_KEY?.replace(/\\n/g, '\n');
    if (!pub) {
      throw new UnauthorizedException('JWT_PUBLIC_KEY is not configured');
    }
    const { sub } = jwt.verify(refreshToken, pub, { algorithms: ['RS256'] }) as any;

    // rotate: revoke old, issue new
    await this.tokens.revoke(hashToken(refreshToken));
    const user = await this.users.findById(String(sub));
    if (!user) throw new UnauthorizedException('User not found');
    
    const payload = { sub: user.id, email: user.email, roles: user.roles };
    const accessToken = signAccessToken(payload);
    const newRefresh = signRefreshToken({ sub: user.id });
    await this.tokens.store(user.id, hashToken(newRefresh));

    return { accessToken, refreshToken: newRefresh };
  }

  async revoke(refreshToken?: string) {
    if (!refreshToken) return;
    await this.tokens.revoke(hashToken(refreshToken));
  }

  setRefreshCookie(res: Response, token: string) {
    const name = process.env.REFRESH_COOKIE_NAME || 'refresh';
    res.cookie(name, token, {
      httpOnly: true,
      secure: process.env.COOKIE_SECURE === 'true',
      sameSite: (process.env.SAMESITE as any) || 'strict',
      domain: process.env.COOKIE_DOMAIN || undefined,
      path: '/auth',
      maxAge: Number(process.env.REFRESH_TOKEN_TTL || 604800) * 1000,
    });
  }

  clearRefreshCookie(res: Response) {
    const name = process.env.REFRESH_COOKIE_NAME || 'refresh';
    res.clearCookie(name, { path: '/auth' });
  }
}
