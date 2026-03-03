package com.followmemobile.camidecavalls.presentation.splash

import androidx.compose.ui.geometry.Offset

/**
 * Simplified route paths for splash screen animation.
 * Coordinates are in the SVG coordinate space of the Menorca silhouette path,
 * aligned using ICP (Iterative Closest Point) to match the silhouette outline.
 * They use the same translate+scale transform as the silhouette for rendering.
 */
object RoutePathsData {

    val routes: List<List<Offset>> = listOf(
        // Route 1: Maó - Es Grau
        listOf(
            Offset(781.22f, 414.16f), Offset(790.98f, 413.58f), Offset(801.89f, 406.61f), Offset(812.21f, 405.03f),
            Offset(828.21f, 389.73f), Offset(826.55f, 383.29f), Offset(819.87f, 384.74f), Offset(817.94f, 382.44f),
            Offset(821.23f, 371.32f), Offset(819.4f, 371.11f), Offset(818.98f, 363.5f), Offset(812.02f, 352.05f),
            Offset(804.08f, 347.6f), Offset(802.18f, 342.44f), Offset(793.55f, 342.86f), Offset(793.16f, 337.52f),
            Offset(788.31f, 334.32f), Offset(791.21f, 322.97f), Offset(789.62f, 317.99f)
        ),
        // Route 2: Es Grau - Favàritx
        listOf(
            Offset(789.62f, 317.99f), Offset(787.26f, 317.73f), Offset(784.63f, 310.0f), Offset(788.33f, 302.85f),
            Offset(784.35f, 300.42f), Offset(786.77f, 295.18f), Offset(781.04f, 290.8f), Offset(781.72f, 287.41f),
            Offset(771.59f, 286.65f), Offset(769.07f, 274.37f), Offset(766.11f, 271.05f), Offset(768.33f, 261.5f),
            Offset(775.85f, 253.31f), Offset(772.77f, 238.82f), Offset(769.25f, 233.12f), Offset(761.55f, 230.61f),
            Offset(771.9f, 219.42f)
        ),
        // Route 3: Favàritx - Arenal d'en Castell
        listOf(
            Offset(771.9f, 219.42f), Offset(751.71f, 224.22f), Offset(745.3f, 231.88f), Offset(735.38f, 223.87f),
            Offset(732.77f, 229.81f), Offset(723.41f, 229.25f), Offset(720.56f, 236.69f), Offset(717.13f, 236.42f),
            Offset(712.51f, 246.5f), Offset(710.55f, 245.26f), Offset(705.11f, 252.46f), Offset(705.22f, 256.52f),
            Offset(698.33f, 248.33f), Offset(696.05f, 238.09f), Offset(697.28f, 230.06f), Offset(691.85f, 229.29f),
            Offset(691.56f, 220.97f), Offset(686.43f, 219.97f), Offset(680.11f, 207.66f), Offset(670.14f, 208.74f),
            Offset(667.98f, 194.36f), Offset(671.34f, 181.6f), Offset(660.67f, 182.1f), Offset(655.29f, 177.21f)
        ),
        // Route 4: Arenal d'en Castell - Cala Tirant
        listOf(
            Offset(655.29f, 177.21f), Offset(642.88f, 174.49f), Offset(637.35f, 167.92f), Offset(634.53f, 171.09f),
            Offset(629.62f, 168.05f), Offset(629.01f, 164.62f), Offset(626.34f, 167.67f), Offset(615.7f, 165.63f),
            Offset(607.01f, 171.34f), Offset(599.81f, 168.2f), Offset(594.43f, 178.46f), Offset(594.07f, 190.48f),
            Offset(580.51f, 184.27f), Offset(569.77f, 186.68f), Offset(564.96f, 177.52f), Offset(572.41f, 152.71f),
            Offset(567.96f, 148.21f), Offset(559.78f, 148.48f), Offset(554.74f, 144.35f)
        ),
        // Route 5: Cala Tirant - Els Alocs
        listOf(
            Offset(554.74f, 144.35f), Offset(551.59f, 147.01f), Offset(545.24f, 145.75f), Offset(543.99f, 149.88f),
            Offset(538.58f, 144.35f), Offset(541.04f, 138.37f), Offset(538.71f, 121.96f), Offset(534.68f, 123.44f),
            Offset(529.98f, 118.64f), Offset(529.61f, 107.11f), Offset(525.78f, 102.77f), Offset(523.19f, 109.39f),
            Offset(517.96f, 111.19f), Offset(513.67f, 120.24f), Offset(508.14f, 117.1f), Offset(504.47f, 123.09f),
            Offset(500.43f, 122.77f), Offset(492.82f, 128.86f), Offset(490.47f, 125.59f), Offset(480.63f, 126.07f),
            Offset(476.19f, 133.55f), Offset(476.67f, 137.36f)
        ),
        // Route 6: Els Alocs - Algaiarens
        listOf(
            Offset(476.67f, 137.36f), Offset(471.7f, 140.03f), Offset(467.02f, 138.72f), Offset(442.85f, 122.72f),
            Offset(437.13f, 121.93f), Offset(433.48f, 126.85f), Offset(421.37f, 130.76f), Offset(422.57f, 137.14f),
            Offset(416.86f, 136.82f), Offset(411.62f, 131.65f), Offset(408.82f, 133.5f), Offset(409.51f, 138.5f),
            Offset(404.86f, 141.41f), Offset(402.52f, 140.14f), Offset(401.43f, 142.67f), Offset(398.04f, 139.92f),
            Offset(393.13f, 147.08f), Offset(382.52f, 147.47f)
        ),
        // Route 7: Algaiarens - Cala Morell
        listOf(
            Offset(382.52f, 147.47f), Offset(370.14f, 148.81f), Offset(368.44f, 153.09f), Offset(362.96f, 156.12f),
            Offset(360.23f, 150.99f), Offset(352.26f, 145.81f), Offset(351.43f, 154.38f), Offset(347.49f, 159.55f),
            Offset(347.11f, 164.44f), Offset(344.88f, 160.53f), Offset(336.53f, 160.29f), Offset(332.41f, 168.16f),
            Offset(323.93f, 171.9f), Offset(324.24f, 163.74f), Offset(318.54f, 160.09f), Offset(313.38f, 165.21f),
            Offset(300.16f, 169.64f), Offset(295.48f, 175.19f), Offset(291.29f, 168.08f), Offset(289.48f, 173.47f),
            Offset(285.41f, 174.06f)
        ),
        // Route 8: Cala Morell - Punta Nati
        listOf(
            Offset(285.41f, 174.06f), Offset(282.53f, 177.54f), Offset(278.06f, 174.15f), Offset(274.29f, 161.3f),
            Offset(266.9f, 165.67f), Offset(264.04f, 161.02f), Offset(242.97f, 154.67f), Offset(242.5f, 151.01f),
            Offset(227.47f, 166.01f)
        ),
        // Route 9: Punta Nati - Ciutadella
        listOf(
            Offset(227.47f, 166.01f), Offset(225.39f, 160.24f), Offset(214.83f, 159.88f), Offset(210.15f, 173.78f),
            Offset(206.16f, 176.15f), Offset(202.79f, 173.82f), Offset(201.67f, 165.89f), Offset(193.11f, 165.99f),
            Offset(187.58f, 161.17f), Offset(168.73f, 165.48f), Offset(165.18f, 172.41f), Offset(156.05f, 175.2f),
            Offset(145.97f, 183.93f)
        ),
        // Route 10: Ciutadella - Cap d'Artrutx
        listOf(
            Offset(145.97f, 183.93f), Offset(132.16f, 205.58f), Offset(127.57f, 225.0f), Offset(110.98f, 235.55f),
            Offset(108.32f, 240.55f), Offset(110.23f, 245.86f), Offset(107.81f, 247.72f), Offset(108.49f, 252.67f),
            Offset(110.37f, 259.0f), Offset(118.51f, 262.89f), Offset(117.65f, 267.41f), Offset(125.35f, 263.17f),
            Offset(135.29f, 269.91f), Offset(135.7f, 279.11f), Offset(147.88f, 278.2f), Offset(147.93f, 274.24f),
            Offset(163.03f, 266.1f)
        ),
        // Route 11: Cap d'Artrutx - Cala en Turqueta
        listOf(
            Offset(163.03f, 266.1f), Offset(169.14f, 256.88f), Offset(166.41f, 268.64f), Offset(168.9f, 281.82f),
            Offset(159.31f, 284.49f), Offset(162.27f, 291.3f), Offset(160.17f, 293.44f), Offset(161.03f, 300.6f),
            Offset(164.39f, 306.15f), Offset(166.6f, 304.79f), Offset(173.04f, 309.63f), Offset(175.72f, 306.8f),
            Offset(176.66f, 309.14f), Offset(174.58f, 313.56f), Offset(166.09f, 310.22f), Offset(168.04f, 314.89f),
            Offset(164.54f, 316.27f), Offset(163.26f, 328.21f), Offset(165.63f, 332.06f), Offset(170.04f, 330.5f),
            Offset(170.83f, 332.96f), Offset(166.62f, 337.09f), Offset(169.01f, 346.48f), Offset(163.68f, 357.79f),
            Offset(160.51f, 358.03f), Offset(156.72f, 364.92f), Offset(159.38f, 373.29f), Offset(159.69f, 386.98f),
            Offset(154.85f, 392.39f), Offset(155.37f, 403.95f)
        ),
        // Route 12: Cala en Turqueta - Cala Galdana
        listOf(
            Offset(155.37f, 403.95f), Offset(152.44f, 411.24f), Offset(156.36f, 420.71f), Offset(174.0f, 411.59f),
            Offset(177.16f, 412.62f), Offset(179.03f, 418.73f), Offset(185.06f, 412.85f), Offset(207.37f, 418.04f),
            Offset(220.12f, 416.65f), Offset(220.59f, 413.44f), Offset(231.83f, 415.58f), Offset(251.23f, 410.11f),
            Offset(251.37f, 406.35f), Offset(255.59f, 402.32f), Offset(262.45f, 404.86f), Offset(260.86f, 408.27f),
            Offset(262.59f, 411.34f), Offset(265.75f, 410.73f), Offset(268.63f, 402.18f), Offset(274.46f, 406.97f),
            Offset(281.39f, 405.97f), Offset(285.26f, 397.64f), Offset(284.1f, 390.41f), Offset(286.61f, 388.86f)
        ),
        // Route 13: Cala Galdana - Sant Tomàs
        listOf(
            Offset(286.61f, 388.86f), Offset(294.66f, 388.92f), Offset(306.32f, 383.41f), Offset(311.65f, 385.67f),
            Offset(313.5f, 382.96f), Offset(309.99f, 379.17f), Offset(314.1f, 376.05f), Offset(321.94f, 374.96f),
            Offset(327.77f, 379.76f), Offset(332.64f, 380.0f), Offset(335.79f, 375.91f), Offset(345.76f, 374.99f),
            Offset(349.69f, 369.51f), Offset(351.7f, 373.76f), Offset(356.31f, 373.39f), Offset(354.44f, 380.74f)
        ),
        // Route 14: Sant Tomàs - Son Bou
        listOf(
            Offset(354.44f, 380.74f), Offset(363.21f, 379.94f), Offset(364.82f, 382.11f), Offset(369.79f, 376.33f),
            Offset(386.35f, 373.38f), Offset(386.38f, 370.74f), Offset(389.91f, 371.63f), Offset(390.21f, 368.17f),
            Offset(399.39f, 368.23f), Offset(400.03f, 365.43f), Offset(404.35f, 367.09f), Offset(406.17f, 363.7f),
            Offset(410.77f, 363.23f), Offset(410.86f, 360.1f), Offset(416.8f, 360.61f), Offset(425.29f, 367.17f),
            Offset(422.72f, 375.86f), Offset(425.48f, 379.72f), Offset(424.7f, 382.7f), Offset(437.87f, 380.43f),
            Offset(437.75f, 384.46f), Offset(445.61f, 393.77f), Offset(445.64f, 397.78f), Offset(458.42f, 405.31f)
        ),
        // Route 15: Son Bou - Cala en Porter
        listOf(
            Offset(458.42f, 405.31f), Offset(471.44f, 409.72f), Offset(475.09f, 416.62f), Offset(480.07f, 416.16f),
            Offset(487.28f, 421.15f), Offset(492.05f, 412.74f), Offset(490.63f, 406.37f), Offset(493.15f, 405.09f),
            Offset(496.65f, 410.41f), Offset(504.63f, 409.05f), Offset(509.1f, 420.57f), Offset(525.57f, 434.21f)
        ),
        // Route 16: Cala en Porter - Binissafúller
        listOf(
            Offset(525.57f, 434.21f), Offset(530.84f, 440.01f), Offset(530.36f, 449.66f), Offset(544.27f, 445.17f),
            Offset(551.37f, 439.45f), Offset(567.9f, 435.98f), Offset(565.04f, 441.52f), Offset(576.5f, 443.32f),
            Offset(585.55f, 453.84f), Offset(588.65f, 453.66f), Offset(589.25f, 463.53f), Offset(596.03f, 464.13f),
            Offset(596.41f, 472.59f), Offset(602.32f, 477.44f)
        ),
        // Route 17: Binissafúller - Punta Prima
        listOf(
            Offset(602.32f, 477.44f), Offset(608.1f, 476.17f), Offset(608.94f, 484.69f), Offset(614.18f, 481.36f),
            Offset(621.48f, 490.3f), Offset(631.76f, 489.41f), Offset(652.13f, 500.32f), Offset(652.61f, 506.08f),
            Offset(659.33f, 510.43f), Offset(659.39f, 515.02f), Offset(665.84f, 515.11f), Offset(675.21f, 521.01f),
            Offset(695.76f, 517.44f), Offset(695.12f, 521.83f), Offset(704.06f, 523.12f), Offset(708.54f, 535.01f),
            Offset(717.18f, 536.59f), Offset(718.64f, 542.05f), Offset(722.91f, 545.94f), Offset(722.26f, 553.23f)
        ),
        // Route 18: Punta Prima - Cala de Sant Esteve
        listOf(
            Offset(722.26f, 553.23f), Offset(732.43f, 553.3f), Offset(744.22f, 564.1f), Offset(749.43f, 561.98f),
            Offset(752.43f, 567.94f), Offset(760.4f, 573.01f), Offset(772.06f, 572.68f), Offset(776.14f, 577.27f),
            Offset(785.58f, 579.79f), Offset(798.22f, 579.05f), Offset(798.08f, 582.65f), Offset(800.23f, 583.35f),
            Offset(814.82f, 583.92f), Offset(821.22f, 579.42f), Offset(819.85f, 575.85f), Offset(824.5f, 573.05f)
        ),
        // Route 19: Cala de Sant Esteve - Es Castell
        listOf(
            Offset(824.5f, 573.05f), Offset(828.41f, 572.22f), Offset(841.85f, 555.68f), Offset(840.03f, 545.65f),
            Offset(836.95f, 544.72f), Offset(838.8f, 540.28f), Offset(836.21f, 538.3f), Offset(839.93f, 519.95f),
            Offset(836.88f, 502.81f), Offset(848.19f, 489.42f), Offset(846.66f, 479.27f), Offset(843.26f, 474.41f)
        ),
        // Route 20: Es Castell - Maó
        listOf(
            Offset(843.26f, 474.41f), Offset(841.98f, 464.6f), Offset(828.83f, 448.8f), Offset(811.11f, 437.76f),
            Offset(801.45f, 438.8f), Offset(791.64f, 428.3f), Offset(785.52f, 427.01f), Offset(779.83f, 418.66f),
            Offset(781.22f, 414.16f)
        )
    )
}
