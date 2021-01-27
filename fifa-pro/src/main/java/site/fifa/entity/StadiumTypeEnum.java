package site.fifa.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StadiumTypeEnum {

    VILLAGE(100, 0, 1, "https://tramplinsport.ru/uploads/images/3-09-16-2019.jpg"),
    DISTRICT (500, 5000, 12, "https://tramplinsport.ru/uploads/images/3-09-16-2019.jpg"),
    TOWN(3000, 25000, 100, "https://tramplinsport.ru/uploads/images/3-09-16-2019.jpg"),
    CITY(10000, 150000, 300, "https://tramplinsport.ru/uploads/images/3-09-16-2019.jpg"),
    WORLD(50000, 1000000, 1000, "https://tramplinsport.ru/uploads/images/3-09-16-2019.jpg");

    private final int population;
    private final int price;
    private final int updateHours;
    private final String defaultImageLink;
}
