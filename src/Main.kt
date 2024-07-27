import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Image
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer

class GamePanel : JPanel(), ActionListener {
    private val playerImage: Image
    private val ballImage: Image
    private val playerSizeFactor = 1 / 4.0 // Reduced to 1/4 to make it half the current size
    private val ballSizeFactor = 1 / 5.0
    private var playerX = 200
    private var playerY = 600
    private var ballX = 200
    private var ballY = 0
    private var ballSpeed = 10 // Consistent speed for the ball
    private var score = 0
    private var strikes = 0
    private var foulCount = 0 // ファールカウント
    private var isGameOver = false
    private var message = "" // Variable to hold the message to be displayed
    private var messageAlpha = 0 // Transparency of the message
    private var messageDisplayTime = 0 // Counter for message display time

    private var gameState = "START" // Game state: START, PLAYING, GAME_OVER

    private val timer: Timer

    init {
        playerImage = loadImage("asset/player.png")
        ballImage = loadImage("asset/ball.png")

        preferredSize = Dimension(400, 800)
        background = Color.BLACK

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (gameState == "START") {
                    gameState = "PLAYING"
                } else if (isGameOver) {
                    resetGame()
                } else {
                    swingBat()
                }
                repaint()
            }
        })

        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                if (isGameOver || gameState == "START") return
                playerX = e.x - (playerImage.getWidth(this@GamePanel) * playerSizeFactor).toInt() / 2
                repaint()
            }
        })

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_F5) {
                    if (isGameOver) {
                        resetGame()
                        repaint()
                    }
                }
            }
        })

        timer = Timer(16, this)
        timer.start()
        isFocusable = true
        requestFocusInWindow()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        when (gameState) {
            "START" -> drawStartScreen(g)
            "PLAYING" -> drawGameScreen(g)
            "GAME_OVER" -> drawGameOverScreen(g)
        }
    }

    private fun drawStartScreen(g: Graphics) {
        g.color = Color.WHITE
        g.drawString("野獣先輩のホームランダービー", width / 2 - 80, height / 2 - 20)
        g.drawString("クリックしてスタート", width / 2 - 60, height / 2)
    }

    private fun drawGameScreen(g: Graphics) {
        // Draw the player with the resized image
        val playerWidth = (playerImage.getWidth(this) * playerSizeFactor).toInt()
        val playerHeight = (playerImage.getHeight(this) * playerSizeFactor).toInt()
        g.drawImage(playerImage, playerX, playerY, playerWidth, playerHeight, this)

        // Draw the ball with the resized image
        val ballWidth = (ballImage.getWidth(this) * ballSizeFactor).toInt()
        val ballHeight = (ballImage.getHeight(this) * ballSizeFactor).toInt()
        g.drawImage(ballImage, ballX, ballY, ballWidth, ballHeight, this)

        // Draw the hit zone as a light blue circle
        g.color = Color.CYAN
        g.drawOval(playerX, playerY, playerWidth, playerHeight)

        // Draw text information
        g.color = Color.WHITE
        g.drawString("Score: $score", 10, 20)
        g.drawString("Strikes: $strikes", 10, 40)
        g.drawString("Fouls: $foulCount", 10, 60)

        // Draw message at the bottom of the screen
        if (messageAlpha > 0) {
            val originalColor = g.color
            g.color = Color(255, 255, 0, messageAlpha)
            g.drawString(message, width / 2 - 50, height - height / 9)
            g.color = originalColor
        }
    }

    private fun drawGameOverScreen(g: Graphics) {
        drawGameScreen(g)
        g.color = Color.RED
        g.drawString("Game Over", width / 2 - 50, height / 2)
        g.drawString("Press F5 to Restart", width / 2 - 80, height / 2 + 20)
    }

    override fun actionPerformed(e: ActionEvent) {
        if (gameState != "PLAYING") return

        ballY += ballSpeed
        if (ballY > height) {
            if (!isBallInHittingZone()) {
                foulCount++
                if (foulCount >= 100) {
                    gameOver()
                }
            }
            resetBall()
        }
        if (messageDisplayTime > 0) {
            messageDisplayTime--
            messageAlpha = (255 * messageDisplayTime / 30).coerceAtLeast(0)
        }
        repaint()
    }

    private fun swingBat() {
        if (isBallInHittingZone()) {
            // 20%の確率でファールにする
            if (Math.random() < 0.2) {
                println("ファール！")
                message = "ファール！"
                foulCount++
                if (foulCount >= 100) {
                    gameOver()
                }
            } else {
                println("ホームラン！")
                message = "ホームラン！"
                score += 1
                resetBall()
            }
        } else {
            println("ファール！")
            message = "ファール！"
            foulCount++
            if (foulCount >= 100) {
                gameOver()
            }
        }
        messageDisplayTime = 30 // Display the message for 0.5 seconds (30 frames at 60 FPS)
        messageAlpha = 255 // Fully opaque initially
    }

    private fun isBallInHittingZone(): Boolean {
        val playerWidth = (playerImage.getWidth(this) * playerSizeFactor).toInt()
        val playerHeight = (playerImage.getHeight(this) * playerSizeFactor).toInt()
        val playerCenterX = playerX + playerWidth / 2
        val playerCenterY = playerY + playerHeight / 2
        val ballWidth = (ballImage.getWidth(this) * ballSizeFactor).toInt()
        val ballHeight = (ballImage.getHeight(this) * ballSizeFactor).toInt()
        val ballCenterX = ballX + ballWidth / 2
        val ballCenterY = ballY + ballHeight / 2

        val distance = Math.sqrt(Math.pow((playerCenterX - ballCenterX).toDouble(), 2.0) +
                Math.pow((playerCenterY - ballCenterY).toDouble(), 2.0))

        return distance <= playerWidth / 2
    }

    private fun resetBall() {
        ballX = (Math.random() * (width - (ballImage.getWidth(this) * ballSizeFactor).toInt())).toInt()
        ballY = 0
    }

    private fun gameOver() {
        isGameOver = true
        gameState = "GAME_OVER"
        message = "ゲームオーバー"
        messageDisplayTime = 30
        messageAlpha = 255
    }

    private fun resetGame() {
        playerX = 200
        playerY = 600
        ballX = 200
        ballY = 0
        ballSpeed = 10 // Consistent speed for the ball
        score = 0
        strikes = 0
        foulCount = 0 // ファールカウントをリセット
        isGameOver = false
        gameState = "START"
        message = ""
        messageAlpha = 0
        requestFocusInWindow() // Re-focus to ensure key events are captured
    }

    private fun loadImage(path: String): Image {
        val url = javaClass.getResource(path)
            ?: throw IllegalArgumentException("画像ファイルが見つかりません: $path")
        return ImageIcon(url).image
    }
}

fun main() {
    val frame = JFrame("野獣先輩のホームランダービー")
    val gamePanel = GamePanel()
    frame.add(gamePanel)
    frame.pack()
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
}
