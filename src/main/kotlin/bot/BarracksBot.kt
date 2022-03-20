package bot

import bwapi.Game
import bwapi.Unit as Unit
import bwapi.Player
import bwapi.UnitType

class BarracksBot {
    fun train (
        barracks: Unit,
        target: UnitType,
        self: Player,
        game: Game
    ) {
        if ( barracks.type == UnitType.Terran_Barracks
            && barracks.trainingQueue.size <= 0
            && barracks.canTrain(target)
            && self.minerals() >= 50
            && game.allUnits.filter { it.type == UnitType.Terran_Barracks }.size < 4
        ) {
            barracks.train(target)
        }
    }
}
