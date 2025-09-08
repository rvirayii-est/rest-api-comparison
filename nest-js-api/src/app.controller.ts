import { Controller, Get } from '@nestjs/common';

@Controller()
export class AppController {
  @Get('/')
  health() {
    return { ok: true, ts: new Date().toISOString() };
  }
}
