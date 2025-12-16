package com.example.forcetrack.config

object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val BLOQUES_ROUTE = "bloques"
    const val BLOQUES_ARG = "usuarioId"
    const val BLOQUES = "$BLOQUES_ROUTE/{$BLOQUES_ARG}"

    const val SPLIT_ROUTE = "split"
    const val SPLIT_ARG = "bloqueId"
    const val SPLIT = "$SPLIT_ROUTE/{$SPLIT_ARG}"

    const val RUTINA_ROUTE = "rutina"
    const val RUTINA_ARG = "diaId"
    const val RUTINA = "$RUTINA_ROUTE/{$RUTINA_ARG}"

    const val EJERCICIOS_ROUTE = "ejercicios"
    const val EJERCICIOS_ARG = "diaId"
    const val EJERCICIOS = "$EJERCICIOS_ROUTE/{$EJERCICIOS_ARG}"

    const val CALENDAR_ROUTE = "calendar"
    const val CALENDAR_ARG = "usuarioId"
    const val CALENDAR = "$CALENDAR_ROUTE/{$CALENDAR_ARG}"

    const val DAILY_ROUTE = "daily"
    const val DAILY_ARG_USER = "usuarioId"
    const val DAILY_ARG_DATE = "dateIso"
    const val DAILY = "$DAILY_ROUTE/{$DAILY_ARG_USER}/{$DAILY_ARG_DATE}"

    const val BLOQUES_PUBLICOS = "bloques_publicos"
    const val BLOQUE_PUBLICO_DETALLE_ROUTE = "bloque_publico_detalle"
    const val BLOQUE_PUBLICO_DETALLE_ARG = "bloqueId"
    const val BLOQUE_PUBLICO_DETALLE = "$BLOQUE_PUBLICO_DETALLE_ROUTE/{$BLOQUE_PUBLICO_DETALLE_ARG}"
    const val BLOQUE_PUBLICO_LAB = "bloque_publico_lab"

    /**
     * Construye una ruta con parámetros de forma segura
     */
    fun bloquesWithUserId(userId: Int) = "$BLOQUES_ROUTE/$userId"
    fun splitWithBloqueId(bloqueId: Int) = "$SPLIT_ROUTE/$bloqueId"
    fun rutinaWithDiaId(diaId: Int) = "$RUTINA_ROUTE/$diaId"
    fun ejerciciosWithDiaId(diaId: Int) = "$EJERCICIOS_ROUTE/$diaId"
    fun calendarWithUserId(userId: Int) = "$CALENDAR_ROUTE/$userId"
    fun dailyWithParams(userId: Int, dateIso: String) = "$DAILY_ROUTE/$userId/$dateIso"
    fun bloquePublicoDetalle(bloqueId: Int) = "$BLOQUE_PUBLICO_DETALLE_ROUTE/$bloqueId"
}
