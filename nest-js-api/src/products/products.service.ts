import { Injectable } from '@nestjs/common';
@Injectable()
export class ProductsService {
  list() { return [
    { id: 'p1', name: 'Widget', price: 4999 },
    { id: 'p2', name: 'Gadget', price: 7999 },
  ]; }
}
