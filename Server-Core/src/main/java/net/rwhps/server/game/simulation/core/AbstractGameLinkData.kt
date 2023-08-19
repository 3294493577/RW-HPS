/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.simulation.core

import net.rwhps.server.util.annotations.GameSimulationLayer

/**
 * Link The game comes with settings to avoid some of the distractions caused by confusion
 *
 * @author RW-HPS/Dr
 */
interface AbstractGameLinkData {
    @GameSimulationLayer.GameSimulationLayer_KeyWords("overrideTeamLayout: unhandled layout:")
    val teamOperationsSyncObject: Any


    var maxUnit: Int

    var sharedcontrol: Boolean

    var fog: Int

    var nukes: Boolean

    var credits: Int

    var aiDifficuld: Int

    var income: Float

    var startingunits: Int
}