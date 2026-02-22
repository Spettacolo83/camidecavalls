package com.followmemobile.camidecavalls.domain.util

/**
 * Provides localized strings based on the selected language.
 * This replaces Compose Resources stringResource() for app-controlled localization.
 */
class LocalizedStrings(val languageCode: String) {

    private val strings = when (languageCode.lowercase()) {
        "ca" -> StringsCa
        "es" -> StringsEs
        "en" -> StringsEn
        "de" -> StringsDe
        "fr" -> StringsFr
        "it" -> StringsIt
        else -> StringsEn // Default to English
    }

    // Home Screen
    val homeNoRoutes: String get() = strings.homeNoRoutes
    val homeDistance: String get() = strings.homeDistance
    val homeElevation: String get() = strings.homeElevation
    val homeDuration: String get() = strings.homeDuration
    val difficultyLow: String get() = strings.difficultyLow
    val difficultyMedium: String get() = strings.difficultyMedium
    val difficultyHigh: String get() = strings.difficultyHigh

    // Route Detail
    val routeStage: (Int) -> String = { number -> strings.routeStage(number) }
    val routeViewDetails: String get() = strings.routeViewDetails
    val trackingDistance: String get() = strings.trackingDistance
    val routeDistance: (String) -> String = { km -> strings.routeDistance(km) }
    val routeDifficulty: String get() = strings.routeDifficulty
    val routeDetailElevationGain: String get() = strings.routeDetailElevationGain
    val routeDetailElevationLoss: String get() = strings.routeDetailElevationLoss
    val routeDetailMaxAltitude: String get() = strings.routeDetailMaxAltitude
    val routeDetailMinAltitude: String get() = strings.routeDetailMinAltitude
    val routeDetailAsphalt: String get() = strings.routeDetailAsphalt
    val routeDetailEstimatedTime: String get() = strings.routeDetailEstimatedTime
    val routeDetailMeters: (Int) -> String = { meters -> strings.routeDetailMeters(meters) }
    val routeDetailMinutes: (Int) -> String = { minutes -> strings.routeDetailMinutes(minutes) }
    val routeDetailHours: (Double) -> String = { hours -> strings.routeDetailHours(hours) }
    val routeDetailPercent: (Int) -> String = { percent -> strings.routeDetailPercent(percent) }
    val routeDetailDescription: String get() = strings.routeDetailDescription
    val routeDetailElevationProfile: String get() = strings.routeDetailElevationProfile
    val routeDetailNoElevationData: String get() = strings.routeDetailNoElevationData
    val elevationChartDistance: String get() = strings.elevationChartDistance
    val elevationChartElevation: String get() = strings.elevationChartElevation
    val elevationChartMin: String get() = strings.elevationChartMin
    val elevationChartMid: String get() = strings.elevationChartMid
    val elevationChartMax: String get() = strings.elevationChartMax
    val startPoint: String get() = strings.startPoint
    val endPoint: String get() = strings.endPoint

    // Settings
    val settingsTitle: String get() = strings.settingsTitle
    val settingsLanguage: String get() = strings.settingsLanguage
    val languageCa: String get() = strings.languageCa
    val languageEs: String get() = strings.languageEs
    val languageEn: String get() = strings.languageEn
    val languageDe: String get() = strings.languageDe
    val languageFr: String get() = strings.languageFr
    val languageIt: String get() = strings.languageIt

    // Drawer Menu
    val menuAbout: String get() = strings.menuAbout
    val menuRoutes: String get() = strings.menuRoutes
    val menuMap: String get() = strings.menuMap
    val menuTracking: String get() = strings.menuTracking
    val menuPOIs: String get() = strings.menuPOIs
    val menuNotebook: String get() = strings.menuNotebook
    val menuSettings: String get() = strings.menuSettings

    // Screen Titles (unified with menu labels)
    val aboutTitle: String get() = strings.aboutTitle
    val routesTitle: String get() = strings.routesTitle
    val mapTitle: String get() = strings.mapTitle
    val poisTitle: String get() = strings.poisTitle
    val notebookTitle: String get() = strings.notebookTitle

    // POI Filters
    val poiTypeBeach: String get() = strings.poiTypeBeach
    val poiTypeNatural: String get() = strings.poiTypeNatural
    val poiTypeHistoric: String get() = strings.poiTypeHistoric
    val poiTypeCommercial: String get() = strings.poiTypeCommercial
    val poisFiltersLabel: String get() = strings.poisFiltersLabel

    // About Screen
    val aboutWelcome: String get() = strings.aboutWelcome
    val aboutDescription: String get() = strings.aboutDescription
    val aboutUNESCO: String get() = strings.aboutUNESCO
    val aboutLength: String get() = strings.aboutLength
    val aboutStages: String get() = strings.aboutStages
    val aboutCTA: String get() = strings.aboutCTA
    val aboutExplore: String get() = strings.aboutExplore

    // Notebook / Diary Screen
    val notebookNoSessions: String get() = strings.notebookNoSessions
    val notebookStartTracking: String get() = strings.notebookStartTracking
    val notebookDeleteTitle: String get() = strings.notebookDeleteTitle
    val notebookDeleteMessage: String get() = strings.notebookDeleteMessage
    val notebookDeleteConfirm: String get() = strings.notebookDeleteConfirm
    val notebookCancel: String get() = strings.notebookCancel
    val notebookSaveSessionTitle: String get() = strings.notebookSaveSessionTitle
    val notebookSessionName: String get() = strings.notebookSessionName
    val notebookSave: String get() = strings.notebookSave
    val notebookGeneralTracking: String get() = strings.notebookGeneralTracking

    // Session Detail Screen
    val sessionDetails: String get() = strings.sessionDetails
    val sessionAltitude: String get() = strings.sessionAltitude
    val sessionAvgSpeed: String get() = strings.sessionAvgSpeed
    val sessionMaxSpeed: String get() = strings.sessionMaxSpeed
    val sessionExport: String get() = strings.sessionExport
    val sessionExportMessage: String get() = strings.sessionExportMessage
    val sessionShare: String get() = strings.sessionShare

    // Background Tracking / Permissions
    val backgroundPermissionTitle: String get() = strings.backgroundPermissionTitle
    val backgroundPermissionMessage: String get() = strings.backgroundPermissionMessage
    val backgroundPermissionGrant: String get() = strings.backgroundPermissionGrant
    val notificationTitle: String get() = strings.notificationTitle
    val notificationChannelName: String get() = strings.notificationChannelName

    // Tracking Screen
    val trackingStart: String get() = strings.trackingStart
    val trackingPause: String get() = strings.trackingPause
    val trackingResume: String get() = strings.trackingResume
    val trackingStop: String get() = strings.trackingStop
    val trackingRecordingBadge: String get() = strings.trackingRecordingBadge
    val trackingCompleted: String get() = strings.trackingCompleted
    val trackingError: String get() = strings.trackingError
    val trackingStatisticsTitle: String get() = strings.trackingStatisticsTitle
    val trackingSessionSummary: String get() = strings.trackingSessionSummary
    val trackingLatitude: String get() = strings.trackingLatitude
    val trackingLongitude: String get() = strings.trackingLongitude
    val trackingAccuracy: String get() = strings.trackingAccuracy
    val trackingSpeed: String get() = strings.trackingSpeed
    val trackingAcquiringSignal: String get() = strings.trackingAcquiringSignal
    val trackingStartNewSession: String get() = strings.trackingStartNewSession
    val trackingRetry: String get() = strings.trackingRetry
    val trackingFarTitle: String get() = strings.trackingFarTitle
    val trackingStartAnyway: String get() = strings.trackingStartAnyway
    val trackingSessionPrefix: (String) -> String = { id -> strings.trackingSessionPrefix(id) }
    val trackingFarMessage: (String) -> String = { distance -> strings.trackingFarMessage(distance) }
    val trackingErrorPermission: String get() = strings.trackingErrorPermission
    val trackingErrorGeneric: String get() = strings.trackingErrorGeneric

