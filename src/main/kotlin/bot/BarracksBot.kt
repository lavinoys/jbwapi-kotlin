package bot

import bwapi.Game
import bwapi.Unit as Unit
import bwapi.Player
import bwapi.UnitType
import bwem.BWEM

class BarracksBot {
    fun train (
        barracks: Unit,
        target: UnitType,
        self: Player,
        game: Game,
        bwem: BWEM
    ) {
        if ( barracks.type == UnitType.Terran_Barracks
            && barracks.trainingQueue.size <= 0
            && barracks.canTrain(target)
            && self.minerals() >= 50
            && game.allUnits.filter { it.type == target }.size < 40
        ) {
            barracks.train(target)
        }
    }
}
