import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, IsNull } from 'typeorm';
import { RefreshToken } from './entities/refresh-token.entity';

@Injectable()
export class TokensService {
  constructor(@InjectRepository(RefreshToken) private repo: Repository<RefreshToken>) {}

  async store(userId: string, tokenHash: string) {
    const rec = this.repo.create({ userId, tokenHash });
    await this.repo.save(rec);
  }

  async verify(tokenHash: string) {
    const rec = await this.repo.findOne({ where: { tokenHash, revokedAt: IsNull() } });
    return !!rec;
  }

  async revoke(tokenHash: string) {
    await this.repo.update({ tokenHash }, { revokedAt: new Date() });
  }
}
