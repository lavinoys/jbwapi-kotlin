package bot

import bwapi.Game
import bwapi.Player
import bwapi.Position
import bwapi.Unit as Unit
import bwapi.UnitType

class ScvBot {
    fun gatherMineral(scv: Unit, game: Game) {
        if (scv.type.isWorker && scv.isIdle) {
            val minerals = game.neutralUnits
                .filter { it.type.isMineralField }
                .sortedBy { scv.getDistance(game.getClosestUnit(it.position)) }
            scv.gather(minerals.first(), false)
        }
    }

    fun selectBuildSupplyDepotScv(
        scv: Unit,
        self: Player
    ): Unit? = if (scv.type.isWorker
        && scv.canBuild(UnitType.Terran_Supply_Depot)
        && !scv.isCarryingMinerals
        && self.supplyTotal()-self.supplyUsed() <= 8
    ) {
        scv
    } else {
        null
    }

    fun selectBuildBarracksScv(
        scv: Unit,
        self: Player
    ): Unit? = if (scv.type.isWorker
        && !scv.isCarryingMinerals
        && scv.canBuild(UnitType.Terran_Barracks)
        && self.minerals() >= 150
    ) {
        scv
    } else {
        null
    }

    fun build(
        scv: Unit,
        target: UnitType,
        game: Game
    ): Boolean = scv.build(
        target,
        game.getBuildLocation(target, scv.tilePosition, 40, false)
    )

    fun doScout(
        scv: Unit,
        startingLocations: List<Position>
    ) {
        startingLocations.forEachIndexed { index, position ->
            if (index == 0) {
                scv.move(position)
            } else {
                scv.move(position, true)
            }
        }
    }

}
