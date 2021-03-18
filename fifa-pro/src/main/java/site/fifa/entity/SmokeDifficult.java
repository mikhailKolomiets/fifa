package site.fifa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum  SmokeDifficult {

    EASY(5),
    NORMAL(10),
    HARD(15);

    private final int percent;

}
