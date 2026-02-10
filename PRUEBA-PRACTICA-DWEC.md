# Justificación DWEC

- Nombre: Ezequiel Ortega
- Curso: 2ºDAW
- Fecha: 10/02/2026

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

- Como se observa aqui he hecho la nueva ruta especificando la pagina `./pages/analysis/analysis`.
- El archivo donde se ha especificado las rutas es `frontend/src/app/app.routes.ts`
- Se ha implementado la navegación activa en toda la app.

**@Component({standalone: true})**
- Como este proyecto esta utilizando Angular 21, los componentes tienen automaticamente el `{standalone: true}` en TODOS los componentes. Aunque no se especifique explicitamente en el decorador @Component, SIEMPE se utiliza standalone true, porque Angular 21 lo añade automaticamente.

**templateUrl + styleUrls**
- Se ha implementado el templateUrl + styleUrl en todos los componentes, incluyendo en los componentes creados en `frontend/src/app/pages/analysis` y 2 componentes funcionales:
    - `frontend/src/app/pages/analysis/analysis-content-section`: La seccion donde se encuentra el contenido y las cards de los analisis globales de la web.
    - `frontend/src/app/pages/analysis/analysis-header-section`: El header de la sección Analisis
    - A la hora de crear cualquier componente se utilizo el `ng g c` (ng generate component) con un historial de commits claro.

**Arquitectura Standalone Implecable**
- Se ha implementado el Header/Footer/Item 100% reutilizables/independientes, 2+ componentes nuevos bien estructurados en Git (ng g c + workflow profesional), módulos auto-contenidos y escalables.

**Integración completa frontend-backend**:
- Con el servicio `AnalysisService` estoy consumiento el endpoint de los analisis con un retry logic, como hago con otros componentes para errores 5xx. 
- Tengo la interfaz de TypeScript donde ahi añado la respuesta que obtengo en la peticion http `AnalyticResponse`

**Manejo eventos**
- Hago un buen uso de manejo compleo de los eventos de navegacion, usando routerLink para navegacion declarativa. Y (click) tipado y reutilizable typescript como he comentado anteriormente.
- Como mi proyecto usa Angular 21, me comunico entre los componentes aprovechando las ultimas tecnologisa de signals y outputs.

## Jerarquía y arquitectura
- El servicio llama al endpoint del backend y recibe un `Array<Analysis>`, cada analisis presenta información del backend de todos los usuarios globales (Title, Amount)

1. Con el servicio que he hecho de AnalysisService lanzo una peticion get a la api del backend. 
2. El backend obtiene la analiticas globales de todos los usuarios. Obtengo un observable de la response analitica. Lo obtiene el componente analisis y lo muestro en la pagina gracias al apoyo de flex y grid en el html.

## Instrucciones de ejecución

### Acceder a la ruta `frontend`
```bash
cd frontend
```

### Instalar dependencias
**Con npm (NodeJS Package Manager)**
```bash
npm install
```

**Con Bun.sh**
```bash
bun install
```


### Empezar servidor en modo local / development:

```bash
ng serve
```

Cuando el servidor de cliente este corriendo pueds abrir el navegador y navegar a `http://localhost:4200/` y la aplicación se cargara automaticamente cuando se haga algun cambio en los source files.
