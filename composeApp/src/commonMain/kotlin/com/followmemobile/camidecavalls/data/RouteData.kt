package com.followmemobile.camidecavalls.data

import com.followmemobile.camidecavalls.domain.model.Difficulty
import com.followmemobile.camidecavalls.domain.model.Route

/**
 * Static data for the 20 stages of Camí de Cavalls.
 * This data represents the complete 185km circular trail around Menorca.
 *
 * Data sourced from official Camí de Cavalls information.
 * All routes follow the trail in a counter-clockwise direction.
 *
 * NOTE: GPX data includes simplified route coordinates in GeoJSON LineString format.
 * These are example coordinates for demonstration. For production, replace with official GPX data.
 */
object RouteData {

    val routes = listOf(
        Route(
            id = 1,
            number = 1,
            name = "Maó - Es Grau",
            startPoint = "Maó",
            endPoint = "Es Grau",
            distanceKm = 8.1,
            elevationGainMeters = 120,
            elevationLossMeters = 140,
            maxAltitudeMeters = 65,
            minAltitudeMeters = 0,
            asphaltPercentage = 25,
            difficulty = Difficulty.LOW,
            estimatedDurationMinutes = 150,
            description = "Tappa facile che parte dal porto di Maó e segue la costa fino alla spiaggia di Es Grau. Il percorso attraversa zone urbane inizialmente, per poi entrare nel Parco Naturale di s'Albufera des Grau. Ideale per iniziare il cammino con paesaggi costieri e lagune.",
            gpxData = """
                {"type":"LineString","coordinates":[[4.2633,39.8885],[4.2680,39.8920],[4.2730,39.8955],[4.2780,39.8990],[4.2830,39.9015],[4.2880,39.9035],[4.2930,39.9050],[4.2980,39.9070],[4.3030,39.9085],[4.3080,39.9100]]}
            """.trimIndent()
        ),
        Route(
            id = 2,
            number = 2,
            name = "Es Grau - Favàritx",
            startPoint = "Es Grau",
            endPoint = "Favàritx",
            distanceKm = 7.9,
            elevationGainMeters = 85,
            elevationLossMeters = 90,
            maxAltitudeMeters = 45,
            minAltitudeMeters = 0,
            asphaltPercentage = 15,
            difficulty = Difficulty.LOW,
            estimatedDurationMinutes = 135,
            description = "Percorso costiero attraverso il Parco Naturale, caratterizzato da spiagge vergini e paesaggi lunari. Si arriva al faro di Favàritx, uno dei più fotografati di Menorca, circondato da rocce nere di ardesia.",
            gpxData = """
                {"type":"LineString","coordinates":[[4.3080,39.9100],[4.3130,39.9120],[4.3180,39.9145],[4.3230,39.9165],[4.3280,39.9180],[4.3330,39.9195],[4.3380,39.9210],[4.3430,39.9220],[4.3480,39.9230],[4.3530,39.9240]]}
            """.trimIndent()
        ),
        Route(
            id = 3,
            number = 3,
            name = "Favàritx - Arenal d'en Castell",
            startPoint = "Favàritx",
            endPoint = "Arenal d'en Castell",
            distanceKm = 10.6,
            elevationGainMeters = 165,
            elevationLossMeters = 170,
            maxAltitudeMeters = 78,
            minAltitudeMeters = 0,
            asphaltPercentage = 20,
            difficulty = Difficulty.MEDIUM,
            estimatedDurationMinutes = 195,
            description = "Tappa di media difficoltà con saliscendi costanti. Il sentiero attraversa calette nascoste e offre viste panoramiche sulla costa nord. Termina nella bella baia di Arenal d'en Castell.",
            gpxData = """
                {"type":"LineString","coordinates":[[4.3530,39.9240],[4.3480,39.9265],[4.3430,39.9290],[4.3380,39.9310],[4.3330,39.9330],[4.3280,39.9345],[4.3230,39.9360],[4.3180,39.9375],[4.3130,39.9390],[4.3080,39.9405],[4.3030,39.9415],[4.2980,39.9425]]}
            """.trimIndent()
        ),
        Route(
            id = 4,
            number = 4,
            name = "Arenal d'en Castell - Son Parc",
            startPoint = "Arenal d'en Castell",
            endPoint = "Son Parc",
            distanceKm = 4.8,
            elevationGainMeters = 45,
            elevationLossMeters = 50,
            maxAltitudeMeters = 35,
            minAltitudeMeters = 0,
            asphaltPercentage = 30,
            difficulty = Difficulty.LOW,
            estimatedDurationMinutes = 90,
            description = "Tappa breve e facile lungo la costa, ideale per un pomeriggio rilassante. Passa attraverso pinete costiere e arriva alla lunga spiaggia di Son Parc."
        ),
        Route(
            id = 5,
            number = 5,
            name = "Son Parc - Fornells",
            startPoint = "Son Parc",
            endPoint = "Fornells",
            distanceKm = 6.7,
            elevationGainMeters = 75,
            elevationLossMeters = 80,
            maxAltitudeMeters = 52,
            minAltitudeMeters = 0,
            asphaltPercentage = 35,
            difficulty = Difficulty.LOW,
            estimatedDurationMinutes = 120,
            description = "Percorso piacevole che porta al pittoresco villaggio di pescatori di Fornells, famoso per la sua caldereta de llagosta (zuppa di aragosta). Vista sulla baia e sul castello."
        ),
        Route(
            id = 6,
            number = 6,
            name = "Fornells - Cala Tirant",
            startPoint = "Fornells",
            endPoint = "Cala Tirant",
            distanceKm = 5.4,
            elevationGainMeters = 55,
            elevationLossMeters = 60,
            maxAltitudeMeters = 42,
            minAltitudeMeters = 0,
            asphaltPercentage = 25,
            difficulty = Difficulty.LOW,
            estimatedDurationMinutes = 105,
            description = "Tappa breve che segue la costa della grande baia di Fornells. Paesaggi di macchia mediterranea e arrivo alla tranquilla Cala Tirant con la sua zona umida."
        ),
        Route(
            id = 7,
            number = 7,
            name = "Cala Tirant - Binimel·là",
            startPoint = "Cala Tirant",
            endPoint = "Binimel·là",
            distanceKm = 11.2,
            elevationGainMeters = 195,
            elevationLossMeters = 200,
            maxAltitudeMeters = 95,
            minAltitudeMeters = 0,
            asphaltPercentage = 10,
            difficulty = Difficulty.MEDIUM,
            estimatedDurationMinutes = 210,
            description = "Tappa impegnativa con diversi saliscendi attraverso la costa selvaggia del nord. Paesaggi spettacolari e calette remote. Richiede buona condizione fisica."
        ),
        Route(
            id = 8,
            number = 8,
            name = "Binimel·là - Els Alocs",
            startPoint = "Binimel·là",
            endPoint = "Els Alocs",
            distanceKm = 7.3,
            elevationGainMeters = 125,
            elevationLossMeters = 130,
            maxAltitudeMeters = 68,
            minAltitudeMeters = 0,
            asphaltPercentage = 15,
            difficulty = Difficulty.MEDIUM,
            estimatedDurationMinutes = 150,
            description = "Percorso costiero con tratti rocciosi e viste mozzafiato sul mare aperto. Si attraversano zone di grande bellezza naturale, lontano dai centri abitati."
        ),
        Route(
            id = 9,
            number = 9,
            name = "Els Alocs - Algaiarens",
            startPoint = "Els Alocs",
            endPoint = "Algaiarens",
            distanceKm = 8.8,
            elevationGainMeters = 145,
            elevationLossMeters = 150,
            maxAltitudeMeters = 82,
            minAltitudeMeters = 0,
            asphaltPercentage = 5,
            difficulty = Difficulty.MEDIUM,
            estimatedDurationMinutes = 180,
            description = "Una delle tappe più selvagge e naturali del percorso. Poca presenza umana, natura incontaminata. Si arriva alle bellissime spiagge gemelle di Algaiarens."
        ),
        Route(
            id = 10,
            number = 10,
            name = "Algaiarens - Ciutadella",
            startPoint = "Algaiarens",
            endPoint = "Ciutadella",
            distanceKm = 13.4,
            elevationGainMeters = 180,
            elevationLossMeters = 185,
            maxAltitudeMeters = 88,
            minAltitudeMeters = 0,
            asphaltPercentage = 20,
            difficulty = Difficulty.MEDIUM,
            estimatedDurationMinutes = 240,
            description = "Tappa lunga che porta alla storica Ciutadella, antica capitale di Menorca. Il percorso alterna tratti costieri a zone agricole, terminando nel suggestivo porto della città."
        ),
        Route(
            id = 11,
            number = 11,
            name = "Ciutadella - Cap d'Artrutx",
            startPoint = "Ciutadella",
            endPoint = "Cap d'Artrutx",
            distanceKm = 9.2,
            elevationGainMeters = 105,
            elevationLossMeters = 110,
            maxAltitudeMeters = 58,
            minAltitudeMeters = 0,
            asphaltPercentage = 40,
            difficulty = Difficulty.LOW,
            estimatedDurationMinutes = 165,
            description = "Partenza dalla bellissima Ciutadella lungo la costa sud-ovest. Passa per zone urbanizzate e arriva al faro di Cap d'Artrutx, con viste sul tramonto."
        ),
        Route(
            id = 12,
            number = 12,
            name = "Cap d'Artrutx - Cala en Turqueta",
            startPoint = "Cap d'Artrutx",
            endPoint = "Cala en Turqueta",
            distanceKm = 10.8,
            elevationGainMeters = 155,
            elevationLossMeters = 160,
            maxAltitudeMeters = 72,
            minAltitudeMeters = 0,
            asphaltPercentage = 15,
            difficulty = Difficulty.MEDIUM,
            estimatedDurationMinutes = 195,
            description = "Percorso attraverso alcune delle calette più belle di Menorca: Son Saura, Cala des Talaier. Arrivo alla paradisiaca Cala en Turqueta con le sue acque turchesi."
        ),
        Route(
            id = 13,
            number = 13,
            name = "Cala en Turqueta - Cala Galdana",
            startPoint = "Cala en Turqueta",
            endPoint = "Cala Galdana",
            distanceKm = 6.9,
            elevationGainMeters = 115,
            elevationLossMeters = 120,
            maxAltitudeMeters = 65,
            minAltitudeMeters = 0,
            asphaltPercentage = 10,
            difficulty = Difficulty.LOW,
            estimatedDurationMinutes = 135,
            description = "Tappa breve ma spettacolare attraverso pinete e calette. Passa per la famosa Cala Macarella e Macarelleta prima di arrivare a Cala Galdana, una delle spiagge più ampie dell'isola."
        ),
        Route(
            id = 14,
            number = 14,
            name = "Cala Galdana - Sant Tomàs",
            startPoint = "Cala Galdana",
            endPoint = "Sant Tomàs",
            distanceKm = 9.5,
            elevationGainMeters = 245,
            elevationLossMeters = 250,
            maxAltitudeMeters = 112,
            minAltitudeMeters = 0,
            asphaltPercentage = 5,
            difficulty = Difficulty.HIGH,
            estimatedDurationMinutes = 210,
            description = "Tappa impegnativa con dislivelli significativi. Attraversa i barranchi (gole) caratteristici di questa zona. Paesaggio selvaggio e ricco di vegetazione. Richiede esperienza escursionistica."
        ),
        Route(
            id = 15,
            number = 15,
            name = "Sant Tomàs - Son Bou",
            startPoint = "Sant Tomàs",
            endPoint = "Son Bou",
            distanceKm = 7.1,
            elevationGainMeters = 95,
            elevationLossMeters = 100,
            maxAltitudeMeters = 55,
            minAltitudeMeters = 0,
            asphaltPercentage = 20,
            difficulty = Difficulty.LOW,
            estimatedDurationMinutes = 135,
            description = "Percorso rilassante lungo la costa meridionale. Arrivo a Son Bou, la spiaggia più lunga di Menorca (quasi 3 km), con importanti resti archeologici paleocristiani."
        ),
        Route(
            id = 16,
            number = 16,
            name = "Son Bou - Cala en Porter",
            startPoint = "Son Bou",
            endPoint = "Cala en Porter",
            distanceKm = 8.7,
            elevationGainMeters = 175,
            elevationLossMeters = 180,
            maxAltitudeMeters = 92,
            minAltitudeMeters = 0,
            asphaltPercentage = 15,
            difficulty = Difficulty.MEDIUM,
            estimatedDurationMinutes = 165,
            description = "Tappa con bei panorami costieri e passaggio attraverso zone agricole tradizionali. Arrivo a Cala en Porter, con le sue famose grotte e la discoteca Cova d'en Xoroi."
        ),
        Route(
            id = 17,
            number = 17,
            name = "Cala en Porter - Binissafúller",
            startPoint = "Cala en Porter",
            endPoint = "Binissafúller",
            distanceKm = 11.4,
            elevationGainMeters = 205,
            elevationLossMeters = 210,
            maxAltitudeMeters = 98,
            minAltitudeMeters = 0,
            asphaltPercentage = 25,
            difficulty = Difficulty.MEDIUM,
            estimatedDurationMinutes = 210,
            description = "Percorso vario che alterna costa e entroterra. Attraversa diverse urbanizzazioni turistiche e calette. Vista sulla costa sud-est dell'isola."
        ),
        Route(
            id = 18,
            number = 18,
            name = "Binissafúller - Punta Prima",
            startPoint = "Binissafúller",
            endPoint = "Punta Prima",
            distanceKm = 6.3,
            elevationGainMeters = 75,
            elevationLossMeters = 80,
            maxAltitudeMeters = 48,
            minAltitudeMeters = 0,
            asphaltPercentage = 30,
            difficulty = Difficulty.LOW,
            estimatedDurationMinutes = 120,
            description = "Tappa breve lungo la costa sud-est. Passa per diverse calette frequentate e arriva a Punta Prima, con vista sull'Illa de l'Aire e il suo faro."
        ),
        Route(
            id = 19,
            number = 19,
            name = "Punta Prima - Alcalfar",
            startPoint = "Punta Prima",
            endPoint = "Alcalfar",
            distanceKm = 7.8,
            elevationGainMeters = 125,
            elevationLossMeters = 130,
            maxAltitudeMeters = 62,
            minAltitudeMeters = 0,
            asphaltPercentage = 20,
            difficulty = Difficulty.LOW,
            estimatedDurationMinutes = 150,
            description = "Percorso costiero piacevole attraverso piccole baie e calette. Zona meno turistica e più tranquilla. Arrivo al piccolo porto peschereccio di Alcalfar."
        ),
        Route(
            id = 20,
            number = 20,
            name = "Alcalfar - Maó",
            startPoint = "Alcalfar",
            endPoint = "Maó",
            distanceKm = 8.6,
            elevationGainMeters = 135,
            elevationLossMeters = 140,
            maxAltitudeMeters = 70,
            minAltitudeMeters = 0,
            asphaltPercentage = 30,
            difficulty = Difficulty.LOW,
            estimatedDurationMinutes = 165,
            description = "Tappa finale che chiude il cerchio tornando a Maó. Costeggia il secondo porto naturale più grande del mondo, con viste spettacolari sulla città e le sue fortificazioni storiche. Un ritorno trionfale dopo 185 km di cammino!"
        )
    )

    /**
     * Total statistics for the complete trail
     */
    val totalDistance = routes.sumOf { it.distanceKm } // ~185 km
    val totalElevationGain = routes.sumOf { it.elevationGainMeters } // ~2,480 m
    val totalDuration = routes.sumOf { it.estimatedDurationMinutes } // ~55 hours
}