    // Accessibility
    val back: String get() = strings.back
    val openMenu: String get() = strings.openMenu
    val trackingShowStatistics: String get() = strings.trackingShowStatistics
    val trackingGpsFollowEnabled: String get() = strings.trackingGpsFollowEnabled
    val trackingGpsFollowDisabled: String get() = strings.trackingGpsFollowDisabled
    val trackingDiscardTitle: String get() = strings.trackingDiscardTitle
    val trackingDiscardMessage: String get() = strings.trackingDiscardMessage
    val trackingDiscard: String get() = strings.trackingDiscard

    // Bottom Bar
    val bottomBarMap: String get() = strings.bottomBarMap
    val bottomBarRoutes: String get() = strings.bottomBarRoutes
    val bottomBarPoi: String get() = strings.bottomBarPoi
    val bottomBarNotebook: String get() = strings.bottomBarNotebook
    val bottomBarSettings: String get() = strings.bottomBarSettings

    // Settings Hub
    val settingsLanguageOption: String get() = strings.settingsLanguageOption
    val settingsAboutOption: String get() = strings.settingsAboutOption
    val settingsContactUs: String get() = strings.settingsContactUs

    // Complete Route
    val completeRouteName: String get() = strings.completeRouteName
    val completeRouteSubtitle: String get() = strings.completeRouteSubtitle
}

private interface Strings {
    // Home Screen
    val homeNoRoutes: String
    val homeDistance: String
    val homeElevation: String
    val homeDuration: String
    val difficultyLow: String
    val difficultyMedium: String
    val difficultyHigh: String

    // Route Detail
    fun routeStage(number: Int): String
    val routeViewDetails: String
    val trackingDistance: String
    fun routeDistance(km: String): String
    val routeDifficulty: String
    val routeDetailElevationGain: String
    val routeDetailElevationLoss: String
    val routeDetailMaxAltitude: String
    val routeDetailMinAltitude: String
    val routeDetailAsphalt: String
    val routeDetailEstimatedTime: String
    fun routeDetailMeters(meters: Int): String
    fun routeDetailMinutes(minutes: Int): String
    fun routeDetailHours(hours: Double): String
    fun routeDetailPercent(percent: Int): String
    val routeDetailDescription: String
    val routeDetailElevationProfile: String
    val routeDetailNoElevationData: String
    val elevationChartDistance: String
    val elevationChartElevation: String
    val elevationChartMin: String
    val elevationChartMid: String
    val elevationChartMax: String
    val startPoint: String
    val endPoint: String

    // Settings
    val settingsTitle: String
    val settingsLanguage: String
    val languageCa: String
    val languageEs: String
    val languageEn: String
    val languageDe: String
    val languageFr: String
    val languageIt: String

    // Drawer Menu
    val menuAbout: String
    val menuRoutes: String
    val menuMap: String
    val menuTracking: String
    val menuPOIs: String
    val menuNotebook: String
    val menuSettings: String

    // Screen Titles (unified with menu labels)
    val aboutTitle: String
    val routesTitle: String
    val mapTitle: String
    val poisTitle: String
    val notebookTitle: String

    // POI Filters
    val poiTypeBeach: String
    val poiTypeNatural: String
    val poiTypeHistoric: String
    val poiTypeCommercial: String
    val poisFiltersLabel: String

    // About Screen
    val aboutWelcome: String
    val aboutDescription: String
    val aboutUNESCO: String
    val aboutLength: String
    val aboutStages: String
    val aboutCTA: String
    val aboutExplore: String

    // Notebook / Diary Screen
    val notebookNoSessions: String
    val notebookStartTracking: String
    val notebookDeleteTitle: String
    val notebookDeleteMessage: String
    val notebookDeleteConfirm: String
    val notebookCancel: String
    val notebookSaveSessionTitle: String
    val notebookSessionName: String
    val notebookSave: String
    val notebookGeneralTracking: String

    // Session Detail Screen
    val sessionDetails: String
    val sessionAltitude: String
    val sessionAvgSpeed: String
    val sessionMaxSpeed: String
    val sessionExport: String
    val sessionExportMessage: String
    val sessionShare: String

    // Background Tracking / Permissions
    val backgroundPermissionTitle: String
    val backgroundPermissionMessage: String
    val backgroundPermissionGrant: String
    val notificationTitle: String
    val notificationChannelName: String

    // Tracking Screen
    val trackingStart: String
    val trackingPause: String
    val trackingResume: String
    val trackingStop: String
    val trackingRecordingBadge: String
    val trackingCompleted: String
    val trackingError: String
    val trackingStatisticsTitle: String
    val trackingSessionSummary: String
    val trackingLatitude: String
    val trackingLongitude: String
    val trackingAccuracy: String
    val trackingSpeed: String
    val trackingAcquiringSignal: String
    val trackingStartNewSession: String
    val trackingRetry: String
    val trackingFarTitle: String
    val trackingStartAnyway: String
    fun trackingSessionPrefix(id: String): String
    fun trackingFarMessage(distance: String): String
    val trackingErrorPermission: String
    val trackingErrorGeneric: String

    // Accessibility
    val back: String
    val openMenu: String
    val trackingShowStatistics: String
    val trackingGpsFollowEnabled: String
    val trackingGpsFollowDisabled: String
    val trackingDiscardTitle: String
    val trackingDiscardMessage: String
    val trackingDiscard: String

    // Bottom Bar
    val bottomBarMap: String
    val bottomBarRoutes: String
    val bottomBarPoi: String
    val bottomBarNotebook: String
    val bottomBarSettings: String

    // Settings Hub
    val settingsLanguageOption: String
    val settingsAboutOption: String
    val settingsContactUs: String

    // Complete Route
    val completeRouteName: String
    val completeRouteSubtitle: String
}

private object StringsCa : Strings {
    override val homeNoRoutes = "Cap ruta disponible"
    override val homeDistance = "Distància"
    override val homeElevation = "Desnivell"
    override val homeDuration = "Duració"
    override val difficultyLow = "Baixa"
    override val difficultyMedium = "Mitjana"
    override val difficultyHigh = "Alta"

    override fun routeStage(number: Int) = "Etapa $number"
    override val routeViewDetails = "Detalls de la ruta"
    override val trackingDistance = "Distància"
    override fun routeDistance(km: String) = "$km km"
    override val routeDifficulty = "Dificultat"
    override val routeDetailElevationGain = "Desnivell Positiu"
    override val routeDetailElevationLoss = "Desnivell Negatiu"
    override val routeDetailMaxAltitude = "Altitud Màxima"
    override val routeDetailMinAltitude = "Altitud Mínima"
    override val routeDetailAsphalt = "Asfalt"
    override val routeDetailEstimatedTime = "Temps Estimat"
    override fun routeDetailMeters(meters: Int) = "$meters m"
    override fun routeDetailMinutes(minutes: Int) = "$minutes min"
    override fun routeDetailHours(hours: Double) = "${(hours * 10).toInt() / 10.0} h"
    override fun routeDetailPercent(percent: Int) = "$percent%"
    override val routeDetailDescription = "Descripció"
    override val routeDetailElevationProfile = "Perfil d'Elevació"
    override val routeDetailNoElevationData = "No hi ha dades d'elevació disponibles"
    override val elevationChartDistance = "Distància"
    override val elevationChartElevation = "Elevació"
    override val elevationChartMin = "Mín"
    override val elevationChartMid = "Mig"
    override val elevationChartMax = "Màx"
    override val startPoint = "Punt d'Inici"
    override val endPoint = "Punt Final"

