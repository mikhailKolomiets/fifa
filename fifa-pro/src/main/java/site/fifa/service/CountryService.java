package site.fifa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.fifa.dto.NewTeamCreateRequest;
import site.fifa.entity.Country;
import site.fifa.repository.CountryRepository;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;
    private final TeamService teamService;
    /**
     *  add country to the database only from this code place
     */
    private final List<Country> stubCountries = Arrays.asList(
        new Country(1L, "Ukraine", 1),
        new Country(2L, "Germany", 1),
        new Country(3L, "France", 1),
            new Country(null, "Spain", 15)
    );

    @PostConstruct
    public List<Country> getCountries() {
        List<Country> countries = (List<Country>) countryRepository.findAll();

        if (countries.size() < stubCountries.size()) {
            for (int i = countries.size(); i < stubCountries.size(); i++ ) {
                countries.add(countryRepository.save(stubCountries.get(i)));
            }
        }

        if (teamService.getFreeByCountry(1L).isEmpty()) {
            teamService.createNewTeam(new NewTeamCreateRequest("Карпати", "Ukraine"));
            teamService.createNewTeam(new NewTeamCreateRequest("Металіст-1925", "Ukraine"));
            teamService.createNewTeam(new NewTeamCreateRequest("Динамо", "Ukraine"));
            teamService.createNewTeam(new NewTeamCreateRequest("Ворскла", "Ukraine"));
            teamService.createNewTeam(new NewTeamCreateRequest("Дніпро", "Ukraine"));
            teamService.createNewTeam(new NewTeamCreateRequest("ЗСУ", "Ukraine"));
            teamService.createNewTeam(new NewTeamCreateRequest("Таврія", "Ukraine"));
            teamService.createNewTeam(new NewTeamCreateRequest("Кривбас", "Ukraine"));
        }


        return countries;
    }
}
