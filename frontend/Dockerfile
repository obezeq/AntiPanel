# ========================================
# Multi-Stage Dockerfile for Angular 21 + Bun
# Node.js 24 LTS (Krypton) + Best Practices
# ========================================

# ========================================
# Stage 1: Base - Node.js 24 LTS Alpine
# ========================================
FROM node:24-alpine AS base

# Install system dependencies (bash, curl, unzip required for Bun)
RUN apk add --no-cache bash curl unzip

# Install Bun (ultra-fast package manager & runtime)
RUN curl -fsSL https://bun.sh/install | bash && \
    ln -s /root/.bun/bin/bun /usr/local/bin/bun

# Verify installations
RUN node --version && bun --version

# Set working directory
WORKDIR /app

# ========================================
# Stage 2: Dependencies - Install packages
# ========================================
FROM base AS dependencies

# Copy package files
COPY package.json bun.lockb* ./

# Install dependencies with Bun (5-10x faster than npm)
RUN bun install --frozen-lockfile

# ========================================
# Stage 3: Development - Hot Reload
# ========================================
FROM base AS development

# Copy installed dependencies from previous stage
COPY --from=dependencies /app/node_modules ./node_modules

# Copy all project files
COPY . .

# Expose Angular dev server port
EXPOSE 4200

# Development command with hot reload
# --host 0.0.0.0: Allow external connections (required for Docker)
# --poll 2000: Enable polling for file changes (Docker volume compatibility)
# --disable-host-check: Allow connections from any host
CMD ["bun", "run", "start"]

# ========================================
# Stage 4: Builder - Production Build
# ========================================
FROM base AS builder

# Copy dependencies
COPY --from=dependencies /app/node_modules ./node_modules

# Copy source code
COPY . .

# Build for production
RUN bun run build --configuration production

# ========================================
# Stage 5: Production - Nginx Server
# ========================================
FROM nginx:1.27-alpine AS production

# Copy custom nginx config (future)
# COPY nginx.conf /etc/nginx/nginx.conf

# Copy built files from builder stage
COPY --from=builder /app/dist/antipanel-frontend/browser /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