    override val settingsTitle = "Configuració"
    override val settingsLanguage = "Idioma"
    override val languageCa = "Català"
    override val languageEs = "Español"
    override val languageEn = "English"
    override val languageDe = "Deutsch"
    override val languageFr = "Français"
    override val languageIt = "Italiano"

    override val menuAbout = "Camí de Cavalls"
    override val menuRoutes = "Rutes"
    override val menuMap = "Mapa"
    override val menuTracking = "Seguiment"
    override val menuPOIs = "Punts d'Interès"
    override val menuNotebook = "Quadern"
    override val menuSettings = "Configuració"

    override val aboutTitle = "Camí de Cavalls"
    override val routesTitle = "Rutes"
    override val mapTitle = "Mapa"
    override val poisTitle = "Punts d'Interès"
    override val notebookTitle = "Quadern"

    override val poiTypeBeach = "Zona Costanera"
    override val poiTypeNatural = "Espai Natural"
    override val poiTypeHistoric = "Patrimoni Històric"
    override val poiTypeCommercial = "Activitats comercials"
    override val poisFiltersLabel = "Filtra POI"

    override val aboutWelcome = "Benvingut al Camí de Cavalls"
    override val aboutDescription = "El Camí de Cavalls és una de les millors maneres de viure Menorca en totes les seves dimensions. Aquest històric camí costaner rodeja l'illa oferint un viatge immersiu per paisatges naturals, gastronomia, cultura i història.\n\nEl recorregut comprèn 20 etapes distintives, cadascuna dissenyada per mostrar diferents aspectes ambientals i històrics de l'illa."
    override val aboutUNESCO = "Menorca va ser declarada Reserva de la Biosfera per la UNESCO l'octubre de 1993, reconeixent l'equilibri entre la preservació del seu ric patrimoni natural i cultural i el desenvolupament econòmic."
    override val aboutLength = "185 km de senderisme costaner"
    override val aboutStages = "20 etapes · GR-223"
    override val aboutCTA = "Conèixer és estimar. Descobreix els valors ambientals que fan de Menorca un lloc tan excepcional."
    override val aboutExplore = "Comença la teva aventura"

    override val notebookNoSessions = "Encara no tens cap sessió registrada"
    override val notebookStartTracking = "Iniciar Seguiment"
    override val notebookDeleteTitle = "Eliminar sessió"
    override val notebookDeleteMessage = "Estàs segur que vols eliminar aquesta sessió? Aquesta acció no es pot desfer."
    override val notebookDeleteConfirm = "Eliminar"
    override val notebookCancel = "Cancel·lar"
    override val notebookSaveSessionTitle = "Desar sessió"
    override val notebookSessionName = "Nom de la sessió"
    override val notebookSave = "Desar"
    override val notebookGeneralTracking = "Seguiment General"

    override val sessionDetails = "Detalls de la sessió"
    override val sessionAltitude = "Altitud"
    override val sessionAvgSpeed = "Velocitat mitjana"
    override val sessionMaxSpeed = "Velocitat màxima"
    override val sessionExport = "Exportar GPX"
    override val sessionExportMessage = "Comparteix el teu recorregut amb altres aplicacions"
    override val sessionShare = "Compartir"

    override val backgroundPermissionTitle = "Seguiment en segon pla"
    override val backgroundPermissionMessage = "Per seguir registrant el teu recorregut quan l'app és en segon pla, cal el permís de localització \"Sempre\"."
    override val backgroundPermissionGrant = "Concedir permís"
    override val notificationTitle = "Seguiment GPS actiu"
    override val notificationChannelName = "Seguiment GPS"

    override val trackingStart = "Iniciar"
    override val trackingPause = "Pausar"
    override val trackingResume = "Reprendre"
    override val trackingStop = "Aturar"
    override val trackingRecordingBadge = "ENREGISTRANT"
    override val trackingCompleted = "Resum de la sessió"
    override val trackingError = "Error de Seguiment"
    override val trackingStatisticsTitle = "Estadístiques en Directe"
    override val trackingSessionSummary = "Resum de Sessió"
    override val trackingLatitude = "Latitud"
    override val trackingLongitude = "Longitud"
    override val trackingAccuracy = "Precisió"
    override val trackingSpeed = "Velocitat"
    override val trackingAcquiringSignal = "Adquirint senyal GPS…"
    override val trackingStartNewSession = "Tancar"
    override val trackingRetry = "Reintentar"
    override val trackingFarTitle = "Ets lluny de la ruta"
    override val trackingStartAnyway = "Iniciar igualment"
    override fun trackingSessionPrefix(id: String) = "Sessió: $id…"
    override fun trackingFarMessage(distance: String) = "Ets a $distance km del punt més proper de la ruta. Vols iniciar el seguiment igualment?"
    override val trackingErrorPermission = "Cal el permís de localització per iniciar el seguiment."
    override val trackingErrorGeneric = "S'ha produït un error"

    override val back = "Enrere"
    override val openMenu = "Obrir menú"
    override val trackingShowStatistics = "Mostrar estadístiques"
    override val trackingGpsFollowEnabled = "Seguiment GPS activat"
    override val trackingGpsFollowDisabled = "Seguiment GPS desactivat"
    override val trackingDiscardTitle = "Descartar seguiment?"
    override val trackingDiscardMessage = "Totes les dades registrades es perdran permanentment."
    override val trackingDiscard = "Descartar"

    override val bottomBarMap = "Mapa"
    override val bottomBarRoutes = "Rutes"
    override val bottomBarPoi = "POI"
    override val bottomBarNotebook = "Diari"
    override val bottomBarSettings = "Configuració"
    override val settingsLanguageOption = "Idioma"
    override val settingsAboutOption = "Camí de Cavalls"
    override val settingsContactUs = "Contacta'ns"
    override val completeRouteName = "Ruta Completa"
    override val completeRouteSubtitle = "Bucle complet"
}

private object StringsEs : Strings {
    override val homeNoRoutes = "No hay rutas disponibles"
    override val homeDistance = "Distancia"
    override val homeElevation = "Desnivel"
    override val homeDuration = "Duración"
    override val difficultyLow = "Baja"
    override val difficultyMedium = "Media"
    override val difficultyHigh = "Alta"

