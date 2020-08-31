package site.fifa.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Data
public class NewTeamCreateRequest {

    @NotBlank
    private String teamName;
    @NotBlank
    private String countryName;
}
