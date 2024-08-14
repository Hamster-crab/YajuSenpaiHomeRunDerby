#include <GL/glew.h>
#include <GLFW/glfw3.h>
#include <SOIL/SOIL.h>
#include <iostream>
#include <string>
#include <cmath>

const int WIDTH = 400;
const int HEIGHT = 800;
const char* PLAYER_IMG_PATH = "resources/player.png";
const char* BALL_IMG_PATH = "resources/ball.png";

// グローバル変数
int fuckCount = 0;
GLuint playerTexture, ballTexture;
int playerX = 200, playerY = 600;
int ballX = 200, ballY = 0;
int ballSpeed = 10;
int score = 0;
int strikes = 0;
int foulCount = 0;
bool isGameOver = false;
std::string message = "";
int messageAlpha = 0;
int messageDisplayTime = 0;
std::string gameState = "START";
GLFWwindow* window; // ウィンドウのグローバル宣言

// 関数プロトタイプ宣言
void loadTexture(GLuint &texture, const char* path);
void drawTexture(GLuint texture, int x, int y, int width, int height);
void display();
void update();
void swingBat();
bool isBallInHittingZone();
void resetBall();
void gameOver();
void resetGame();
void mouseCallback(GLFWwindow* window, int button, int action, int mods);
void mouseMoveCallback(GLFWwindow* window, double xpos, double ypos);
void keyCallback(GLFWwindow* window, int key, int scancode, int action, int mods);

void loadTexture(GLuint &texture, const char* path)
{
    glGenTextures(1, &texture);
    glBindTexture(GL_TEXTURE_2D, texture);
    int width, height;
    unsigned char* image = SOIL_load_image(path, &width, &height, 0, SOIL_LOAD_RGBA);
    if (!image)
    {
        std::cerr << "Failed to load texture: " << path << std::endl;
    }
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
    glGenerateMipmap(GL_TEXTURE_2D);
    SOIL_free_image_data(image);
    glBindTexture(GL_TEXTURE_2D, 0);
}

void drawTexture(GLuint texture, int x, int y, int width, int height)
{
    glBindTexture(GL_TEXTURE_2D, texture);
    glEnable(GL_TEXTURE_2D);
    glBegin(GL_QUADS);
    glTexCoord2f(0.0f, 1.0f); glVertex2f(x, y);
    glTexCoord2f(1.0f, 1.0f); glVertex2f(x + width, y);
    glTexCoord2f(1.0f, 0.0f); glVertex2f(x + width, y + height);
    glTexCoord2f(0.0f, 0.0f); glVertex2f(x, y + height);
    glEnd();
    glDisable(GL_TEXTURE_2D);
    glBindTexture(GL_TEXTURE_2D, 0);
}



void display()
{
    glClear(GL_COLOR_BUFFER_BIT);
    if (gameState == "START")
    {}
    else if (gameState == "PLAYING")
    {
        drawTexture(playerTexture, playerX, playerY, 100, 100); // Resize as needed
        drawTexture(ballTexture, ballX, ballY, 80, 80); // Resize as needed
    }
    else if (gameState == "GAME_OVER")
    {
        drawTexture(playerTexture, playerX, playerY, 100, 100); // Resize as needed
        drawTexture(ballTexture, ballX, ballY, 80, 80); // Resize as needed
    }
    glfwSwapBuffers(window);
}

void update()
{
    if (gameState != "PLAYING") return;

    ballY += ballSpeed;
    if (ballY > HEIGHT)
    {
        if (!isBallInHittingZone())
        {
            foulCount++;
            if (foulCount >= 15)
            {
                gameOver();
            }
        }
        resetBall();
    }
    if (messageDisplayTime > 0)
    {
        messageDisplayTime--;
        messageAlpha = std::max(0, 255 * messageDisplayTime / 30);
    }
}