    override fun routeStage(number: Int) = "Etapa $number"
    override val routeViewDetails = "Detalles de la ruta"
    override val trackingDistance = "Distancia"
    override fun routeDistance(km: String) = "$km km"
    override val routeDifficulty = "Dificultad"
    override val routeDetailElevationGain = "Desnivel Positivo"
    override val routeDetailElevationLoss = "Desnivel Negativo"
    override val routeDetailMaxAltitude = "Altitud Máxima"
    override val routeDetailMinAltitude = "Altitud Mínima"
    override val routeDetailAsphalt = "Asfalto"
    override val routeDetailEstimatedTime = "Tiempo Estimado"
    override fun routeDetailMeters(meters: Int) = "$meters m"
    override fun routeDetailMinutes(minutes: Int) = "$minutes min"
    override fun routeDetailHours(hours: Double) = "${(hours * 10).toInt() / 10.0} h"
    override fun routeDetailPercent(percent: Int) = "$percent%"
    override val routeDetailDescription = "Descripción"
    override val routeDetailElevationProfile = "Perfil de Elevación"
    override val routeDetailNoElevationData = "No hay datos de elevación disponibles"
    override val elevationChartDistance = "Distancia"
    override val elevationChartElevation = "Elevación"
    override val elevationChartMin = "Mín"
    override val elevationChartMid = "Med"
    override val elevationChartMax = "Máx"
    override val startPoint = "Punto de Inicio"
    override val endPoint = "Punto Final"

    override val settingsTitle = "Configuración"
    override val settingsLanguage = "Idioma"
    override val languageCa = "Català"
    override val languageEs = "Español"
    override val languageEn = "English"
    override val languageDe = "Deutsch"
    override val languageFr = "Français"
    override val languageIt = "Italiano"

    override val menuAbout = "Camí de Cavalls"
    override val menuRoutes = "Rutas"
    override val menuMap = "Mapa"
    override val menuTracking = "Seguimiento"
    override val menuPOIs = "Puntos de Interés"
    override val menuNotebook = "Diario"
    override val menuSettings = "Configuración"

    override val aboutTitle = "Camí de Cavalls"
    override val routesTitle = "Rutas"
    override val mapTitle = "Mapa"
    override val poisTitle = "Puntos de Interés"
    override val notebookTitle = "Diario"

    override val poiTypeBeach = "Zona Costera"
    override val poiTypeNatural = "Espacio Natural"
    override val poiTypeHistoric = "Patrimonio Histórico"
    override val poiTypeCommercial = "Actividades comerciales"
    override val poisFiltersLabel = "Filtrar POI"

    override val aboutWelcome = "Bienvenido al Camí de Cavalls"
    override val aboutDescription = "El Camí de Cavalls es una de las mejores maneras de experimentar Menorca en todas sus dimensiones. Este histórico sendero costero rodea la isla ofreciendo un viaje inmersivo a través de paisajes naturales, gastronomía, cultura e historia.\n\nEl recorrido comprende 20 etapas distintivas, cada una diseñada para mostrar diferentes aspectos ambientales e históricos de la isla."
    override val aboutUNESCO = "Menorca fue declarada Reserva de la Biosfera por la UNESCO en octubre de 1993, reconociendo el equilibrio entre la preservación de su rico patrimonio natural y cultural y el desarrollo económico."
    override val aboutLength = "185 km de senderismo costero"
    override val aboutStages = "20 etapas · GR-223"
    override val aboutCTA = "Conocer es amar. Descubre los valores ambientales que hacen de Menorca un lugar tan excepcional."
    override val aboutExplore = "Comienza tu aventura"

    override val notebookNoSessions = "Aún no tienes ninguna sesión registrada"
    override val notebookStartTracking = "Iniciar Seguimiento"
    override val notebookDeleteTitle = "Eliminar sesión"
    override val notebookDeleteMessage = "¿Estás seguro de que quieres eliminar esta sesión? Esta acción no se puede deshacer."
    override val notebookDeleteConfirm = "Eliminar"
    override val notebookCancel = "Cancelar"
    override val notebookSaveSessionTitle = "Guardar sesión"
    override val notebookSessionName = "Nombre de la sesión"
    override val notebookSave = "Guardar"
    override val notebookGeneralTracking = "Seguimiento General"

    override val sessionDetails = "Detalles de la sesión"
    override val sessionAltitude = "Altitud"
    override val sessionAvgSpeed = "Velocidad media"
    override val sessionMaxSpeed = "Velocidad máxima"
    override val sessionExport = "Exportar GPX"
    override val sessionExportMessage = "Comparte tu recorrido con otras aplicaciones"
    override val sessionShare = "Compartir"

    override val backgroundPermissionTitle = "Seguimiento en segundo plano"
    override val backgroundPermissionMessage = "Para seguir registrando tu recorrido cuando la app está en segundo plano, es necesario el permiso de ubicación \"Siempre\"."
    override val backgroundPermissionGrant = "Conceder permiso"
    override val notificationTitle = "Seguimiento GPS activo"
    override val notificationChannelName = "Seguimiento GPS"

    override val trackingStart = "Iniciar"
    override val trackingPause = "Pausar"
    override val trackingResume = "Reanudar"
    override val trackingStop = "Detener"
    override val trackingRecordingBadge = "GRABANDO"
    override val trackingCompleted = "Resumen de la sesión"
    override val trackingError = "Error de Seguimiento"
    override val trackingStatisticsTitle = "Estadísticas en Vivo"
    override val trackingSessionSummary = "Resumen de Sesión"
    override val trackingLatitude = "Latitud"
    override val trackingLongitude = "Longitud"
    override val trackingAccuracy = "Precisión"
    override val trackingSpeed = "Velocidad"
    override val trackingAcquiringSignal = "Adquiriendo señal GPS…"
    override val trackingStartNewSession = "Cerrar"
    override val trackingRetry = "Reintentar"
    override val trackingFarTitle = "Estás lejos de la ruta"
    override val trackingStartAnyway = "Iniciar de todos modos"
    override fun trackingSessionPrefix(id: String) = "Sesión: $id…"
    override fun trackingFarMessage(distance: String) = "Estás a $distance km del punto más cercano de la ruta. ¿Quieres iniciar el seguimiento de todos modos?"
    override val trackingErrorPermission = "Se necesita el permiso de ubicación para iniciar el seguimiento."
    override val trackingErrorGeneric = "Se ha producido un error"

    override val back = "Atrás"
    override val openMenu = "Abrir menú"
    override val trackingShowStatistics = "Mostrar estadísticas"
    override val trackingGpsFollowEnabled = "Seguimiento GPS activado"
    override val trackingGpsFollowDisabled = "Seguimiento GPS desactivado"
    override val trackingDiscardTitle = "¿Descartar seguimiento?"
    override val trackingDiscardMessage = "Todos los datos registrados se perderán permanentemente."
    override val trackingDiscard = "Descartar"

    override val bottomBarMap = "Mapa"
    override val bottomBarRoutes = "Rutas"
    override val bottomBarPoi = "POI"
    override val bottomBarNotebook = "Diario"
    override val bottomBarSettings = "Ajustes"
    override val settingsLanguageOption = "Idioma"
    override val settingsAboutOption = "Camí de Cavalls"
    override val settingsContactUs = "Contáctanos"
    override val completeRouteName = "Ruta Completa"
    override val completeRouteSubtitle = "Bucle completo"
}

private object StringsEn : Strings {
    override val homeNoRoutes = "No routes available"
    override val homeDistance = "Distance"
    override val homeElevation = "Elevation"
    override val homeDuration = "Duration"
    override val difficultyLow = "Low"
    override val difficultyMedium = "Medium"
    override val difficultyHigh = "High"

