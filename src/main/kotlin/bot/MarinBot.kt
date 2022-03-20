package bot

import bwapi.Game
import bwapi.Position
import bwapi.UnitType

class MarinBot {

    fun attack(
        marine: bwapi.Unit,
        game: Game,
        enemyLocation: Position?,
        startingLocations: List<Position>
    ) {
        if (marine.type == UnitType.Terran_Marine
            && game.allUnits.filter { it.type == UnitType.Terran_Marine }.size > 4
            && marine.isIdle
        ) {
            if (enemyLocation != null) {
                marine.attack(enemyLocation)
            } else {
                startingLocations.forEach {
                    marine.attack(it, true)
                }
            }
        }
    }
}
