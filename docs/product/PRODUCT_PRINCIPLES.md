# Product Principles

> Reglas para tomar decisiones de producto y de técnica. Si una decisión futura contradice un
> principio, o se cambia la decisión o se cambia el principio — pero explícitamente.

## P1 — El diario primero

Toda funcionalidad se evalúa primero por su aporte al diario personal. Lo social es una capa
sobre el diario, nunca al revés. Si el producto es valioso para un usuario solo en la
plataforma, la base es correcta.

## P2 — El MVP prueba la propuesta, no demuestra capacidades

Una funcionalidad entra al MVP únicamente si es necesaria para probar la propuesta central
(registrar, valorar y consultar teatro visto) o para que el loop principal funcione. "Es simple
de hacer" no es razón suficiente para entrar.

## P3 — Un solo desarrollador es una restricción de diseño, no un detalle

Toda decisión técnica y operativa debe ser sostenible por una persona trabajando parcialmente.
Esto incluye el costo operativo continuo (curaduría del catálogo, moderación, infraestructura),
no solo el costo de construcción.

## P4 — Escalable significa "evolucionable sin reescritura", no "distribuido"

Preferimos límites internos claros que permitan separar componentes en el futuro antes que
implementar arquitectura distribuida sin un problema concreto que la justifique. Ninguna
decisión se toma "porque escala".

## P5 — La calidad del catálogo es un activo central

El catálogo es curado (cerrado a carga directa de usuarios) para garantizar identidad única de
las producciones y calidad de datos. Los usuarios pueden sugerir; el admin aprueba. Cualquier
cambio a este principio requiere resolver antes duplicados, moderación e identidad de entidades.

## P6 — Los datos que caducan son deuda operativa

Evitamos incorporar datos que requieren actualización constante para no quedar obsoletos
(agenda de funciones, horarios, precios). Un dato desactualizado visible es peor que su
ausencia: destruye confianza. El estado de una producción (en cartel / cerrada / próximamente)
es el máximo nivel de "vigencia" que mantenemos en la etapa inicial.

## P7 — Separar saber / suponer / decidir / pendiente

Ningún supuesto se convierte silenciosamente en requisito. Todo supuesto relevante queda
registrado en DECISION_LOG.md con su nivel de evidencia. Las necesidades de usuarios no
validadas se marcan como hipótesis.

## P8 — Instagram es la referencia de fricción, no de features

No competimos con Instagram en lo visual/efímero. Competimos en estructura. Pero la fricción
de registrar debe medirse contra la de subir una story: si registrar cuesta más y devuelve
menos, el hábito no se forma.

## P9 — Máximo USD 10/mes, y cada dólar se defiende

Todo gasto recurrente debe justificarse. Una alternativa gratuita solo se descarta con
evidencia concreta: riesgo de seguridad, falta de una funcionalidad clave, o complejidad
que le agrega al trabajo. La regla corta para los dos lados: tampoco se adopta un free
tier que cueste más en horas de trabajo de lo que ahorra en dólares.