    override fun routeStage(number: Int) = "Stage $number"
    override val routeViewDetails = "Route Details"
    override val trackingDistance = "Distance"
    override fun routeDistance(km: String) = "$km km"
    override val routeDifficulty = "Difficulty"
    override val routeDetailElevationGain = "Elevation Gain"
    override val routeDetailElevationLoss = "Elevation Loss"
    override val routeDetailMaxAltitude = "Max Altitude"
    override val routeDetailMinAltitude = "Min Altitude"
    override val routeDetailAsphalt = "Asphalt"
    override val routeDetailEstimatedTime = "Estimated Time"
    override fun routeDetailMeters(meters: Int) = "$meters m"
    override fun routeDetailMinutes(minutes: Int) = "$minutes min"
    override fun routeDetailHours(hours: Double) = "${(hours * 10).toInt() / 10.0} h"
    override fun routeDetailPercent(percent: Int) = "$percent%"
    override val routeDetailDescription = "Description"
    override val routeDetailElevationProfile = "Elevation Profile"
    override val routeDetailNoElevationData = "No elevation data available"
    override val elevationChartDistance = "Distance"
    override val elevationChartElevation = "Elevation"
    override val elevationChartMin = "Min"
    override val elevationChartMid = "Mid"
    override val elevationChartMax = "Max"
    override val startPoint = "Start Point"
    override val endPoint = "End Point"

    override val settingsTitle = "Settings"
    override val settingsLanguage = "Language"
    override val languageCa = "Català"
    override val languageEs = "Español"
    override val languageEn = "English"
    override val languageDe = "Deutsch"
    override val languageFr = "Français"
    override val languageIt = "Italiano"

    override val menuAbout = "Camí de Cavalls"
    override val menuRoutes = "Routes"
    override val menuMap = "Map"
    override val menuTracking = "Tracking"
    override val menuPOIs = "Points of Interest"
    override val menuNotebook = "Notebook"
    override val menuSettings = "Settings"

    override val aboutTitle = "Camí de Cavalls"
    override val routesTitle = "Routes"
    override val mapTitle = "Map"
    override val poisTitle = "Points of Interest"
    override val notebookTitle = "Notebook"

    override val poiTypeBeach = "Coastal Area"
    override val poiTypeNatural = "Natural Site"
    override val poiTypeHistoric = "Historic Site"
    override val poiTypeCommercial = "Commercial Activities"
    override val poisFiltersLabel = "Filter POIs"

    override val aboutWelcome = "Welcome to Camí de Cavalls"
    override val aboutDescription = "The Camí de Cavalls is one of the best ways to experience Menorca in all its dimensions. This historic coastal path encircles the island, offering an immersive journey through natural landscapes, gastronomy, culture, and history.\n\nThe trail comprises 20 distinctive stages, each designed to showcase different environmental and historical aspects of the island."
    override val aboutUNESCO = "Menorca has held UNESCO Biosphere Reserve designation since October 1993, recognizing the balance between preserving its rich natural and cultural heritage while supporting economic development."
    override val aboutLength = "185 km of coastal hiking"
    override val aboutStages = "20 stages · GR-223"
    override val aboutCTA = "Knowing is loving. Discover the environmental values that make Menorca such an exceptional place."
    override val aboutExplore = "Start your adventure"

    override val notebookNoSessions = "You don't have any recorded sessions yet"
    override val notebookStartTracking = "Start Tracking"
    override val notebookDeleteTitle = "Delete session"
    override val notebookDeleteMessage = "Are you sure you want to delete this session? This action cannot be undone."
    override val notebookDeleteConfirm = "Delete"
    override val notebookCancel = "Cancel"
    override val notebookSaveSessionTitle = "Save session"
    override val notebookSessionName = "Session name"
    override val notebookSave = "Save"
    override val notebookGeneralTracking = "General Tracking"

    override val sessionDetails = "Session details"
    override val sessionAltitude = "Altitude"
    override val sessionAvgSpeed = "Average speed"
    override val sessionMaxSpeed = "Max speed"
    override val sessionExport = "Export GPX"
    override val sessionExportMessage = "Share your track with other apps"
    override val sessionShare = "Share"

    override val backgroundPermissionTitle = "Background tracking"
    override val backgroundPermissionMessage = "To keep recording your track when the app is in the background, the \"Always\" location permission is required."
    override val backgroundPermissionGrant = "Grant permission"
    override val notificationTitle = "GPS tracking active"
    override val notificationChannelName = "GPS Tracking"

    override val trackingStart = "Start"
    override val trackingPause = "Pause"
    override val trackingResume = "Resume"
    override val trackingStop = "Stop"
    override val trackingRecordingBadge = "RECORDING"
    override val trackingCompleted = "Session summary"
    override val trackingError = "Tracking Error"
    override val trackingStatisticsTitle = "Live Statistics"
    override val trackingSessionSummary = "Session Summary"
    override val trackingLatitude = "Latitude"
    override val trackingLongitude = "Longitude"
    override val trackingAccuracy = "Accuracy"
    override val trackingSpeed = "Speed"
    override val trackingAcquiringSignal = "Acquiring GPS signal…"
    override val trackingStartNewSession = "Close"
    override val trackingRetry = "Retry"
    override val trackingFarTitle = "You are far from the route"
    override val trackingStartAnyway = "Start anyway"
    override fun trackingSessionPrefix(id: String) = "Session: $id…"
    override fun trackingFarMessage(distance: String) = "You are $distance km away from the nearest route. Do you want to start tracking anyway?"
    override val trackingErrorPermission = "Location permission is required to start tracking."
    override val trackingErrorGeneric = "An error occurred"

    override val back = "Back"
    override val openMenu = "Open menu"
    override val trackingShowStatistics = "Show statistics"
    override val trackingGpsFollowEnabled = "GPS follow enabled"
    override val trackingGpsFollowDisabled = "GPS follow disabled"
    override val trackingDiscardTitle = "Discard tracking?"
    override val trackingDiscardMessage = "All recorded data will be permanently lost."
    override val trackingDiscard = "Discard"

    override val bottomBarMap = "Map"
    override val bottomBarRoutes = "Routes"
    override val bottomBarPoi = "POI"
    override val bottomBarNotebook = "Diary"
    override val bottomBarSettings = "Settings"
    override val settingsLanguageOption = "Language"
    override val settingsAboutOption = "Camí de Cavalls"
    override val settingsContactUs = "Contact us"
    override val completeRouteName = "Complete Route"
    override val completeRouteSubtitle = "Full loop"
}

private object StringsDe : Strings {
    override val homeNoRoutes = "Keine Routen verfügbar"
    override val homeDistance = "Entfernung"
    override val homeElevation = "Höhenmeter"
    override val homeDuration = "Dauer"
    override val difficultyLow = "Niedrig"
    override val difficultyMedium = "Mittel"
    override val difficultyHigh = "Hoch"

    override fun routeStage(number: Int) = "Etappe $number"
    override val routeViewDetails = "Routendetails"
    override val trackingDistance = "Entfernung"
    override fun routeDistance(km: String) = "$km km"
    override val routeDifficulty = "Schwierigkeitsgrad"
    override val routeDetailElevationGain = "Höhengewinn"
    override val routeDetailElevationLoss = "Höhenverlust"
    override val routeDetailMaxAltitude = "Maximale Höhe"
    override val routeDetailMinAltitude = "Minimale Höhe"
    override val routeDetailAsphalt = "Asphalt"
    override val routeDetailEstimatedTime = "Geschätzte Zeit"
    override fun routeDetailMeters(meters: Int) = "$meters m"
    override fun routeDetailMinutes(minutes: Int) = "$minutes min"
    override fun routeDetailHours(hours: Double) = "${(hours * 10).toInt() / 10.0} Std"
    override fun routeDetailPercent(percent: Int) = "$percent%"
    override val routeDetailDescription = "Beschreibung"
    override val routeDetailElevationProfile = "Höhenprofil"
    override val routeDetailNoElevationData = "Keine Höhendaten verfügbar"
    override val elevationChartDistance = "Entfernung"
    override val elevationChartElevation = "Höhe"
    override val elevationChartMin = "Min"
    override val elevationChartMid = "Mit"
    override val elevationChartMax = "Max"
    override val startPoint = "Startpunkt"
    override val endPoint = "Endpunkt"

