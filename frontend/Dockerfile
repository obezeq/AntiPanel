# ========================================
# Dockerfile for Angular 21 + Bun (Development)
# ========================================
FROM oven/bun:1-alpine

# Set working directory
WORKDIR /app

# Copy package files for dependency installation
COPY package.json bun.lockb* ./

# Install dependencies using Bun (fast!)
RUN bun install

# Copy source code
COPY . .

# Expose Angular dev server port
EXPOSE 4200

# Run Angular dev server with hot reload
# --host 0.0.0.0: Allow external connections (required for Docker)
# --poll 2000: Enable polling for file changes (Docker volume compatibility)
CMD ["bun", "run", "start", "--", "--host", "0.0.0.0", "--poll", "2000"]
