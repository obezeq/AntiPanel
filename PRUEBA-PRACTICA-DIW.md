# Justificación DIW

He hecho una pagina donde muestra las analiticas de todos los usuarios, ruta /analysis.
- Dentro de esa página presento componentes que engloba (analysis header component y analysis content component).
- Dentro del analysis content component presento las cards de las analiticas globales de todos los usuarios utilizando grid como se ha pedido en los criterios para alinear las cards por columnas responsive, y en cada card utilizo flex para alinear elementos dentro de la card.
- Los estilos implementados que he utilizado en los componentes se encuentran en este archivo `frontend/src/styles/00-settings/_variables.scss` y he implementado los siguientes colores:

```scss
--color-analysis-text: hsl(0, 0, 69%); // ESTE ES LA NUEVA VARIANTE DE COLOR PARA UTILIZAR EN EL COMPONENTE: COLOR TEXTO
--color-analysis-bg: hsl(134, 100%, 33%); // ESTE ES LA NUEVA VARIANTE DE COLOR PARA UTILIZAR EN EL COMPONENTE NUEVO: COLOR BACKGROUND
```


## Arquitectura:
**¿Por qué has colocado tus variables en la capa Settings y tus estilos en Components?**
- Porque en mi proyecto he elegido la arquitectura ITCSS + BEN + Emulated. Uso las características que proporciona Angular Emulated. En la arquitectura ITCSS defino las variables en `00-settings` porque voy de lo menos específico a lo mas específico, por motivos de especificidad y tenerlo claro con una buena arquitectura. Angular Emulated dice que los estilos tengo que hacerlo en la carpeta de cada componente.

**¿Qué pasaría si importaras Components antes que Settings en el manifiesto?**
- No pasaría nada siempre y cuando los componentes pueden herederas las variables de css (custom properties), mientras se definan las variables en el :root selector en html no habra problema haran que las varibles esten disponibles en todos lados.

## Metodología: 
Explica una ventaja real que te haya aportado usar BEM en este examen frente a usar selectores de etiqueta anidados (ej: div > button).
- La principal ventaja que he notado a la hora de escribir el codigo utilizando la metodología bem, es la facilidad que me da a mi como desarrollador de encontrar los estilos de forma clara, cuando estoy en el inspeccionar de los navegadores puedo claramente encontrar la clase del elemento de html, y luego en cualquier editor de codigo puedo irme a la barra de busqueda y buscar la clase especificamente, permitiendome editar los estilos de forma rapida.
- Ademas tambien me aporta tener una clara **especificidad** lo cual es muy importante a la hora de hacer codigo CSS. Si aumentamos mucho la especificidad, hay un problema debido a que cada vez necesitaremos ir aumentando mas y mas la especificidad, tentando al desarrollador a usar !important, y cosas similares las cuales no son buena practico, bajo casos muy muy muy especificos, como los que tengo en mi proyecto, por motivos de navegadores (por ejemplo el background ese que tengo en forma como con reticulas, tuve que usar !important por motivos de incompatibilidad de navegador Chrome 🖖 . )

## Documentación consultada:
- https://envasador.github.io/diseno-interfaces-web/
- https://lenguajehtml.com/
- https://lenguajecss.com/
- https://stackoverflow.com/questions/75156126/using-css-variable-with-styled-components#75156172
- https://stackoverflow.com/questions/64573177/unable-to-resolve-dependency-tree-error-when-installing-npm-packages
- https://angular.dev/
- https://damiandeluca.com.ar/10-ejemplos-de-css-grid
- https://www.toptal.com/designers/htmlarrows/symbols/white-heart-suit/