    override val settingsTitle = "Einstellungen"
    override val settingsLanguage = "Sprache"
    override val languageCa = "Català"
    override val languageEs = "Español"
    override val languageEn = "English"
    override val languageDe = "Deutsch"
    override val languageFr = "Français"
    override val languageIt = "Italiano"

    override val menuAbout = "Camí de Cavalls"
    override val menuRoutes = "Routen"
    override val menuMap = "Karte"
    override val menuTracking = "Verfolgung"
    override val menuPOIs = "Sehenswürdigkeiten"
    override val menuNotebook = "Tagebuch"
    override val menuSettings = "Einstellungen"

    override val aboutTitle = "Camí de Cavalls"
    override val routesTitle = "Routen"
    override val mapTitle = "Karte"
    override val poisTitle = "Sehenswürdigkeiten"
    override val notebookTitle = "Tagebuch"

    override val poiTypeBeach = "Küstenbereich"
    override val poiTypeNatural = "Naturraum"
    override val poiTypeHistoric = "Historischer Ort"
    override val poiTypeCommercial = "Kommerzielle Angebote"
    override val poisFiltersLabel = "POIs filtern"

    override val aboutWelcome = "Willkommen beim Camí de Cavalls"
    override val aboutDescription = "Der Camí de Cavalls ist eine der besten Möglichkeiten, Menorca in all seinen Dimensionen zu erleben. Dieser historische Küstenweg umrundet die Insel und bietet eine immersive Reise durch Naturlandschaften, Gastronomie, Kultur und Geschichte.\n\nDer Weg umfasst 20 unterschiedliche Etappen, jede entworfen, um verschiedene Umwelt- und historische Aspekte der Insel zu zeigen."
    override val aboutUNESCO = "Menorca wurde im Oktober 1993 von der UNESCO zum Biosphärenreservat erklärt und würdigt damit das Gleichgewicht zwischen der Erhaltung des reichen Natur- und Kulturerbes und der wirtschaftlichen Entwicklung."
    override val aboutLength = "185 km Küstenwanderung"
    override val aboutStages = "20 Etappen · GR-223"
    override val aboutCTA = "Kennen bedeutet lieben. Entdecken Sie die Umweltwerte, die Menorca zu einem so außergewöhnlichen Ort machen."
    override val aboutExplore = "Starten Sie Ihr Abenteuer"

    override val notebookNoSessions = "Sie haben noch keine aufgezeichneten Sitzungen"
    override val notebookStartTracking = "Verfolgung starten"
    override val notebookDeleteTitle = "Sitzung löschen"
    override val notebookDeleteMessage = "Sind Sie sicher, dass Sie diese Sitzung löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden."
    override val notebookDeleteConfirm = "Löschen"
    override val notebookCancel = "Abbrechen"
    override val notebookSaveSessionTitle = "Sitzung speichern"
    override val notebookSessionName = "Sitzungsname"
    override val notebookSave = "Speichern"
    override val notebookGeneralTracking = "Allgemeine Verfolgung"

    override val sessionDetails = "Sitzungsdetails"
    override val sessionAltitude = "Höhe"
    override val sessionAvgSpeed = "Durchschnittsgeschwindigkeit"
    override val sessionMaxSpeed = "Höchstgeschwindigkeit"
    override val sessionExport = "GPX exportieren"
    override val sessionExportMessage = "Teilen Sie Ihre Strecke mit anderen Apps"
    override val sessionShare = "Teilen"

    override val backgroundPermissionTitle = "Hintergrundverfolgung"
    override val backgroundPermissionMessage = "Um Ihre Strecke weiter aufzuzeichnen, wenn die App im Hintergrund ist, wird die Standortberechtigung \"Immer\" benötigt."
    override val backgroundPermissionGrant = "Berechtigung erteilen"
    override val notificationTitle = "GPS-Verfolgung aktiv"
    override val notificationChannelName = "GPS-Verfolgung"

    override val trackingStart = "Starten"
    override val trackingPause = "Pause"
    override val trackingResume = "Fortsetzen"
    override val trackingStop = "Stoppen"
    override val trackingRecordingBadge = "AUFZEICHNUNG"
    override val trackingCompleted = "Sitzungsübersicht"
    override val trackingError = "Verfolgungsfehler"
    override val trackingStatisticsTitle = "Live-Statistiken"
    override val trackingSessionSummary = "Sitzungszusammenfassung"
    override val trackingLatitude = "Breitengrad"
    override val trackingLongitude = "Längengrad"
    override val trackingAccuracy = "Genauigkeit"
    override val trackingSpeed = "Geschwindigkeit"
    override val trackingAcquiringSignal = "GPS-Signal wird gesucht…"
    override val trackingStartNewSession = "Schließen"
    override val trackingRetry = "Erneut versuchen"
    override val trackingFarTitle = "Sie sind weit von der Route entfernt"
    override val trackingStartAnyway = "Trotzdem starten"
    override fun trackingSessionPrefix(id: String) = "Sitzung: $id…"
    override fun trackingFarMessage(distance: String) = "Sie sind $distance km vom nächsten Punkt der Route entfernt. Möchten Sie die Verfolgung trotzdem starten?"
    override val trackingErrorPermission = "Standortberechtigung ist erforderlich, um die Verfolgung zu starten."
    override val trackingErrorGeneric = "Ein Fehler ist aufgetreten"

    override val back = "Zurück"
    override val openMenu = "Menü öffnen"
    override val trackingShowStatistics = "Statistiken anzeigen"
    override val trackingGpsFollowEnabled = "GPS-Verfolgung aktiviert"
    override val trackingGpsFollowDisabled = "GPS-Verfolgung deaktiviert"
    override val trackingDiscardTitle = "Aufzeichnung verwerfen?"
    override val trackingDiscardMessage = "Alle aufgezeichneten Daten gehen dauerhaft verloren."
    override val trackingDiscard = "Verwerfen"

    override val bottomBarMap = "Karte"
    override val bottomBarRoutes = "Routen"
    override val bottomBarPoi = "POI"
    override val bottomBarNotebook = "Tagebuch"
    override val bottomBarSettings = "Einstellungen"
    override val settingsLanguageOption = "Sprache"
    override val settingsAboutOption = "Camí de Cavalls"
    override val settingsContactUs = "Kontaktiere uns"
    override val completeRouteName = "Vollständige Route"
    override val completeRouteSubtitle = "Volle Runde"
}

private object StringsFr : Strings {
    override val homeNoRoutes = "Aucune route disponible"
    override val homeDistance = "Distance"
    override val homeElevation = "Dénivelé"
    override val homeDuration = "Durée"
    override val difficultyLow = "Facile"
    override val difficultyMedium = "Moyenne"
    override val difficultyHigh = "Difficile"

