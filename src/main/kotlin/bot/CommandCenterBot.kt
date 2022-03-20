package bot

import bwapi.Game
import bwapi.Player
import bwapi.UnitType
import bwapi.Unit as Unit

class CommandCenterBot {

    fun train(commandCenter: Unit, self: Player, game: Game) {
        if (commandCenter.type == UnitType.Terran_Command_Center
            && commandCenter.canTrain()
            && self.minerals() >= 50
            && game.allUnits.filter { it.type.isWorker }.size < 28
        ) {
            commandCenter.train(UnitType.Terran_SCV)
        }
    }
}
