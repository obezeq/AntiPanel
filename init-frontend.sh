#!/bin/bash
#
# init-frontend.sh
# Inicializa el proyecto Angular 21 usando Docker puro
#

set -e  # Exit on error

echo "=================================================="
echo "  AntiPanel - Inicialización Frontend con Docker"
echo "=================================================="
echo ""

# Variables
PROJECT_DIR="./frontend"
CONTAINER_NAME="antipanel-angular-init"
NODE_VERSION="24-alpine"  # Node.js 24 LTS (Krypton)
ANGULAR_VERSION="21"      # Angular 21 (latest stable)

# Verificar que Docker esté corriendo
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker no está corriendo"
    exit 1
fi

echo "✅ Docker está corriendo"
echo ""

# Verificar si el proyecto ya existe
if [ -f "$PROJECT_DIR/package.json" ]; then
    echo "⚠️  El proyecto ya existe en $PROJECT_DIR"
    read -p "¿Deseas eliminarlo y crear uno nuevo? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Operación cancelada"
        exit 1
    fi
    echo "🗑️  Eliminando proyecto existente..."
    rm -rf "$PROJECT_DIR"/{*,.*} 2>/dev/null || true
fi

# Crear directorio si no existe
mkdir -p "$PROJECT_DIR"

echo "📦 Usando Node.js $NODE_VERSION"
echo "📦 Instalando Angular CLI $ANGULAR_VERSION"
echo ""

# Paso 1: Crear el proyecto Angular con Docker
echo "🔨 Paso 1/3: Creando proyecto Angular 21..."
echo ""

docker run --rm -it \
    --name "$CONTAINER_NAME" \
    -v "$(pwd)/$PROJECT_DIR:/workspace" \
    -w /workspace \
    node:$NODE_VERSION \
    sh -c "
        set -e

        echo '📦 Instalando dependencias del sistema...'
        apk add --no-cache bash curl unzip

        echo '📦 Instalando Bun...'
        curl -fsSL https://bun.sh/install | bash
        export PATH=\"\$HOME/.bun/bin:\$PATH\"

        echo '📦 Verificando instalaciones...'
        node --version
        bun --version

        echo '📦 Instalando Angular CLI $ANGULAR_VERSION...'
        npm install -g @angular/cli@$ANGULAR_VERSION

        echo '🚀 Creando proyecto Angular...'
        ng new antipanel-frontend \
            --directory=. \
            --package-manager=bun \
            --routing=true \
            --style=scss \
            --skip-git=true \
            --ssr=false \
            --standalone=true

        echo '✅ Proyecto creado exitosamente'
    "

echo ""
echo "✅ Paso 1/3 completado"
echo ""

# Paso 2: Configurar package.json para Docker
echo "🔧 Paso 2/3: Configurando package.json para Docker..."

# Backup del package.json original
cp "$PROJECT_DIR/package.json" "$PROJECT_DIR/package.json.backup"

# Modificar el script "start" usando sed
sed -i 's/"start": "ng serve"/"start": "ng serve --host 0.0.0.0 --poll 2000 --disable-host-check"/' "$PROJECT_DIR/package.json"

echo "✅ Paso 2/3 completado"
echo ""

# Paso 3: Crear archivo .dockerignore si no existe
echo "📝 Paso 3/3: Verificando .dockerignore..."

if [ ! -f "$PROJECT_DIR/.dockerignore" ]; then
    echo "Creando .dockerignore..."
    cat > "$PROJECT_DIR/.dockerignore" << 'EOF'
# Node
node_modules/
npm-debug.log*
yarn-debug.log*
yarn-error.log*
bun.lockb

# Angular
.angular/
dist/
tmp/
out-tsc/

# IDE
.idea/
.vscode/
*.swp
*.swo
*~

# OS
.DS_Store
Thumbs.db

# Tests
coverage/
.nyc_output/

# Misc
*.log
.env
.env.local
EOF
fi

echo "✅ Paso 3/3 completado"
echo ""

# Resumen final
echo "=================================================="
echo "  ✅ Inicialización Completada"
echo "=================================================="
echo ""
echo "📁 Proyecto creado en: $PROJECT_DIR/"
echo ""
echo "🚀 Siguiente paso - Levantar los servicios:"
echo ""
echo "   docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build"
echo ""
echo "🌐 URLs:"
echo "   - Frontend: http://localhost:4200"
echo "   - Backend:  http://localhost:8080/api"
echo "   - Swagger:  http://localhost:8080/swagger-ui.html"
echo "   - pgAdmin:  http://localhost:5050"
echo ""
echo "=================================================="
