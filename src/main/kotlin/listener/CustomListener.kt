package listener

import bot.BarracksBot
import bot.CommandCenterBot
import bot.MarinBot
import bot.ScvBot
import bwapi.*
import bwapi.Unit
import bwem.BWEM
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class CustomListener(
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
    private var firstScout: Boolean = true
    private var scoutScv: Unit? = null

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
    }

    override fun onFrame() {
        runBlocking {
            val self: Player = game.self()
            val enemy: Player = game.enemy()
            game.drawTextScreen(10, 10, "Playing as ${self.name} - ${self.race}")
            game.drawTextScreen(10, 20, "supplyTotal : ${self.supplyTotal()}, supplyUsed : ${self.supplyUsed()}")
//        println("x : ${enemy.startLocation.x} , y : ${enemy.startLocation.y} / ${game.mapWidth()} , ${game.mapHeight()}")

            chokes.forEach { game.drawCircleMap(it, 10, Color.Red) }
            startingLocations.forEach { game.drawCircleMap(it, 50, Color.Cyan) }
            enemyLocation?.let { game.drawCircleMap(it, 40, Color.Red) }

            /*
            * 정찰한 곳에 적진이 있으면 저장
            * */
            if (enemyLocation == null) {
                if (enemy.units.size > 1) {
                    enemyLocation = startingLocations.minByOrNull { it.getApproxDistance(enemy.units.first().position) }
                    scoutScv?.let { scvBot.gatherMineral(it, game) }
                }
            }

            /*
            * 일꾼 한 기 정찰 ㄱㄱ
            * */
            if (firstScout) {
                val findScoutScvDeffer = async {
                    scoutScv = self.units.first { it.type.isWorker && it.isIdle }
                }
                val doScoutScvDeffer = async {
                    scoutScv?.let {
                        scvBot.doScout(it, startingLocations)
                    }
                }
                findScoutScvDeffer.await()
                doScoutScvDeffer.await()
                firstScout = false
            }

            self.units
                .filter { it.isCompleted }
                .filter { it.lastCommandFrame + game.latencyFrames < game.frameCount }
                .forEach { myUnit ->
                    runBlocking {
//                drawVisible.worker(myUnit, game)
//                drawVisible.supplyDepot(myUnit, game)
                        commandCenter.train(myUnit, self, game)
                        scvBot.gatherMineral(myUnit, game)
                        buildingScv?.let {
                            scvBot.build(it, UnitType.Terran_Supply_Depot, game)
                        }?: run {
                            if (myUnit.id != scoutScv?.id) {
                                buildingScv = scvBot.selectBuildSupplyDepotScv(myUnit, self)
                            }
                        }

                        buildingBarracksScv?.let {
                            scvBot.build(it, UnitType.Terran_Barracks, game)
                        }?: run {
                            if (myUnit.id != scoutScv?.id) {
                                buildingBarracksScv = scvBot.selectBuildBarracksScv(myUnit, self)
                            }
                        }

                        barracks.trainMarine(myUnit, self)
                        marinBot.attack(myUnit, self, enemyLocation)
                    }
                }
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
