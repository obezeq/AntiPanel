# Justificación decisiones de diseño

## 1. Concepto general del proyecto

### La filosofía "AntiPanel"

Decidimos romper completamente con las convenciones de los paneles SMM tradicionales que hay en el mercado. La mayoría de estos paneles están saturados de información, tienen interfaces confusas y abruman al usuario con un número estúpido de opciones.

Nuestra propuesta es lo contrario: crear una experiencia minimalista pero potente, donde cada elemento tiene un propósito claro. Lo que le llamo una Quality over Quantity experience, aplicando la regla de pareto al máximo.

El nombre "AntiPanel" refleja esta filosofía: no queremos ser una página web más, queremos ser la antítesis de lo que existe. Esto significa eliminar todo lo innecesario y enfocarnos en lo que realmente importa para el usuario, lo que viene a ser eliminar el ruido.

## 2. Home page

### Decisión: Exploración sin registro

Permitimos que los usuarios exploren servicios y creen una simulación de compra demo sin necesidad de registrarse. Esta decisión se basa en reducir la fricción inicial. Los usuarios pueden ver exactamente qué ofrecemos y cómo funciona antes de comprometerse con una cuenta. Solo pedimos registro cuando van a completar una compra real, debido a que es necesario que el usuario ingrese fondos y por cuestiones de seguridad, es importante que el usuario reciba una confirmación de compra, y quede registrado de forma más formal.

**Justificación:** La mayoría de paneles piden registro inmediato, lo que genera desconfianza, sobre todo cuando no puedes ver los servicios que hay ni siquiera cuánto te costaría, tienes que registrarte para averiguarlo… Al dejar explorar libremente, el usuario se familiariza con el servicio y toma una decisión. Haciendo que la UX sea superiormente elevada al resto de paneles SMM actuales.

### Decisión: Sistema de lenguaje natural para órdenes

En lugar de formularios tradicionales con múltiples campos y menús desplegables… implementamos un sistema donde el usuario describe lo que quiere en lenguaje natural dentro de un “buscador inteligente”. Por ejemplo: “Quiero 3000 followers en ig para @cristiano”. Mientras el usuario escribe, se prepará la compra en forma del componente “Quick Order”, donde la cantidad y el usuario se presentarán automáticamente.

Si después el usuario quiere cambiar la cantidad de followers,o el usuario a recibir, puede hacerlo o bien cambiando el input de nuevo o simplemente editando el campo que se le ha preparado automáticamente en el “Quick Order”.

**Justificación:** Los formularios tradicionales requieren que el usuario entienda la estructura del sistema. Con lenguaje natural, el usuario piensa en términos de su objetivo, no en términos técnicos. Esto reduce la carga cognitiva y hace el proceso más intuitivo.

### Decisión: Exploración por plataformas

Organizamos los servicios por plataforma social (Instagram, TikTok, YouTube, etc.) en lugar de por tipo de servicio. Cuando el usuario selecciona una plataforma, ve todos los servicios disponibles para esa red. Adicionalmente el usuario puede posteriormente hacer click en el servicio que desee mientras explora la lista de servicios, para que se le prepare un "Quick Order" con ese servicio en específico y no tenga que escribirlo a mano.

**Justificación:** Los usuarios piensan en términos de "quiero crecer mi Instagram con el menor precio posible y la máxima calidad posible", no "voy a probar 33 tipos de servicios de ig followers a ver cual es el que realmente funciona para crecer mi Instagram". La organización por plataforma se alinea con el modelo mental del usuario.

### Decisión: Tipografía dominante

Usamos una topografía relativamente grande y en negrita como elemento principal de la interfaz, no como decoración. Los títulos ocupan mucho espacio y son el foco visual principal. Queremos respetar la jerarquía de la información, jugando con "tamaño" y "importancia".

**Justificación:** En un mundo saturado de interfaces complejas, la tipografía grande y directa comunica confianza y claridad. No hay dudas sobre qué estás viendo o qué debes hacer, o sobre lo que es realmente importante en la pantalla que estas viendo.

### Decisión: Acceso discreto al login

El botón de acceso (login) esta presente en el header de forma fija, para que siempre sea fácil iniciar sesión en la aplicación o crearse una cuenta si no tienen. Mientras el usuario hace scroll en la home page, el header se mantendrá fijo, para que no tenga que subir hacia arriba para darle al botón. Aparece en una esquina de forma minimalista.

**Justificación:** No queremos que el login sea el foco principal de la home page. El foco debe estar en mostrar el valor del servicio. Los usuarios que ya tienen cuenta saben dónde buscar el acceso. Sin embargo, si una persona quiere iniciar sesión o crear una cuenta, sea un usuario existente o no, es fácil y accesible a su vez.

## 3. Dashboard de usuario

### Decisión: Sistema de order centralizado

Todo el dashboard gira alrededor de la funcionalidad de crear órdenes. Es lo primero que ve el usuario y ocupa el espacio principal, pues es lo único que es realmente importante, tanto para el usuario como para la página web.

**Justificación:** La tarea principal de un usuario en un SMM panel es crear órdenes. Todo lo demás (historial, estadísticas, balance) es secundario. Priorizamos la acción principal.

### Decisión: Autocompletado único

En lugar de mostrar múltiples sugerencias simultáneas, mostramos solo una sugerencia de autocompletado mientras el usuario escribe.

**Justificación:** Múltiples sugerencias crean ruido visual y obligan al usuario a comparar opciones. Una sola sugerencia inteligente es más rápida de procesar y no interrumpe el flujo de escritura.

