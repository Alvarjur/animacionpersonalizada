package com.alvaroarmas.animacionpersonalizada;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSocket;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.czyzby.websocket.WebSockets;
import com.github.czyzby.websocket.WebSocketListener;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    // Filas y columnas del sprite sheet
    private static final int FRAME_COLS = 3, FRAME_ROWS = 1;


    Texture background;
    Texture activeSheet;
    Animation<TextureRegion> walkRight;
    Animation<TextureRegion> walkLeft;
    Animation<TextureRegion> walkUp;
    Animation<TextureRegion> walkDown;
    Animation<TextureRegion> currentAnimation;
    SpriteBatch spriteBatch;

    Boolean isMoving;

    float stateTime;
    float speed = 200;
    float posX = 150;
    float posY = 50;
    Rectangle up, down, left, right, fire;
    final int IDLE=0, UP=1, DOWN=2, LEFT=3, RIGHT=4;

    float lastSend = 0f;
    OrthographicCamera camera;
    WebSocket socket;
    String address = "localhost";
    int port = 8888;
    float networkTimer = 0f;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        stateTime = 0f;

        background = new Texture(Gdx.files.internal("the_surface.png"));

        walkRight = helperCrearAnimacion("cloud_walk_right.png", FRAME_COLS, FRAME_ROWS);
        walkLeft = helperCrearAnimacion("cloud_walk_left.png", FRAME_COLS, FRAME_ROWS);
        walkUp = helperCrearAnimacion("cloud_walk_up3.png", FRAME_COLS, FRAME_ROWS);
        walkDown = helperCrearAnimacion("cloud_walking_down_3.png", FRAME_COLS, FRAME_ROWS);

        // Empezamos mirando hacia abajo
        currentAnimation = walkDown;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // facilities per calcular el "touch"
        up = new Rectangle(0, (float) (Gdx.graphics.getHeight() * 2) /3, Gdx.graphics.getWidth(), (float) Gdx.graphics.getHeight() /3);
        down = new Rectangle(0, 0, Gdx.graphics.getWidth(), (float) Gdx.graphics.getHeight() /3);
        left = new Rectangle(0, 0, (float) Gdx.graphics.getWidth() /3, Gdx.graphics.getHeight());
        right = new Rectangle((float) (Gdx.graphics.getWidth() * 2) /3, 0, (float) Gdx.graphics.getWidth() /3, Gdx.graphics.getHeight());

        socket = WebSockets.newSocket(WebSockets.toWebSocketUrl(address, port));
        // ULL: si és a traves de HTTPS , el protocol seria wss enlloc de ws
        //socket = WebSockets.newSocket(WebSockets.toSecureWebSocketUrl(address, port));
        socket.setSendGracefully(false);
        socket.addListener((WebSocketListener) new MyWSListener());
        socket.connect();
        socket.send("Enviar dades");
    }


    // Función auxiliar para no repetir código
    private Animation<TextureRegion> helperCrearAnimacion(String path, int cols, int rows) {
        Texture sheet = new Texture(Gdx.files.internal(path));
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / cols, sheet.getHeight() / rows);
        TextureRegion[] frames = new TextureRegion[cols * rows];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames[index++] = tmp[i][j];
            }
        }
        return new Animation<>(0.125f, frames);
    }

    protected int virtual_joystick_control() {
        for (int i = 0; i < 10; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector3 touchPos = new Vector3();
                touchPos.set(Gdx.input.getX(i), Gdx.input.getY(i), 0);

                // Usamos la cámara local para convertir las coordenadas
                camera.unproject(touchPos);

                if (up.contains(touchPos.x, touchPos.y)) return UP;
                if (down.contains(touchPos.x, touchPos.y)) return DOWN;
                if (left.contains(touchPos.x, touchPos.y)) return LEFT;
                if (right.contains(touchPos.x, touchPos.y)) return RIGHT;
            }
        }
        return IDLE;
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float delta = Gdx.graphics.getDeltaTime();
        isMoving = false;

        // 1. Obtener input del "joystick" táctil
        int direccionTouch = virtual_joystick_control();

        // 2. Lógica combinada (Teclado + Táctil)
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W) || direccionTouch == UP) {
            posY += speed * delta;
            currentAnimation = walkUp;
            isMoving = true;
        } else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S) || direccionTouch == DOWN) {
            posY -= speed * delta;
            currentAnimation = walkDown;
            isMoving = true;
        }

        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A) || direccionTouch == LEFT) {
            posX -= speed * delta;
            currentAnimation = walkLeft;
            isMoving = true;
        } else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D) || direccionTouch == RIGHT) {
            posX += speed * delta;
            currentAnimation = walkRight;
            isMoving = true;
        }

        // Actualización de tiempo de animación
        if (isMoving) {
            stateTime += delta;
        } else {
            stateTime = 0.125f; // Frame de reposo
        }

        // Dibujado
        TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTime, true);
        spriteBatch.setProjectionMatrix(camera.combined); // Asegura que use la cámara
        spriteBatch.begin();
        spriteBatch.draw(background, 0, 0, 800, 480); // Ajustado a la cámara
        spriteBatch.draw(currentFrame, posX, posY, 32, 32);
        spriteBatch.end();

        networkTimer += delta;
        if( networkTimer-lastSend > 1.0f ) {
            lastSend = networkTimer;
            if (socket.isOpen()) {
                socket.send("Posición actual: " + posX + "," + posY);
            }
        }
    }

    @Override
    public void dispose() { // Los SpriteBatches y Textures deben ser desechados siempre
        spriteBatch.dispose();
        background.dispose();


    }
}
// COMUNICACIONS (rebuda de missatges)
/////////////////////////////////////////////
class MyWSListener implements WebSocketListener {

    @Override
    public boolean onOpen(WebSocket webSocket) {
        System.out.println("Opening...");
        return false;
    }

    @Override
    public boolean onClose(WebSocket webSocket, int closeCode, String reason) {
        System.out.println("Closing...");
        return false;
    }

    @Override
    public boolean onMessage(WebSocket webSocket, String packet) {
        System.out.println("Message:"+packet);
        return false;
    }

    @Override
    public boolean onMessage(WebSocket webSocket, byte[] packet) {
        System.out.println("Message:"+packet);
        return false;
    }

    @Override
    public boolean onError(WebSocket webSocket, Throwable error) {
        System.out.println("ERROR:"+error.toString());
        return false;
    }
}
