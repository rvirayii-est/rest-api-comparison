import { Injectable } from '@nestjs/common';
import { UsersRepository } from './users.repository';

@Injectable()
export class UsersService {
  constructor(private repo: UsersRepository) {}
  create(u: { email: string; passwordHash: string; roles: string[] }) { return this.repo.create(u); }
  findByEmail(email: string) { return this.repo.findByEmail(email); }
  findById(id: string) { return this.repo.findById(id); }
  makeAdmin(id: string) { return this.repo.update(id, { roles: ['USER', 'ADMIN'] }); }
}