    override fun routeStage(number: Int) = "Étape $number"
    override val routeViewDetails = "Détails de l'itinéraire"
    override val trackingDistance = "Distance"
    override fun routeDistance(km: String) = "$km km"
    override val routeDifficulty = "Difficulté"
    override val routeDetailElevationGain = "Dénivelé Positif"
    override val routeDetailElevationLoss = "Dénivelé Négatif"
    override val routeDetailMaxAltitude = "Altitude Maximale"
    override val routeDetailMinAltitude = "Altitude Minimale"
    override val routeDetailAsphalt = "Asphalte"
    override val routeDetailEstimatedTime = "Temps Estimé"
    override fun routeDetailMeters(meters: Int) = "$meters m"
    override fun routeDetailMinutes(minutes: Int) = "$minutes min"
    override fun routeDetailHours(hours: Double) = "${(hours * 10).toInt() / 10.0} h"
    override fun routeDetailPercent(percent: Int) = "$percent%"
    override val routeDetailDescription = "Description"
    override val routeDetailElevationProfile = "Profil d'Altitude"
    override val routeDetailNoElevationData = "Aucune donnée d'altitude disponible"
    override val elevationChartDistance = "Distance"
    override val elevationChartElevation = "Altitude"
    override val elevationChartMin = "Min"
    override val elevationChartMid = "Moy"
    override val elevationChartMax = "Max"
    override val startPoint = "Point de Départ"
    override val endPoint = "Point d'Arrivée"

    override val settingsTitle = "Paramètres"
    override val settingsLanguage = "Langue"
    override val languageCa = "Català"
    override val languageEs = "Español"
    override val languageEn = "English"
    override val languageDe = "Deutsch"
    override val languageFr = "Français"
    override val languageIt = "Italiano"

    override val menuAbout = "Camí de Cavalls"
    override val menuRoutes = "Itinéraires"
    override val menuMap = "Carte"
    override val menuTracking = "Suivi"
    override val menuPOIs = "Points d'Intérêt"
    override val menuNotebook = "Carnet"
    override val menuSettings = "Paramètres"

    override val aboutTitle = "Camí de Cavalls"
    override val routesTitle = "Itinéraires"
    override val mapTitle = "Carte"
    override val poisTitle = "Points d'Intérêt"
    override val notebookTitle = "Carnet"

    override val poiTypeBeach = "Zone Côtière"
    override val poiTypeNatural = "Espace Naturel"
    override val poiTypeHistoric = "Site Historique"
    override val poiTypeCommercial = "Activités commerciales"
    override val poisFiltersLabel = "Filtrer les POI"

    override val aboutWelcome = "Bienvenue sur le Camí de Cavalls"
    override val aboutDescription = "Le Camí de Cavalls est l'une des meilleures façons de découvrir Minorque dans toutes ses dimensions. Ce sentier côtier historique fait le tour de l'île, offrant un voyage immersif à travers les paysages naturels, la gastronomie, la culture et l'histoire.\n\nLe parcours comprend 20 étapes distinctives, chacune conçue pour présenter différents aspects environnementaux et historiques de l'île."
    override val aboutUNESCO = "Minorque a été désignée Réserve de Biosphère de l'UNESCO en octobre 1993, reconnaissant l'équilibre entre la préservation de son riche patrimoine naturel et culturel et le développement économique."
    override val aboutLength = "185 km de randonnée côtière"
    override val aboutStages = "20 étapes · GR-223"
    override val aboutCTA = "Connaître c'est aimer. Découvrez les valeurs environnementales qui font de Minorque un lieu si exceptionnel."
    override val aboutExplore = "Commencez votre aventure"

    override val notebookNoSessions = "Vous n'avez pas encore de sessions enregistrées"
    override val notebookStartTracking = "Démarrer le suivi"
    override val notebookDeleteTitle = "Supprimer la session"
    override val notebookDeleteMessage = "Êtes-vous sûr de vouloir supprimer cette session ? Cette action ne peut pas être annulée."
    override val notebookDeleteConfirm = "Supprimer"
    override val notebookCancel = "Annuler"
    override val notebookSaveSessionTitle = "Enregistrer la session"
    override val notebookSessionName = "Nom de la session"
    override val notebookSave = "Enregistrer"
    override val notebookGeneralTracking = "Suivi Général"

    override val sessionDetails = "Détails de la session"
    override val sessionAltitude = "Altitude"
    override val sessionAvgSpeed = "Vitesse moyenne"
    override val sessionMaxSpeed = "Vitesse maximale"
    override val sessionExport = "Exporter GPX"
    override val sessionExportMessage = "Partagez votre parcours avec d'autres applications"
    override val sessionShare = "Partager"

    override val backgroundPermissionTitle = "Suivi en arrière-plan"
    override val backgroundPermissionMessage = "Pour continuer à enregistrer votre parcours lorsque l'application est en arrière-plan, l'autorisation de localisation \"Toujours\" est nécessaire."
    override val backgroundPermissionGrant = "Accorder l'autorisation"
    override val notificationTitle = "Suivi GPS actif"
    override val notificationChannelName = "Suivi GPS"

    override val trackingStart = "Démarrer"
    override val trackingPause = "Pause"
    override val trackingResume = "Reprendre"
    override val trackingStop = "Arrêter"
    override val trackingRecordingBadge = "ENREGISTREMENT"
    override val trackingCompleted = "Résumé de la session"
    override val trackingError = "Erreur de Suivi"
    override val trackingStatisticsTitle = "Statistiques en Direct"
    override val trackingSessionSummary = "Résumé de Session"
    override val trackingLatitude = "Latitude"
    override val trackingLongitude = "Longitude"
    override val trackingAccuracy = "Précision"
    override val trackingSpeed = "Vitesse"
    override val trackingAcquiringSignal = "Acquisition du signal GPS…"
    override val trackingStartNewSession = "Fermer"
    override val trackingRetry = "Réessayer"
    override val trackingFarTitle = "Vous êtes loin de l'itinéraire"
    override val trackingStartAnyway = "Démarrer quand même"
    override fun trackingSessionPrefix(id: String) = "Session : $id…"
    override fun trackingFarMessage(distance: String) = "Vous êtes à $distance km du point le plus proche de l'itinéraire. Voulez-vous démarrer le suivi quand même ?"
    override val trackingErrorPermission = "L'autorisation de localisation est nécessaire pour démarrer le suivi."
    override val trackingErrorGeneric = "Une erreur s'est produite"

    override val back = "Retour"
    override val openMenu = "Ouvrir le menu"
    override val trackingShowStatistics = "Afficher les statistiques"
    override val trackingGpsFollowEnabled = "Suivi GPS activé"
    override val trackingGpsFollowDisabled = "Suivi GPS désactivé"
    override val trackingDiscardTitle = "Supprimer le suivi ?"
    override val trackingDiscardMessage = "Toutes les données enregistrées seront définitivement perdues."
    override val trackingDiscard = "Supprimer"

    override val bottomBarMap = "Carte"
    override val bottomBarRoutes = "Itinéraires"
    override val bottomBarPoi = "POI"
    override val bottomBarNotebook = "Journal"
    override val bottomBarSettings = "Paramètres"
    override val settingsLanguageOption = "Langue"
    override val settingsAboutOption = "Camí de Cavalls"
    override val settingsContactUs = "Contactez-nous"
    override val completeRouteName = "Itinéraire Complet"
    override val completeRouteSubtitle = "Boucle complète"
}

private object StringsIt : Strings {
    override val homeNoRoutes = "Nessun percorso disponibile"
    override val homeDistance = "Distanza"
    override val homeElevation = "Dislivello"
    override val homeDuration = "Durata"
    override val difficultyLow = "Bassa"
    override val difficultyMedium = "Media"
    override val difficultyHigh = "Alta"

