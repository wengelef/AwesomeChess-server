/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package chess

import chess.domain.Game
import chess.domain.GameServer
import chess.domain.GameServerImpl
import chess.model.Board
import chess.model.GameSession
import chess.model.Player
import chess.model.Team
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.*
import io.ktor.util.generateNonce
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.mapNotNull
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import org.slf4j.event.Level
import java.time.Duration
import java.util.*

object GameModule {
    fun get() = Kodein.Module("GameModule") {
        bind<ArrayList<Player>>() with provider { arrayListOf(Player(Team.Black), Player(Team.White)) }
        bind<Board>() with provider { Board() }
        bind<Game>() with singleton { Game(instance(), instance()) }
    }
}

object AppModule {
    fun get() = Kodein.Module("AppModule") {
        bind<Gson>() with singleton { Gson() }
        bind<GameServer>() with singleton { GameServerImpl(instance()) }
    }
}

val kodein = Kodein {
    import(GameModule.get())
    import(AppModule.get())
}

val game: Game by kodein.instance()
val gson: Gson by kodein.instance()
val server: GameServer by kodein.instance()

data class Command(val command: String)

fun main(args: Array<String>) {

    val port = System.getenv("PORT")?.let { value -> Integer.valueOf(value) } ?: 8080

    embeddedServer(Netty, port) {
        install(DefaultHeaders)

        install(CallLogging) {
            level = Level.INFO
        }

        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
            }
        }

        install(WebSockets) {
            pingPeriod = Duration.ofMinutes(1)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE

            masking = false
        }

        install(Sessions) {
            cookie<GameSession>("Session")
        }

        intercept(ApplicationCallPipeline.Features) {
            if (call.sessions.get<GameSession>() == null) {
                call.sessions.set(GameSession(generateNonce()))
            }
        }

        routing {
            webSocket("/") {
                val session = call.sessions.get<GameSession>()

                if (session == null) {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No Session"))
                    return@webSocket
                }

                server.memberJoin(session.id, this)

                try {

                    incoming.mapNotNull { it as? Frame.Text }
                            .consumeEach { frame ->
                                val command = try {
                                    gson.fromJson(frame.readText(), Command::class.java)
                                } catch (exception: JsonSyntaxException) {
                                    Command(frame.readText())
                                }.command

                                when (command) {
                                    "start" -> {
                                        game.start()
                                        server.sendBoard(game.board)
                                    }
                                    "turn" -> {
                                        game.nextTurn()
                                        server.sendBoard(game.board)
                                        if (game.isOver) {
                                            server.broadcast("${game.winner.get()} won the Game")
                                        }
                                    }
                                    "board" -> server.sendBoard(game.board)
                                    "test" -> {
                                        while (!game.isOver) {
                                            game.nextTurn()
                                            server.sendBoard(game.board)
                                        }
                                        server.broadcast("${game.winner.get()} won the Game")
                                    }
                                    else -> server.sendTo(session.id, "server", "Huh?")
                                }
                            }
                } catch (exception: Exception) {
                    println("Caught Exception : ${exception.printStackTrace()}")
                } finally {
                    server.memberLeft(session.id, this)
                    close(CloseReason(CloseReason.Codes.NORMAL, "Good Bye."))
                }
            }
        }
    }.start(true)
}
