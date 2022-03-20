package bot

import bwapi.Player
import bwapi.UnitType
import bwapi.Unit as Unit

class CommandCenterBot {

    fun makeScv(commandCenter: Unit, self: Player) {
        if (commandCenter.type == UnitType.Terran_Command_Center
            && commandCenter.canTrain()
            && commandCenter.trainingQueue.size <= 0
            && self.minerals() >= 50
        ) {
            commandCenter.train(UnitType.Terran_SCV)
        }
    }
}
