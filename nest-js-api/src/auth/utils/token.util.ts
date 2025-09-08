import * as crypto from 'crypto';

export function signAccessToken(payload: object) {
  const jwt = require('jsonwebtoken');
  const privateKey = process.env.JWT_PRIVATE_KEY?.replace(/\\n/g, '\n');
  const ttl = Number(process.env.ACCESS_TOKEN_TTL || 900);
  return jwt.sign(payload, privateKey, { algorithm: 'RS256', expiresIn: ttl });
}

export function signRefreshToken(payload: object) {
  const jwt = require('jsonwebtoken');
  const privateKey = process.env.JWT_PRIVATE_KEY?.replace(/\\n/g, '\n');
  const ttl = Number(process.env.REFRESH_TOKEN_TTL || 604800);
  return jwt.sign(payload, privateKey, { algorithm: 'RS256', expiresIn: ttl });
}

export function hashToken(token: string) {
  return crypto.createHash('sha256').update(token).digest('hex');
}
