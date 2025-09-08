import { Column, Entity, Index, PrimaryGeneratedColumn } from 'typeorm';

@Entity('refresh_tokens')
export class RefreshToken {
  @PrimaryGeneratedColumn('uuid') id!: string;
  @Index() @Column() userId!: string;
  @Index() @Column({ unique: true, name: 'token_hash' }) tokenHash!: string;
  @Column({ type: 'timestamptz', default: () => 'CURRENT_TIMESTAMP' }) createdAt!: Date;
  @Column({ type: 'timestamptz', nullable: true }) revokedAt!: Date | null;
}
