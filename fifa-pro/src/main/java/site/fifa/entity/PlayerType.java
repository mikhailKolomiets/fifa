package site.fifa.entity;

public enum PlayerType {

    GK, CD, MD, ST;

    public PlayerType random() {
        return values()[(int) (Math.random() * values().length)];
    }

}
