package bot

import bwapi.Game
import bwapi.Player
import bwapi.TilePosition
import bwapi.Unit as Unit
import bwapi.UnitType
import tile.CustomAnalyseTile

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
        && self.supplyTotal()-self.supplyUsed() < 6) {
        scv
    } else {
        null
    }

    fun buildSupplyDepotCustom(
        scv: Unit,
        self: Player,
        customAnalyseTile: CustomAnalyseTile,
        game: Game
    ): Boolean = customAnalyseTile.getBuildTile(
        scv,
        UnitType.Terran_Supply_Depot,
        self.startLocation
    )?.let {
        return scv.build(UnitType.Terran_Supply_Depot, it)
    }?: false

    fun buildSupplyDepot(
        scv: Unit,
        game: Game
    ): Boolean = scv.build(
        UnitType.Terran_Supply_Depot,
        game.getBuildLocation(UnitType.Terran_Supply_Depot, scv.tilePosition, 40, false)
    )

}
