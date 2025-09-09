import * as crypto from 'crypto';
import * as jwt from 'jsonwebtoken';

export function signAccessToken(payload: object) {
  const privateKey = process.env.JWT_PRIVATE_KEY?.replace(/\\n/g, '\n');
  if (!privateKey) {
    throw new Error('JWT_PRIVATE_KEY is not configured');
  }
  const ttl = Number(process.env.ACCESS_TOKEN_TTL || 900);
  return jwt.sign(payload, privateKey, { algorithm: 'RS256', expiresIn: ttl });
}

export function signRefreshToken(payload: object) {
  const privateKey = process.env.JWT_PRIVATE_KEY?.replace(/\\n/g, '\n');
  if (!privateKey) {
    throw new Error('JWT_PRIVATE_KEY is not configured');
  }
  const ttl = Number(process.env.REFRESH_TOKEN_TTL || 604800);
  return jwt.sign(payload, privateKey, { algorithm: 'RS256', expiresIn: ttl });
}

export function hashToken(token: string) {
  return crypto.createHash('sha256').update(token).digest('hex');
}
