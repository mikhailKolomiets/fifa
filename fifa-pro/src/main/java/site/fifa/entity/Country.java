package site.fifa.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "country_id")
    private Long countryId;

    @NotBlank
    @Column(name = "country_name")
    private String countryName;

    @Min(value = 1)
    @Column(name = "team_max_number")
    private int teamMaxNumber = 1;

    public Country(String countryName) {
        this.countryName = countryName;
    }

    public Country(Long countryId) {this.countryId = countryId;}
}
