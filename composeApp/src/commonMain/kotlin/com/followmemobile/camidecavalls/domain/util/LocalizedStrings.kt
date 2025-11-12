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

    // Drawer Menu
    val menuAbout: String get() = strings.menuAbout
    val menuRoutes: String get() = strings.menuRoutes
    val menuMap: String get() = strings.menuMap
    val menuPOIs: String get() = strings.menuPOIs
    val menuNotebook: String get() = strings.menuNotebook
    val menuSettings: String get() = strings.menuSettings

    // Screen Titles (unified with menu labels)
    val aboutTitle: String get() = strings.aboutTitle
    val routesTitle: String get() = strings.routesTitle
    val mapTitle: String get() = strings.mapTitle
    val poisTitle: String get() = strings.poisTitle
    val notebookTitle: String get() = strings.notebookTitle

    // About Screen
    val aboutWelcome: String get() = strings.aboutWelcome
    val aboutDescription: String get() = strings.aboutDescription
    val aboutUNESCO: String get() = strings.aboutUNESCO
    val aboutLength: String get() = strings.aboutLength
    val aboutStages: String get() = strings.aboutStages
    val aboutCTA: String get() = strings.aboutCTA
    val aboutExplore: String get() = strings.aboutExplore
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

    // Drawer Menu
    val menuAbout: String
    val menuRoutes: String
    val menuMap: String
    val menuPOIs: String
    val menuNotebook: String
    val menuSettings: String

    // Screen Titles (unified with menu labels)
    val aboutTitle: String
    val routesTitle: String
    val mapTitle: String
    val poisTitle: String
    val notebookTitle: String

    // About Screen
    val aboutWelcome: String
    val aboutDescription: String
    val aboutUNESCO: String
    val aboutLength: String
    val aboutStages: String
    val aboutCTA: String
    val aboutExplore: String
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

    override val menuAbout = "Camí de Cavalls"
    override val menuRoutes = "Rutes"
    override val menuMap = "Mapa"
    override val menuPOIs = "Punts d'Interès"
    override val menuNotebook = "Quadern"
    override val menuSettings = "Configuració"

    override val aboutTitle = "Camí de Cavalls"
    override val routesTitle = "Rutes"
    override val mapTitle = "Mapa"
    override val poisTitle = "Punts d'Interès"
    override val notebookTitle = "Quadern"

    override val aboutWelcome = "Benvingut al Camí de Cavalls"
    override val aboutDescription = "El Camí de Cavalls és una de les millors maneres de viure Menorca en totes les seves dimensions. Aquest històric camí costaner rodeja l'illa oferint un viatge immersiu per paisatges naturals, gastronomia, cultura i història.\n\nEl recorregut comprèn 20 etapes distintives, cadascuna dissenyada per mostrar diferents aspectes ambientals i històrics de l'illa."
    override val aboutUNESCO = "Menorca va ser declarada Reserva de la Biosfera per la UNESCO l'octubre de 1993, reconeixent l'equilibri entre la preservació del seu ric patrimoni natural i cultural i el desenvolupament econòmic."
    override val aboutLength = "185 km de senderisme costaner"
    override val aboutStages = "20 etapes · GR-223"
    override val aboutCTA = "Conèixer és estimar. Descobreix els valors ambientals que fan de Menorca un lloc tan excepcional."
    override val aboutExplore = "Comença la teva aventura"
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

    override val menuAbout = "Camí de Cavalls"
    override val menuRoutes = "Rutas"
    override val menuMap = "Mapa"
    override val menuPOIs = "Puntos de Interés"
    override val menuNotebook = "Diario"
    override val menuSettings = "Configuración"

    override val aboutTitle = "Camí de Cavalls"
    override val routesTitle = "Rutas"
    override val mapTitle = "Mapa"
    override val poisTitle = "Puntos de Interés"
    override val notebookTitle = "Diario"

    override val aboutWelcome = "Bienvenido al Camí de Cavalls"
    override val aboutDescription = "El Camí de Cavalls es una de las mejores maneras de experimentar Menorca en todas sus dimensiones. Este histórico sendero costero rodea la isla ofreciendo un viaje inmersivo a través de paisajes naturales, gastronomía, cultura e historia.\n\nEl recorrido comprende 20 etapas distintivas, cada una diseñada para mostrar diferentes aspectos ambientales e históricos de la isla."
    override val aboutUNESCO = "Menorca fue declarada Reserva de la Biosfera por la UNESCO en octubre de 1993, reconociendo el equilibrio entre la preservación de su rico patrimonio natural y cultural y el desarrollo económico."
    override val aboutLength = "185 km de senderismo costero"
    override val aboutStages = "20 etapas · GR-223"
    override val aboutCTA = "Conocer es amar. Descubre los valores ambientales que hacen de Menorca un lugar tan excepcional."
    override val aboutExplore = "Comienza tu aventura"
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

    override val menuAbout = "Camí de Cavalls"
    override val menuRoutes = "Routes"
    override val menuMap = "Map"
    override val menuPOIs = "Points of Interest"
    override val menuNotebook = "Notebook"
    override val menuSettings = "Settings"

    override val aboutTitle = "Camí de Cavalls"
    override val routesTitle = "Routes"
    override val mapTitle = "Map"
    override val poisTitle = "Points of Interest"
    override val notebookTitle = "Notebook"

    override val aboutWelcome = "Welcome to Camí de Cavalls"
    override val aboutDescription = "The Camí de Cavalls is one of the best ways to experience Menorca in all its dimensions. This historic coastal path encircles the island, offering an immersive journey through natural landscapes, gastronomy, culture, and history.\n\nThe trail comprises 20 distinctive stages, each designed to showcase different environmental and historical aspects of the island."
    override val aboutUNESCO = "Menorca has held UNESCO Biosphere Reserve designation since October 1993, recognizing the balance between preserving its rich natural and cultural heritage while supporting economic development."
    override val aboutLength = "185 km of coastal hiking"
    override val aboutStages = "20 stages · GR-223"
    override val aboutCTA = "Knowing is loving. Discover the environmental values that make Menorca such an exceptional place."
    override val aboutExplore = "Start your adventure"
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

    override val menuAbout = "Camí de Cavalls"
    override val menuRoutes = "Routen"
    override val menuMap = "Karte"
    override val menuPOIs = "Sehenswürdigkeiten"
    override val menuNotebook = "Tagebuch"
    override val menuSettings = "Einstellungen"

    override val aboutTitle = "Camí de Cavalls"
    override val routesTitle = "Routen"
    override val mapTitle = "Karte"
    override val poisTitle = "Sehenswürdigkeiten"
    override val notebookTitle = "Tagebuch"

    override val aboutWelcome = "Willkommen beim Camí de Cavalls"
    override val aboutDescription = "Der Camí de Cavalls ist eine der besten Möglichkeiten, Menorca in all seinen Dimensionen zu erleben. Dieser historische Küstenweg umrundet die Insel und bietet eine immersive Reise durch Naturlandschaften, Gastronomie, Kultur und Geschichte.\n\nDer Weg umfasst 20 unterschiedliche Etappen, jede entworfen, um verschiedene Umwelt- und historische Aspekte der Insel zu zeigen."
    override val aboutUNESCO = "Menorca wurde im Oktober 1993 von der UNESCO zum Biosphärenreservat erklärt und würdigt damit das Gleichgewicht zwischen der Erhaltung des reichen Natur- und Kulturerbes und der wirtschaftlichen Entwicklung."
    override val aboutLength = "185 km Küstenwanderung"
    override val aboutStages = "20 Etappen · GR-223"
    override val aboutCTA = "Kennen bedeutet lieben. Entdecken Sie die Umweltwerte, die Menorca zu einem so außergewöhnlichen Ort machen."
    override val aboutExplore = "Starten Sie Ihr Abenteuer"
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

    override val menuAbout = "Camí de Cavalls"
    override val menuRoutes = "Itinéraires"
    override val menuMap = "Carte"
    override val menuPOIs = "Points d'Intérêt"
    override val menuNotebook = "Carnet"
    override val menuSettings = "Paramètres"

    override val aboutTitle = "Camí de Cavalls"
    override val routesTitle = "Itinéraires"
    override val mapTitle = "Carte"
    override val poisTitle = "Points d'Intérêt"
    override val notebookTitle = "Carnet"

    override val aboutWelcome = "Bienvenue sur le Camí de Cavalls"
    override val aboutDescription = "Le Camí de Cavalls est l'une des meilleures façons de découvrir Minorque dans toutes ses dimensions. Ce sentier côtier historique fait le tour de l'île, offrant un voyage immersif à travers les paysages naturels, la gastronomie, la culture et l'histoire.\n\nLe parcours comprend 20 étapes distinctives, chacune conçue pour présenter différents aspects environnementaux et historiques de l'île."
    override val aboutUNESCO = "Minorque a été désignée Réserve de Biosphère de l'UNESCO en octobre 1993, reconnaissant l'équilibre entre la préservation de son riche patrimoine naturel et culturel et le développement économique."
    override val aboutLength = "185 km de randonnée côtière"
    override val aboutStages = "20 étapes · GR-223"
    override val aboutCTA = "Connaître c'est aimer. Découvrez les valeurs environnementales qui font de Minorque un lieu si exceptionnel."
    override val aboutExplore = "Commencez votre aventure"
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

    override val menuAbout = "Camí de Cavalls"
    override val menuRoutes = "Percorsi"
    override val menuMap = "Mappa"
    override val menuPOIs = "Punti di Interesse"
    override val menuNotebook = "Diario"
    override val menuSettings = "Impostazioni"

    override val aboutTitle = "Camí de Cavalls"
    override val routesTitle = "Percorsi"
    override val mapTitle = "Mappa"
    override val poisTitle = "Punti di Interesse"
    override val notebookTitle = "Diario"

    override val aboutWelcome = "Benvenuto al Camí de Cavalls"
    override val aboutDescription = "Il Camí de Cavalls è uno dei modi migliori per vivere Minorca in tutte le sue dimensioni. Questo storico sentiero costiero circonda l'isola offrendo un viaggio immersivo attraverso paesaggi naturali, gastronomia, cultura e storia.\n\nIl percorso comprende 20 tappe distintive, ognuna progettata per mostrare diversi aspetti ambientali e storici dell'isola."
    override val aboutUNESCO = "Minorca è stata designata Riserva della Biosfera UNESCO nell'ottobre 1993, riconoscendo l'equilibrio tra la preservazione del suo ricco patrimonio naturale e culturale e lo sviluppo economico."
    override val aboutLength = "185 km di escursionismo costiero"
    override val aboutStages = "20 tappe · GR-223"
    override val aboutCTA = "Conoscere è amare. Scopri i valori ambientali che rendono Minorca un luogo così eccezionale."
    override val aboutExplore = "Inizia la tua avventura"
}
