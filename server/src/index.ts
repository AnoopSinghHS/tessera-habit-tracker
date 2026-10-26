import { createApp } from './app.js';

const PORT = process.env.PORT ? parseInt(process.env.PORT, 10) : 4000;
const app = createApp();

app.listen(PORT, '0.0.0.0', () => {
  console.log(`[Tessera Engine] Sync & Auth Server active on http://0.0.0.0:${PORT}`);
  console.log(`[Tessera Engine] Health check: http://localhost:${PORT}/api/health`);
});
