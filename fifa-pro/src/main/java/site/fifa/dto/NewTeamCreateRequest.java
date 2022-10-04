package site.fifa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NewTeamCreateRequest {

    @NotBlank
    private String teamName;
    @NotBlank
    private String countryName;
}
