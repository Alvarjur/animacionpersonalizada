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

    // Objetos usados
    Animation<TextureRegion> walkAnimation; // Se debe declarar el tipo de frame (TextureRegion)
    Texture walkRightSheet;
    Texture walkLeftSheet;
    Texture walkUpSheet;
    Texture walkDownSheet;
    Texture background;
    SpriteBatch spriteBatch;

    // Una variable para el tracking del elapsed time de la animación
    float stateTime;
    float speed = 200;
    float posX;

    @Override
    public void create() {

        // Se carga la textura con los frames de la animación separados
        walkRightSheet = new Texture(Gdx.files.internal("cloud_walk_right.png"));
        walkLeftSheet = new Texture(Gdx.files.internal("cloud_walk_left.png"));
        walkDownSheet = new Texture(Gdx.files.internal("cloud_walk_down.png"));
        walkUpSheet = new Texture(Gdx.files.internal("cloud_walk_up3.png")); // Por qué peta con el up3?
        background = new Texture(Gdx.files.internal("the_surface.png"));

        // Se divide la imagen para crear un array 2D de TextureRegions, esto se puede hacer gracias
        // a que la imagen está dividida a partes iguales
        TextureRegion[][] tmp = TextureRegion.split(walkUpSheet,
            walkRightSheet.getWidth() / FRAME_COLS,
            walkRightSheet.getHeight() / FRAME_ROWS);

        // Se colocan las regiones en un array 1D con el orden correcto de la animación
        TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                walkFrames[index++] = tmp[i][j];
            }
        }

        // Se inicializa la Animation con el intervalo de frames y el array de los frames
        walkAnimation = new Animation<TextureRegion>(0.125f, walkFrames);

        // Se instancia un SpriteBatch para el dibujo y se resetea el elapsed animation
        spriteBatch = new SpriteBatch();
        stateTime = 0f;
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Se limpia la pantalla
        stateTime += Gdx.graphics.getDeltaTime(); // Se acumula el tiempo que lleva la animación

        posX += speed * Gdx.graphics.getDeltaTime(); // Haciendo que se mueva (se multiplica por el deltaTime para que los fps no influyan)



        // Se obtiene el frame actual de la animación para el stateTime actual
        TextureRegion currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        spriteBatch.begin();

        spriteBatch.draw(currentFrame, 50, 50, 128, 150); // Se dibuja el frame en (50, 50)
        spriteBatch.end();
    }

    @Override
    public void dispose() { // Los SpriteBatches y Textures deben ser desechados siempre
        spriteBatch.dispose();
        walkRightSheet.dispose();
    }
}
