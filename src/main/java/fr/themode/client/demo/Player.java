package fr.themode.client.demo;

public class Player {

    private float x, y;

    public void move(float x, float y) {
        this.x += x;
        this.y += y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
