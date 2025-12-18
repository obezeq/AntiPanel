# Fase 1: Entender el problema real

**2º DAW – Diseño de Interfaces Web**  

---

## Índice

1. Descripción del problema y su relevancia  
2. User Persona  
3. Análisis de soluciones existentes  
4. Leyes de UX aplicables al proyecto  
   - Ley de Jakob  
   - Sobrecarga de Opciones (Paradoja de la Elección)  
   - Ley de Conectividad Uniforme  

---

## 1. Descripción del problema y su relevancia

Los **SMM Panels** son paneles de marketing para redes sociales donde los usuarios pueden comprar *followers*, *likes*, comentarios y otros servicios de *engagement* para sus perfiles.

El objetivo principal real del usuario es obtener **dopamina**. Las redes sociales están llenas de estímulos constantes, y estas aplicaciones web se dedican a vender esa dopamina en forma de *followers*, *likes* y *comentarios*, los cuales —por supuesto— son interacciones **100 % reales**, definitivamente obtenidas mediante promociones reales, legales y legítimas.

Por ende, el objetivo principal del usuario es comprar estos servicios de la forma **más rápida y simple posible**.

Sin embargo, el proceso actual en estos paneles resulta tedioso. Si el usuario quiere ver todos los servicios disponibles, tiene dos opciones:

- Averiguar que existe una pestaña llamada `/services`, la cual ni siquiera aparece en el *header*, y una vez dentro, esperar varios segundos para que carguen miles de servicios con un sistema de filtrado deficiente.
- Registrarse en una página de dudosa procedencia, verificar el correo electrónico y acceder a una plataforma con una experiencia de usuario de baja calidad solo para poder explorar los servicios.

Mi proyecto se centrará en un **SMM Panel** cuyo objetivo es mejorar la experiencia de usuario, simplificando y optimizando la UX/UI mediante las mejores prácticas posibles, maximizando la accesibilidad y la usabilidad de la aplicación web.

El diseño del panel debe ser **minimalista**. La idea es crear una *anti-página web*, donde la UX y la UI sean la prioridad número uno, logrando la mayor accesibilidad y usabilidad posible. De ahí el nombre **“AntiPanel”**.

---

## 2. User Persona

Se definen dos perfiles de usuario principales que representan los tipos más comunes de usuarios de la aplicación.

### User Persona 1: El buscador de validez social (ego)

- **Nombre y edad:** Marcos, 24 años  
- **Ocupación:** Estudiante  
- **Contexto:** 369 seguidores en Instagram  
- **Motivación:** Necesita validación social constante y busca estatus social  
- **Comportamiento:** Gasta 10 € en *followers* y paga una mensualidad en *likes*  
- **Objetivo:** Comprar *likes* rápidamente antes de que sus amigos vean la foto y aumentar drásticamente sus *followers*

### User Persona 2: El negocio legítimo y fiable

- **Nombre y edad:** Laura, 33 años  
- **Ocupación:** Propietaria de una tienda online  
- **Contexto:** 11 seguidores, perfil que parece “muerto”  
- **Motivación:** Pierde ventas porque los clientes no confían en cuentas pequeñas  
- **Comportamiento:** El crecimiento orgánico es lento debido al algoritmo de las redes sociales  
- **Objetivo:** Conseguir más de 10.000 seguidores para parecer una marca profesional y establecida

---

## 3. Análisis de soluciones existentes

Se han analizado dos SMM Panels:

- **smmraja.com**
- **dripfeedpanel.com**

### smmraja.com

Presenta una sobrecarga extrema de opciones. Existen cientos de categorías y más de **200 tipos de “Instagram Followers”**, lo que genera una gran confusión, especialmente para usuarios novatos.

El inicio de sesión es incoherente: aparentemente solo permite Google Login, pero luego solicita usuario y contraseña.  
El único punto positivo es el sistema de **añadir fondos**, que es rápido y sencillo, aunque el usuario acaba perdido entre miles de opciones sin sentido.

### dripfeedpanel.com

Tiene un inicio de sesión más claro y un filtrado ligeramente mejor estructurado, pero mantiene el mismo problema principal: una cantidad abrumadora de opciones similares sin explicar sus diferencias.

Además, tampoco permite explorar los servicios sin registrarse previamente.

Ambos paneles obligan al registro antes de explorar y presentan una sobrecarga de opciones que paraliza incluso a usuarios expertos.

---

## 4. Leyes de UX aplicables al proyecto

### Ley 1: Ley de Jakob

> “Los usuarios pasan la mayor parte del tiempo en otros sitios, por lo que prefieren que tu sitio funcione igual que todos los que ya conocen”.

Los procesos de registro analizados rompen las expectativas del usuario. Los SaaS modernos deben ofrecer flujos claros, familiares y permitir explorar antes de obligar al registro.

### Ley 2: Sobrecarga de Opciones (Paradoja de la Elección)

> “La tendencia de las personas a sentirse abrumadas cuando se les presenta una gran cantidad de opciones”.

La gran cantidad de categorías y servicios provoca abandono o compras erróneas.  
Para solucionarlo, el proyecto limitará las categorías principales a **tres**, con un sistema de búsqueda guiado e inteligente.

### Ley 3: Ley de Conectividad Uniforme

> “Los elementos que están conectados visualmente se perciben más relacionados que los elementos sin conexión”.

El usuario debe poder completar su objetivo en **menos de tres clics**, mediante una navegación clara, conectada y un diseño minimalista.
