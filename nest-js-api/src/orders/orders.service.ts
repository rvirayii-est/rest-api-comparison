import { Injectable } from '@nestjs/common';
@Injectable()
export class OrdersService {
  listAll() { return [ { id: 'o1', userId: 'u1', productId: 'p1', status: 'PAID' } ]; }
  listByUser(userId: string) { return [ { id: 'oX', userId, productId: 'p2', status: 'PAID' } ]; }
}