    override fun routeStage(number: Int) = "Tappa $number"
    override val routeViewDetails = "Dettagli del percorso"
    override val trackingDistance = "Distanza"
    override fun routeDistance(km: String) = "$km km"
    override val routeDifficulty = "Difficoltà"
    override val routeDetailElevationGain = "Dislivello Positivo"
    override val routeDetailElevationLoss = "Dislivello Negativo"
    override val routeDetailMaxAltitude = "Altitudine Massima"
    override val routeDetailMinAltitude = "Altitudine Minima"
    override val routeDetailAsphalt = "Asfalto"
    override val routeDetailEstimatedTime = "Tempo Stimato"
    override fun routeDetailMeters(meters: Int) = "$meters m"
    override fun routeDetailMinutes(minutes: Int) = "$minutes min"
    override fun routeDetailHours(hours: Double) = "${(hours * 10).toInt() / 10.0} h"
    override fun routeDetailPercent(percent: Int) = "$percent%"
    override val routeDetailDescription = "Descrizione"
    override val routeDetailElevationProfile = "Profilo Altimetrico"
    override val routeDetailNoElevationData = "Nessun dato altimetrico disponibile"
    override val elevationChartDistance = "Distanza"
    override val elevationChartElevation = "Altitudine"
    override val elevationChartMin = "Min"
    override val elevationChartMid = "Med"
    override val elevationChartMax = "Max"
    override val startPoint = "Punto di Partenza"
    override val endPoint = "Punto di Arrivo"

    override val settingsTitle = "Impostazioni"
    override val settingsLanguage = "Lingua"
    override val languageCa = "Català"
    override val languageEs = "Español"
    override val languageEn = "English"
    override val languageDe = "Deutsch"
    override val languageFr = "Français"
    override val languageIt = "Italiano"

    override val menuAbout = "Camí de Cavalls"
    override val menuRoutes = "Percorsi"
    override val menuMap = "Mappa"
    override val menuTracking = "Tracciamento"
    override val menuPOIs = "Punti di Interesse"
    override val menuNotebook = "Diario"
    override val menuSettings = "Impostazioni"

    override val aboutTitle = "Camí de Cavalls"
    override val routesTitle = "Percorsi"
    override val mapTitle = "Mappa"
    override val poisTitle = "Punti di Interesse"
    override val notebookTitle = "Diario"

    override val poiTypeBeach = "Zona Costiera"
    override val poiTypeNatural = "Area Naturale"
    override val poiTypeHistoric = "Sito Storico"
    override val poiTypeCommercial = "Attività commerciali"
    override val poisFiltersLabel = "Filtra POI"

    override val aboutWelcome = "Benvenuto al Camí de Cavalls"
    override val aboutDescription = "Il Camí de Cavalls è uno dei modi migliori per vivere Minorca in tutte le sue dimensioni. Questo storico sentiero costiero circonda l'isola offrendo un viaggio immersivo attraverso paesaggi naturali, gastronomia, cultura e storia.\n\nIl percorso comprende 20 tappe distintive, ognuna progettata per mostrare diversi aspetti ambientali e storici dell'isola."
    override val aboutUNESCO = "Minorca è stata designata Riserva della Biosfera UNESCO nell'ottobre 1993, riconoscendo l'equilibrio tra la preservazione del suo ricco patrimonio naturale e culturale e lo sviluppo economico."
    override val aboutLength = "185 km di escursionismo costiero"
    override val aboutStages = "20 tappe · GR-223"
    override val aboutCTA = "Conoscere è amare. Scopri i valori ambientali che rendono Minorca un luogo così eccezionale."
    override val aboutExplore = "Inizia la tua avventura"

    override val notebookNoSessions = "Non hai ancora sessioni registrate"
    override val notebookStartTracking = "Inizia Tracciamento"
    override val notebookDeleteTitle = "Elimina sessione"
    override val notebookDeleteMessage = "Sei sicuro di voler eliminare questa sessione? Questa azione non può essere annullata."
    override val notebookDeleteConfirm = "Elimina"
    override val notebookCancel = "Annulla"
    override val notebookSaveSessionTitle = "Salva sessione"
    override val notebookSessionName = "Nome della sessione"
    override val notebookSave = "Salva"
    override val notebookGeneralTracking = "Tracciamento Generale"

    override val sessionDetails = "Dettagli sessione"
    override val sessionAltitude = "Altitudine"
    override val sessionAvgSpeed = "Velocità media"
    override val sessionMaxSpeed = "Velocità massima"
    override val sessionExport = "Esporta GPX"
    override val sessionExportMessage = "Condividi il tuo percorso con altre app"
    override val sessionShare = "Condividi"

    override val backgroundPermissionTitle = "Tracciamento in background"
    override val backgroundPermissionMessage = "Per continuare a registrare il percorso quando l'app è in background, è necessario il permesso di posizione \"Sempre\"."
    override val backgroundPermissionGrant = "Concedi permesso"
    override val notificationTitle = "Tracciamento GPS attivo"
    override val notificationChannelName = "Tracciamento GPS"

    override val trackingStart = "Inizia"
    override val trackingPause = "Pausa"
    override val trackingResume = "Riprendi"
    override val trackingStop = "Ferma"
    override val trackingRecordingBadge = "REGISTRAZIONE"
    override val trackingCompleted = "Riepilogo sessione"
    override val trackingError = "Errore Tracciamento"
    override val trackingStatisticsTitle = "Statistiche Live"
    override val trackingSessionSummary = "Riepilogo Sessione"
    override val trackingLatitude = "Latitudine"
    override val trackingLongitude = "Longitudine"
    override val trackingAccuracy = "Precisione"
    override val trackingSpeed = "Velocità"
    override val trackingAcquiringSignal = "Acquisizione segnale GPS…"
    override val trackingStartNewSession = "Chiudi"
    override val trackingRetry = "Riprova"
    override val trackingFarTitle = "Sei lontano dal percorso"
    override val trackingStartAnyway = "Inizia comunque"
    override fun trackingSessionPrefix(id: String) = "Sessione: $id…"
    override fun trackingFarMessage(distance: String) = "Sei a $distance km dal punto più vicino del percorso. Vuoi iniziare il tracciamento comunque?"
    override val trackingErrorPermission = "Permesso di posizione necessario per iniziare il tracciamento."
    override val trackingErrorGeneric = "Si è verificato un errore"

    override val back = "Indietro"
    override val openMenu = "Apri menu"
    override val trackingShowStatistics = "Mostra statistiche"
    override val trackingGpsFollowEnabled = "Segui GPS attivato"
    override val trackingGpsFollowDisabled = "Segui GPS disattivato"
    override val trackingDiscardTitle = "Scartare il tracciamento?"
    override val trackingDiscardMessage = "Tutti i dati registrati andranno persi definitivamente."
    override val trackingDiscard = "Scarta"

    override val bottomBarMap = "Mappa"
    override val bottomBarRoutes = "Percorsi"
    override val bottomBarPoi = "POI"
    override val bottomBarNotebook = "Diario"
    override val bottomBarSettings = "Impostazioni"
    override val settingsLanguageOption = "Lingua"
    override val settingsAboutOption = "Camí de Cavalls"
    override val settingsContactUs = "Contattaci"
    override val completeRouteName = "Percorso Completo"
    override val completeRouteSubtitle = "Anello completo"
}
