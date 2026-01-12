# AntiPanel Frontend

> SMM Panel con diseno minimalista y UX/UI de alta calidad.

## URL de Produccion

**https://antipanel.tech**

---

## Documentacion

- **[Documentacion de Diseno](./docs/design/DOCUMENTACION.md)** - Arquitectura CSS, componentes, accesibilidad, temas y responsive design
- **[Style Guide](http://localhost:4200/style-guide)** - Catalogo visual de componentes (disponible en desarrollo)

### Secciones de la Documentacion

1. Arquitectura CSS y Comunicacion Visual
2. HTML Semantico y Estructura
3. Sistema de Componentes UI
4. Estrategia Responsive
5. Optimizacion Multimedia
6. Sistema de Temas
7. Aplicacion Completa y Despliegue

---

## Stack Tecnologico

- **Framework**: Angular 21 (standalone components, signals, zoneless)
- **Estilos**: SCSS + CSS Custom Properties
- **Metodologia CSS**: ITCSS + BEM + Angular Emulated
- **Fuentes**: Montserrat + IBM Plex Mono
- **Iconos**: ng-icons (Material Icons + Iconoir)
- **Testing**: Vitest
- **Package Manager**: Bun

---

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
