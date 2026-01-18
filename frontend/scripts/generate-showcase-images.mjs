/**
 * Script para generar imagenes optimizadas para la seccion Responsive Images
 * del Style Guide. Genera multiples tamanos y formatos (AVIF, WebP, JPG).
 *
 * Uso: node scripts/generate-showcase-images.mjs
 * Requiere: npm install sharp --save-dev
 */

import sharp from 'sharp';
import { mkdir } from 'fs/promises';
import { join, basename, extname } from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const SIZES = [400, 800, 1200];
const INPUT_DIR = join(__dirname, '../docs/design/screenshots/style-guide');
const OUTPUT_DIR = join(__dirname, '../src/assets/images/showcase');

// Imagenes seleccionadas para showcase
const SELECTED_IMAGES = [
  'website-style-guide-colors.png',
  'website-style-guide-buttons-and-alerts.png'
];

async function generateImages() {
  console.log('Creating output directory...');
  await mkdir(OUTPUT_DIR, { recursive: true });

  for (const filename of SELECTED_IMAGES) {
    const inputPath = join(INPUT_DIR, filename);
    const baseName = basename(filename, extname(filename))
      .replace('website-', '');

    console.log(`\nProcessing: ${filename}`);

    for (const width of SIZES) {
      const image = sharp(inputPath).resize(width, null, { withoutEnlargement: true });

      // AVIF - mejor compresion
      const avifPath = join(OUTPUT_DIR, `${baseName}-${width}w.avif`);
      await image.clone().avif({ quality: 75 }).toFile(avifPath);
      console.log(`  Created: ${baseName}-${width}w.avif`);

      // WebP - amplio soporte
      const webpPath = join(OUTPUT_DIR, `${baseName}-${width}w.webp`);
      await image.clone().webp({ quality: 80 }).toFile(webpPath);
      console.log(`  Created: ${baseName}-${width}w.webp`);

      // JPG - fallback universal
      const jpgPath = join(OUTPUT_DIR, `${baseName}-${width}w.jpg`);
      await image.clone().jpeg({ quality: 85 }).toFile(jpgPath);
      console.log(`  Created: ${baseName}-${width}w.jpg`);
    }

    console.log(`Done with ${filename}`);
  }

  console.log('\nAll images generated successfully!');
  console.log(`Output directory: ${OUTPUT_DIR}`);
}

generateImages().catch((err) => {
  console.error('Error generating images:', err);
  process.exit(1);
});
