package bot

import bwapi.Game
import bwapi.Player
import bwapi.Position
import bwapi.UnitType

class MarinBot {

    fun attack(
        marine: bwapi.Unit,
        self: Player,
        enemyLocation: Position?
    ) {
        if (marine.type == UnitType.Terran_Marine
            && self.units.filter { it.type == UnitType.Terran_Marine }.size > 10
            && marine.isIdle
        ) {
            if (enemyLocation != null) {
                marine.attack(enemyLocation)
            }
        }
    }
}
