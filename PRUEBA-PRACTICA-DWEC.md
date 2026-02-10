# Justificación DWEC

He hecho una pagina donde muestra las analiticas de todos los usuarios, ruta /analysis , con una arquitectura TS escalable.
- Mantengo un manejo completo de eventos de navegación: routerLink="/nueva" + eventos (click)="navegar('nueva')" tipados y reutilizables en TS, con manejo de estados (active route, guards, etc.) y comunicación entre componentes para mantener coherencia en toda la app.
- Tambien presento una arquitectura standalone impecable: Header/Footer/Item 100% reutilizables/independientes, 2+ componentes nuevos bien estructurados en Git (ng g c + workflow profesional), módulos auto-contenidos y escalables. Para generar todos los componentes he seguido el comando clasico de Angular Cli como he mencionado anteriormente
    - `ng g c` (ng generate component)
    - Se ha ido haciendo commits en la rama correcta: `git add .` , `git commit -m "(feat) nombre comit"` y `git push -u origin prueba-practica` cada 30 minutos aproximadamente.
    - Tambien he presentado modulos con auto contenidos y escalables como se puede observar en todo el proyecto.
- Dentro de esa página presento componentes que engloba (analysis header component y analysis content component).
- Dentro del analysis content component presento las cards de las analiticas globales de todos los usuarios utilizando grid como se ha pedido en los criterios para alinear las cards por columnas responsive, y en cada card utilizo flex para alinear elementos dentro de la card.
- He creado un routing avanzando añadiendo 1 ruta nueva como se especifica, cumpliendo este criterio añadiendo la ruta `/analysis`
```typescript
{
    path: 'analysis',
    loadComponent: () =>
        import('./pages/analysis/analysis').then(m => m.Analysis)
}
```

## Jerarquía y arquitectura
- El servicio llama al endpoint del backend y recibe un `Array<Analysis>`, cada analisis presenta información del backend de todos los usuarios globales (Title, Amount)
- El componente
- Para este pr

## Instrucciones de ejecución

### Acceder a la ruta `frontend`
```bash
cd frontend
```

### Instalar dependencias
**Con npm**
```bash
npm install
```

**Con bun**
```bash
bun intsall
```


### Empezar servidor en modo local / development:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.