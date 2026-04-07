package com.alvaroarmas.animacionpersonalizada;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

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

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Se limpia la pantalla
        stateTime += Gdx.graphics.getDeltaTime(); // Se acumula el tiempo que lleva la animación

        float delta = Gdx.graphics.getDeltaTime();

        isMoving = false;

        // Inputs
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)) {
            posY += speed * delta;
            currentAnimation = walkUp;
            isMoving = true;
        } else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S)) {
            posY -= speed * delta;
            currentAnimation = walkDown;
            isMoving = true;
        }

        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A)) {
            posX -= speed * delta;
            currentAnimation = walkLeft;
            isMoving = true;
        } else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) {
            posX += speed * delta;
            currentAnimation = walkRight;
            isMoving = true;
        }

        // Solo acumulamos tiempo si el personaje se está moviendo
        if (isMoving) {
            stateTime += delta;
        } else {
            stateTime = 0.125f;
        }

        // Draw
        TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTime, true);

        spriteBatch.begin();
        spriteBatch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteBatch.draw(currentFrame, posX, posY, 32, 32);
        spriteBatch.end();
    }

    @Override
    public void dispose() { // Los SpriteBatches y Textures deben ser desechados siempre
        spriteBatch.dispose();
        background.dispose();


    }
}
