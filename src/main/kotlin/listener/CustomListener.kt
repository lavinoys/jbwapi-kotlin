package listener

import bot.BarracksBot
import bot.CommandCenterBot
import bot.MarinBot
import bot.ScvBot
import bwapi.*
import bwapi.Unit
import bwem.BWEM
import draw.DrawVisible

class CustomListener(
    private val drawVisible: DrawVisible,
    private val commandCenter: CommandCenterBot,
    private val scvBot: ScvBot,
    private val barracks: BarracksBot,
    private val marinBot: MarinBot
): DefaultBWListener() {
    private val bwClient = BWClient(this)
    private lateinit var game: Game
    private var buildingScv: Unit? = null
    private var buildingBarracksScv: Unit? = null
    private lateinit var bwem: BWEM
    private lateinit var chokes: List<Position>
    private lateinit var startingLocations: List<Position>
    private var enemyLocation: Position? = null

    fun start() {
        bwClient.startGame()
    }

    override fun onStart() {
        game = bwClient.game
        val self = game.self()
        game.setLocalSpeed(35)//게임 속도 30이 기본, 토너먼트에선 20
//        game.setLatCom(true)
        bwem = BWEM(game)
        bwem.initialize()
        bwem.map.run {
            this.data
            this.assignStartingLocationsToSuitableBases()
        }
        chokes = bwem.map.chokePoints.map { it.center.toPosition() }
        startingLocations = bwem.map.startingLocations
            .filter { it.getDistance(self.startLocation) > 10 }
            .map { it.toPosition() }
        if (startingLocations.size == 1) {
            enemyLocation = startingLocations.first()
        }
    }

    override fun onFrame() {
        val self: Player = game.self()
        val enemy: Player = game.enemy()
        game.drawTextScreen(10, 10, "Playing as ${self.name} - ${self.race}")
        game.drawTextScreen(10, 20, "supplyTotal : ${self.supplyTotal()}, supplyUsed : ${self.supplyUsed()}")
//        println("x : ${enemy.startLocation.x} , y : ${enemy.startLocation.y} / ${game.mapWidth()} , ${game.mapHeight()}")

        if (enemyLocation == null
            && enemy.startLocation.x - enemy.startLocation.y != -2
        ) {
            enemyLocation = enemy.startLocation.toPosition()
        }

        chokes.forEach { game.drawCircleMap(it, 10, Color.Red) }
        startingLocations.forEach { game.drawCircleMap(it, 50, Color.Cyan) }

        game.allUnits
            .filter { it.isCompleted }
            .filter { it.lastCommandFrame + game.latencyFrames < game.frameCount }
            .parallelStream()
            .forEach { myUnit ->
//                drawVisible.worker(myUnit, game)
//                drawVisible.supplyDepot(myUnit, game)
                commandCenter.train(myUnit, self, game)
                scvBot.gatherMineral(myUnit, game)
                buildingScv?.let {
                    scvBot.build(it, UnitType.Terran_Supply_Depot, game)
                }?: run { buildingScv = scvBot.selectBuildSupplyDepotScv(myUnit, self) }

                buildingBarracksScv?.let {
                    scvBot.build(it, UnitType.Terran_Barracks, game)
                }?: run { buildingBarracksScv = scvBot.selectBuildBarracksScv(myUnit, self) }

                barracks.train(myUnit, UnitType.Terran_Marine, self, game, bwem)
                marinBot.attack(myUnit, game, enemyLocation, startingLocations)
        }
    }


    override fun onUnitComplete(unit: Unit?) {
        unit?.let { myUnit ->
            if (myUnit.type == UnitType.Terran_Supply_Depot && myUnit.isCompleted) {
                buildingScv = null
            }
            if (myUnit.type == UnitType.Terran_Barracks && myUnit.isCompleted) {
                buildingBarracksScv = null
                myUnit.setRallyPoint(chokes.sortedWith(compareBy { it.getApproxDistance(myUnit.position) })[1])
            }
        }

    }
}
