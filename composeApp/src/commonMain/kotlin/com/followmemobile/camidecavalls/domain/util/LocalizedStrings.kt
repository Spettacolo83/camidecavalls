package com.followmemobile.camidecavalls.domain.util

/**
 * Provides localized strings based on the selected language.
 * This replaces Compose Resources stringResource() for app-controlled localization.
 */
class LocalizedStrings(private val languageCode: String) {

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
}
