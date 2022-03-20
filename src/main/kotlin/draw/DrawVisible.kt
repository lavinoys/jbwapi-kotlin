package draw

import bwapi.Game
import bwapi.UnitType
import bwapi.Unit as Unit

class DrawVisible {

    fun worker(scv: Unit, game: Game) {
        if (scv.type.isWorker) {
            game.drawTextMap(
                scv.position.getX(),
                scv.position.getY(),
                "TilePos: ${scv.tilePosition} Pos: ${scv.position} isCarryingMinerals: ${scv.isCarryingMinerals}"
            )

            /*if (myUnit.type.isWorker) {
                game.drawLineMap(
                    myUnit.position.getX(),
                    myUnit.position.getY(),
                    myUnit.orderTargetPosition.getX(),
                    myUnit.orderTargetPosition.getY(),
                    Color.Black
                )
            }*/
        }
    }

    fun supplyDepot(supplyDepot: Unit, game: Game) {
        if (supplyDepot.type == UnitType.Terran_Supply_Depot) {
            game.drawTextMap(
                supplyDepot.position.getX(),
                supplyDepot.position.getY(),
                "TilePos: ${supplyDepot.tilePosition} Pos: ${supplyDepot.position} isCompleted: ${supplyDepot.isCompleted}"
            )
        }
    }
}
