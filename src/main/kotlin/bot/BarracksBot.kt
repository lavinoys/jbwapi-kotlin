package bot

import bwapi.Unit as Unit
import bwapi.Player
import bwapi.UnitType

class BarracksBot {
    fun trainMarine (
        barracks: Unit,
        self: Player
    ) {
        if ( barracks.type == UnitType.Terran_Barracks
            && barracks.trainingQueue.size <= 0
            && barracks.canTrain(UnitType.Terran_Marine)
            && self.minerals() >= 50
            && self.supplyTotal()-self.supplyUsed() >= 6
        ) {
            barracks.train(UnitType.Terran_Marine)
        }
    }
}