### Decisión: Navegación fluida entre exploración y orden

Cuando el usuario explora servicios y selecciona uno, puede volver fácilmente a explorar más opciones sin perder su progreso. Para ello, en el componente "Quick Order" se ve claramente como se hace. Presentamos 2 tipos de botones:

- "Explore More" (el cual te llevara a ver mas tipos de servicios)

- "More [Service]" donde [Service] es el tipo de servicio que el usuario tiene en el "Quick Order" (por ejemplo: Instagram).

Al hacer click en "More [Service]" (Ejemplo: "More Instagram"), el usuario vera mas servicios pero especificamente de ese tipo de servicio.

Sin embargo, si hace clic en "Explore More", simplemente vera todos los tipos de servicios de todas las redes sociales.

En cualquier caso si el usuario ha hecho click en "More Instagram" puede retroceder de una forma muy fácil, para ver qué más tipos de redes sociales hay.

**Justificación:** Los usuarios raramente saben exactamente qué quieren en el primer intento. Necesitan poder comparar y cambiar de opinión sin frustración. La navegación fluida respeta este proceso natural de toma de decisiones y es de las cosas más importantes que hay que priorizar en la web, por eso se ha invertido un gran tiempo en pensar cuál es la mejor opción. Se ha ilustrado la idea en el "wireframe", sin embargo donde de verdad se ve es en la práctica y en un diseño de más alta fidelidad.

### Decisión: Balance y estadísticas visibles pero no dominantes

La información del balance y estadísticas del usuario está presente en la parte superior del dashboard, como en el header, pero no compite visualmente con la funcionalidad de crear órdenes.

**Justificación:** El usuario necesita saber su balance antes de ordenar, pero no es la razón por la que está en el dashboard. La información está disponible sin ser intrusiva. Y además necesitamos que el usuario pueda depositar fondos rápidamente. Es por ello que lo hemos hecho de esta manera para eliminar la fricción y que el "usuario" compre lo antes posible.

## 4. Panel de administración

### Decisión: Sidebar en vez de header

Elegimos que la navegación de administrador se haga a través de un sidebar en vez de un header.

**Justificación:** Aunque de momento solo presentamos 2 opciones de navegación en nuestro panel de administrador, debido a que es el MVP. En el futuro, se agregarán más opciones y más vistas específicas, donde el número de opciones aumentará exponencialmente, es más visual y efectivo hacer un "sidebar" en vez de un "header", aunque hubiera resultado más cómodo hacerlo directamente en el "header", es una mejor práctica hacer un sidebar para el panel de administración.

### Decisión: Dashboard de administrador minimalista

Se ha decidido mantener la información más relevante así como estadísticas muy básicas al principio, seguido de una vista muy sencilla y compacta para usuarios y servicios.

**Justificación:** es importante que el usuario tenga un "Quick Overview" de lo que está sucediendo, a parte de una navegación rápida si lo necesita, es por ello que en vez de crear una página distinta para ver una nueva vista de "Usuarios" o "Servicios" para una vista rápida, se ha desplegado de forma rápida en el dashboard de administrador.

A parte es perfecto para el MVP, eliminando la necesidad de crear nuevas vistas, si para el producto mínimo viable, sirve con esta vista rápida, sencilla, minimalista y funcional que ofrece el "quick overview" que mantenemos actualmente.

## 5. Panel de administración - Orders

### Decisión: Vista de tabla compacta

Presentamos las órdenes en formato tabla con filas compactas en lugar de tarjetas grandes.

**Justificación:** Los administradores necesitan procesar mucha información rápidamente y de forma sencilla. Una vista compacta permite ver más órdenes simultáneamente sin desplazarse en exceso, facilitando la detección de patrones y problemas con una buena vista.

### Decisión: Detalles expandibles bajo demanda

La información adicional (información del proveedor, timestamps, y descripciones) aparece solo cuando el administrador hace clic en expandir (que sería un icono en la columna "Actions").

**Justificación:** No toda la información es relevante todo el tiempo. Los detalles expandibles mantienen la tabla principal limpia mientras permiten acceso rápido a información específica cuando se necesita.

### Decisión: Acciones contextuales

Los botones de acción (refill, cancelar) solo aparecen cuando son aplicables según el estado de la orden.

**Justificación:** Mostrar acciones que no se pueden ejecutar genera confusión. Las acciones contextuales comunican claramente qué es posible hacer con cada orden sin necesidad de mensajes de error. Esto permite al administrador cancelar una orden si el usuario ha reportado un problema o hacer un "refill" a este, si el usuario lo pide por soporte. Estos botones se presentarán en la columna "Actions" en forma de iconos.

### Decisión: Filtros y búsqueda prominentes

Los controles de filtrado y búsqueda están siempre visibles en la parte superior. Y podrá filtrar entre el estado (al hacer click en "All") el administrador podrá filtrar por todos cada tipo de estado o todos si lo desea, y también podrá filtrar de más reciente a más antiguo. A la vez que podrá buscar por User ID, Order ID, etc... en el Searchbar.

**Justificación:** Con cientos o miles de órdenes, la capacidad de filtrar y buscar es esencial. Estos controles deben ser inmediatamente accesibles, no escondidos en menús.

### Decisión: Paginación flexible

Permitimos al administrador elegir cuántas órdenes ver por página (10, 25, 50, 100).

**Justificación:** Diferentes tareas requieren diferentes densidades de información. Revisar órdenes individuales funciona mejor con menos ítems por página, mientras que el análisis de patrones requiere ver más órdenes simultáneamente.