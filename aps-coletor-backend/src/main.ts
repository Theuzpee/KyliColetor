import { NestFactory } from '@nestjs/core';
import { ValidationPipe } from '@nestjs/common';
import { SwaggerModule, DocumentBuilder } from '@nestjs/swagger';
import { AppModule } from './app.module';
import helmet from 'helmet';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // Segurança básica
  app.use(helmet());

  // Habilitar CORS
  app.enableCors();

  // Validação global de DTOs
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true, // Remove propriedades que não estão no DTO
      forbidNonWhitelisted: true, // Retorna erro se enviar propriedades extras
      transform: true, // Transforma os payloads em instâncias de DTO
    }),
  );

  // Configuração do Swagger
  const config = new DocumentBuilder()
    .setTitle('APS Coletor API')
    .setDescription('API de Sincronização do Coletor Android do Grupo Kyly')
    .setVersion('1.0')
    .addBearerAuth()
    .build();
  const documentFactory = () => SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('api/docs', app, documentFactory);

  await app.listen(process.env.PORT ?? 3000);
}
bootstrap();