void swingBat()
{
    if (isBallInHittingZone())
    {
        if (rand() % 5 == 0)
        {
            std::cout << "ファール！！！！！！" << std::endl;
            foulCount++;
            if (foulCount >= 15)
            {
                gameOver();
            }
        }
        else
        {
            std::cout << "ホームラン よかったね" << std::endl;
            score++;
            resetBall();
        }
    }
    else
    {
        std::cout << "Fuckyou!" << std::endl;
        fuckCount++;
        if (fuckCount >= 5)
        {
            gameOver();
        }
    }
    messageDisplayTime = 30;
    messageAlpha = 255;
}

bool isBallInHittingZone()
{
    int playerWidth = 100; // Update according to texture size
    int playerHeight = 100;
    int playerCenterX = playerX + playerWidth / 2;
    int playerCenterY = playerY + playerHeight / 2;
    int ballWidth = 80; // Update according to texture size
    int ballHeight = 80;
    int ballCenterX = ballX + ballWidth / 2;
    int ballCenterY = ballY + ballHeight / 2;

    double distance = sqrt(pow(playerCenterX - ballCenterX, 2.0) + pow(playerCenterY - ballCenterY, 2.0));
    return distance <= playerWidth / 2;
}

void resetBall()
{
    ballX = rand() % (WIDTH - 80); // Update according to texture size
    ballY = 0;
}

void gameOver()
{
    std::cout << "ファール : " << foulCount << std::endl;
    std::cout << "ホームラン : " << score << std::endl;
    std::cout << "アウト" << fuckCount << std::endl;
    resetGame();
    isGameOver = true;
    gameState = "GAME_OVER";
    message = "ゲームオーバー";
    messageDisplayTime = 30;
    messageAlpha = 255;
}

void resetGame()
{
    playerX = 200;
    playerY = 600;
    ballX = 200;
    ballY = 0;
    ballSpeed = 10;
    score = 0;
    strikes = 0;
    foulCount = 0;
    isGameOver = false;
    message = "";
    messageAlpha = 0;
    messageDisplayTime = 0;
    gameState = "START";
}

void mouseCallback(GLFWwindow* window, int button, int action, int mods)
{
    if (action == GLFW_PRESS && button == GLFW_MOUSE_BUTTON_LEFT)
    {
        if (gameState == "START")
        {
            gameState = "PLAYING";
        }
        else if (gameState == "GAME_OVER")
        {
            resetGame();
        }
        else if (gameState == "PLAYING")
        {
            swingBat();
        }
    }
}

void mouseMoveCallback(GLFWwindow* window, double xpos, double ypos)
{
    playerX = static_cast<int>(xpos) - 50; // Update according to texture size
    playerY = static_cast<int>(ypos) - 50;
}

void keyCallback(GLFWwindow* window, int key, int scancode, int action, int mods)
{
    if (action == GLFW_PRESS && key == GLFW_KEY_F5)
    {
        resetGame();
    }
}

int main()
{
    if (!glfwInit())
    {
        std::cerr << "Failed to initialize GLFW" << std::endl;
        return -1;
    }

    window = glfwCreateWindow(WIDTH, HEIGHT, "野獣先輩のホームランダービー", NULL, NULL);
    if (!window)
    {
        std::cerr << "Failed to create GLFW window" << std::endl;
        glfwTerminate();
        return -1;
    }

    glfwMakeContextCurrent(window);

    if (glewInit() != GLEW_OK)
    {
        std::cerr << "Failed to initialize GLEW" << std::endl;
        return -1;
    }

    glViewport(0, 0, WIDTH, HEIGHT);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrtho(0, WIDTH, HEIGHT, 0, -1, 1);
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    loadTexture(playerTexture, PLAYER_IMG_PATH);
    loadTexture(ballTexture, BALL_IMG_PATH);

    glfwSetMouseButtonCallback(window, mouseCallback);
    glfwSetCursorPosCallback(window, mouseMoveCallback);
    glfwSetKeyCallback(window, keyCallback);

    while (!glfwWindowShouldClose(window))
    {
        update();
        display();
        glfwPollEvents();
    }

    glfwDestroyWindow(window);
    glfwTerminate();
    return 0;
}
